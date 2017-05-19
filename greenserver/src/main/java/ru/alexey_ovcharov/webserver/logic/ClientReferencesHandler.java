package ru.alexey_ovcharov.webserver.logic;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.alexey_ovcharov.webserver.persist.Countries;
import ru.alexey_ovcharov.webserver.persist.Images;
import ru.alexey_ovcharov.webserver.persist.ImagesForPlace;
import ru.alexey_ovcharov.webserver.persist.PlaceTypes;
import ru.alexey_ovcharov.webserver.persist.Places;
import ru.alexey_ovcharov.webserver.util.LangUtil;
import ru.alexey_ovcharov.webserver.util.LoggerFactory;
import ru.alexey_ovcharov.webserver.util.NotNull;
import ru.alexey_ovcharov.webserver.util.Nullable;
import org.apache.commons.codec.binary.Base64;

/**
 * @author Alexey
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ClientReferencesHandler {

    private static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    private final Logger logger = LoggerFactory.createConsoleLogger("referenceHandler");

    @PersistenceContext(unitName = "ru.alexey_ovcharov_webserver_war_1.0PU")
    private EntityManager entityManager;

    @Resource
    private EJBContext context;

    public ClientReferencesHandler() {
    }

    public void handle(JSONObject requestAsJSON) throws Exception {
        logger.debug("Обрабатываю запрос слияния справочников с удаленного устройства");
        UserTransaction transaction = context.getUserTransaction();
        try {
            transaction.begin();
            Map<Integer, PlaceTypes> linkedPlaceTypes = getPlacesTypesBinding(requestAsJSON);
            JSONArray jsonArrayImages = requestAsJSON.getJSONArray("images");
            Map<Integer, byte[]> images = getImages(jsonArrayImages);
            String remoteDatabaseId = requestAsJSON.optString("database_id");

            JSONArray placesJSONArray = requestAsJSON.getJSONArray("places");
            for (int i = 0; i < placesJSONArray.length(); ++i) {
                JSONObject placeInfoJSON = placesJSONArray.getJSONObject(i);
                parsePlaceInfo(placeInfoJSON, linkedPlaceTypes, images, remoteDatabaseId);
            }
            transaction.commit();
        } catch (Exception ex) {
            logger.error(ex, ex);
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }
        logger.debug("Завершено успешно");
    }

    @NotNull
    private Map<Integer, PlaceTypes> getPlacesTypesBinding(JSONObject jsono) throws JSONException {
        logger.debug("Выполняю связывание по типам мест");
        JSONArray placeTypesJSON = jsono.getJSONArray("place_types");
        Map<Integer, PlaceTypes> linkedPlaceTypes = new HashMap<>();
        for (int i = 0; i < placeTypesJSON.length(); ++i) {
            JSONObject placeTypeJSON = placeTypesJSON.getJSONObject(i);
            String placeType = placeTypeJSON.getString("type");
            int idPlaceTypeRemote = placeTypeJSON.getInt("id_place_type");
            PlaceTypes placeTypesLocal = findOrCreatePlaceTypeByName(placeType);
            linkedPlaceTypes.put(idPlaceTypeRemote, placeTypesLocal);
        }
        entityManager.flush();
        logger.debug("Готово");
        return linkedPlaceTypes;
    }

    @NotNull
    private PlaceTypes findOrCreatePlaceTypeByName(@NotNull String placeType) {
        TypedQuery<PlaceTypes> typedQuery = entityManager.createQuery("SELECT p "
                + "FROM PlaceTypes p WHERE p.type= :placeType", PlaceTypes.class);
        typedQuery.setParameter("placeType", placeType);
        List<PlaceTypes> resultList = typedQuery.getResultList();
        if (!resultList.isEmpty()) {
            return resultList.get(0);
        } else {
            PlaceTypes placeTypes = new PlaceTypes();
            placeTypes.setType(placeType);
            entityManager.persist(placeTypes);
            return placeTypes;
        }
    }

    @Nullable
    private Places findPlace(String description,
            Date dateCreate) throws ParseException, JSONException {
        logger.debug("Выполняю поиск места по дате и описанию");
        TypedQuery<Places> query = entityManager.createQuery("SELECT p FROM Places p WHERE "
                + "p.description= :description AND p.dateCreate= :dateCreate", Places.class);
        query.setParameter("description", description);
        query.setParameter("dateCreate", dateCreate);
        List<Places> foundPlaces = query.getResultList();
        Places place = null;
        if (!foundPlaces.isEmpty()) {
            place = foundPlaces.get(0);
            logger.debug("Место уже существует");
        } else {
            logger.debug("Такого места не найдено");
        }
        return place;
    }

    @NotNull
    private Places createPlace(String description, Date dateCreate,
            JSONObject placeInfoJSON, PlaceTypes placeTypesLocal) throws JSONException {
        Places place = new Places();
        place.setDescription(description);
        place.setDateCreate(dateCreate);
        place.setIdPlaceType(placeTypesLocal);

        if (placeInfoJSON.has("address")) {
            String address = placeInfoJSON.getString("address");
            place.setAddress(address);
        }

        @Nullable
        BigDecimal latitude = LangUtil.toDecimalOrNullIfEmpty(placeInfoJSON.optString("latitude"));
        place.setLatitude(latitude);

        @Nullable
        BigDecimal longitude = LangUtil.toDecimalOrNullIfEmpty(placeInfoJSON.optString("longitude"));
        place.setLongitude(longitude);

        @Nullable
        Countries country = findCountryByName(placeInfoJSON.optString("country"));
        place.setIdCountry(country);
        return place;

    }

    @Nullable
    private Countries findCountryByName(String countryName) {
        if (countryName != null) {
            TypedQuery<Countries> typedQuery = entityManager.createQuery(
                    "SELECT c FROM Countries c WHERE C.country= :countryName", Countries.class);
            typedQuery.setParameter("countryName", countryName);
            List<Countries> countries = typedQuery.getResultList();
            if (!countries.isEmpty()) {
                return countries.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @NotNull
    private List<ImagesForPlace> createNewImagesForPlace(Places place,
            String remoteDatabaseId, Set<Integer> remoteImageIds,
            Map<Integer, byte[]> remoteImagesData) {

        List<ImagesForPlace> imagesForPlaces = new ArrayList<>(remoteImageIds.size());
        for (Integer remoteImageIdForThisPlace : remoteImageIds) {
            Images image = new Images();
            image.setImageData(remoteImagesData.get(remoteImageIdForThisPlace));
            image.setIdInRemoteDatabase(remoteImageIdForThisPlace);
            image.setRemoteDatabaseId(remoteDatabaseId);

            ImagesForPlace imagesForPlace = new ImagesForPlace();
            imagesForPlace.setIdPlace(place);
            imagesForPlace.setIdImage(image);

            imagesForPlaces.add(imagesForPlace);
        }
        return imagesForPlaces;
    }

    private void parsePlaceInfo(JSONObject placeInfoJSON,
            Map<Integer, PlaceTypes> linkedPlaceTypes,
            Map<Integer, byte[]> remoteImagesData, String remoteDatabaseId) throws Exception {

        int idPlaceTypeRemote = placeInfoJSON.getInt("id_place_type");
        PlaceTypes placeTypesLocal = linkedPlaceTypes.get(idPlaceTypeRemote);
        if (placeTypesLocal == null) {
            throw new RuntimeException("Не найден тип места с идентификатором "
                    + idPlaceTypeRemote + " в справочнике типов, полученном удаленно");
        }

        String description = placeInfoJSON.getString("description");
        Date dateCreate = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD).parse(
                placeInfoJSON.optString("date_create"));

        Places place = findPlace(description, dateCreate);
        boolean placeExists;
        if (place == null) {
            placeExists = false;
            place = createPlace(description, dateCreate, placeInfoJSON, placeTypesLocal);
            //еще не сохранено в базе
        } else {
            placeExists = true;
        }

        JSONArray imagesJSONArray = placeInfoJSON.getJSONArray("images");
        if (imagesJSONArray.length() > 0) {
            Set<Integer> remoteImageIds = getRemoteImageIds(imagesJSONArray);

            Collection<ImagesForPlace> imagesForPlace = place.getImagesForPlaceCollection();
            if (imagesForPlace == null || imagesForPlace.isEmpty()) {
                List<ImagesForPlace> newImagesForPlace
                        = createNewImagesForPlace(place, remoteDatabaseId,
                                remoteImageIds, remoteImagesData);
                place.setImagesForPlaceCollection(newImagesForPlace);
            } else {
                Set<Integer> newRemoteImagesIds
                        = getOnlyNewImageIds(remoteDatabaseId, imagesForPlace, remoteImageIds);
                List<ImagesForPlace> newImageForPlaces
                        = createNewImagesForPlace(place, remoteDatabaseId,
                                newRemoteImagesIds, remoteImagesData);
                imagesForPlace.addAll(newImageForPlaces);
            }
        }
        if (placeExists) {
            entityManager.refresh(place);
        } else {
            entityManager.persist(place);
        }
    }

    private Set<Integer> getRemoteImageIds(JSONArray imagesJSONArray) throws JSONException {
        Set<Integer> imagesIds = new HashSet<>(imagesJSONArray.length());
        for (int j = 0; j < imagesJSONArray.length(); ++j) {
            int imageId = imagesJSONArray.getInt(j);
            imagesIds.add(imageId);
        }
        return imagesIds;
    }

    @NotNull
    private Map<Integer, byte[]> getImages(JSONArray jsonArray)
            throws JSONException, UnsupportedEncodingException {

        Map<Integer, byte[]> result = new HashMap<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject imageJSON = jsonArray.getJSONObject(i);
            int idImage = imageJSON.getInt("id_image");
            String decodedBytes = imageJSON.getString("binary_data");
            byte[] imageBytes = Base64.decodeBase64(decodedBytes);
            result.put(idImage, imageBytes);
        }
        return result;
    }

    @NotNull
    private Set<Integer> getOnlyNewImageIds(String remoteDatabaseId,
            Collection<ImagesForPlace> imagesForPlace, Set<Integer> remoteImageIds) {
        Set<Integer> result = new HashSet<>();
        Map<Integer, Images> existImages = new HashMap<>();
        for (ImagesForPlace ifp : imagesForPlace) {
            existImages.put(ifp.getIdImage().getIdInRemoteDatabase(), ifp.getIdImage());
        }

        for (Integer remoteImageId : remoteImageIds) {
            Images imageWithSameRemoteId = existImages.get(remoteImageId);
            if (imageWithSameRemoteId != null) {
                if (Objects.equals(imageWithSameRemoteId.getIdInRemoteDatabase(), remoteDatabaseId)) {
                    //такое изображение уже есть в базе
                    continue;
                }
            }
            result.add(remoteImageId);
        }
        return result;
    }

}
