package ru.alexey_ovcharov.webserver.logic;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    public void handleReceivePlaces(JSONObject requestAsJSON) throws Exception {
        logger.debug("Обрабатываю запрос слияния справочников с удаленного устройства");
        UserTransaction transaction = context.getUserTransaction();
        try {
            transaction.begin();
            Map<Integer, PlaceTypes> linkedPlaceTypes = getPlacesTypesBinding(requestAsJSON);
            JSONArray jsonArrayImages = requestAsJSON.getJSONArray("images");
            Map<Integer, Images> allRemoteImages = getImages(jsonArrayImages);

            JSONArray placesJSONArray = requestAsJSON.getJSONArray("places");
            for (int i = 0; i < placesJSONArray.length(); ++i) {
                JSONObject placeInfoJSON = placesJSONArray.getJSONObject(i);
                parsePlaceInfo(placeInfoJSON, linkedPlaceTypes, allRemoteImages);
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
            String guid = placeTypeJSON.getString("guid");
            PlaceTypes placeTypesLocal = findOrCreatePlaceTypeByName(placeType, guid);
            linkedPlaceTypes.put(idPlaceTypeRemote, placeTypesLocal);
        }
        entityManager.flush();
        logger.debug("Готово");
        return linkedPlaceTypes;
    }

    @NotNull
    private PlaceTypes findOrCreatePlaceTypeByName(@NotNull String placeType,
            @NotNull String guid) {
        TypedQuery<PlaceTypes> typedQuery = entityManager.createQuery("SELECT p "
                + "FROM PlaceTypes p WHERE p.type= :placeType", PlaceTypes.class);
        typedQuery.setParameter("placeType", placeType);
        List<PlaceTypes> resultList = typedQuery.getResultList();
        if (!resultList.isEmpty()) {
            return resultList.get(0);
        } else {
            PlaceTypes placeTypes = new PlaceTypes();
            placeTypes.setType(placeType);
            placeTypes.setGuid(UUID.fromString(guid));
            entityManager.persist(placeTypes);
            return placeTypes;
        }
    }

    @Nullable
    private Places findPlace(String guid) throws ParseException, JSONException {
        logger.debug("Выполняю поиск места guid: " + guid);
        TypedQuery<Places> query = entityManager.createQuery("SELECT p FROM "
                + "Places p WHERE p.guid= :guid", Places.class);
        query.setParameter("guid", UUID.fromString(guid));
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
    private Places createPlace(JSONObject placeInfoJSON, PlaceTypes placeTypesLocal) throws JSONException, ParseException {
        Places place = new Places();
        String guid = placeInfoJSON.getString("guid");
        String description = placeInfoJSON.getString("description");
        Date dateCreate = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD).parse(
                placeInfoJSON.optString("date_create"));

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
        place.setGuid(UUID.fromString(guid));
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
            Collection<Images> remoteImagesForPlace) {

        List<ImagesForPlace> imagesForPlaces = new ArrayList<>(remoteImagesForPlace.size());
        for (Images remoteImageForThisPlace : remoteImagesForPlace) {
            ImagesForPlace imagesForPlace = new ImagesForPlace();
            imagesForPlace.setIdPlace(place);
            imagesForPlace.setIdImage(remoteImageForThisPlace);
            imagesForPlaces.add(imagesForPlace);
        }
        return imagesForPlaces;
    }

    private void parsePlaceInfo(JSONObject placeInfoJSON,
            Map<Integer, PlaceTypes> linkedPlaceTypes,
            Map<Integer, Images> allRemoteImages) throws Exception {

        int idPlaceTypeRemote = placeInfoJSON.getInt("id_place_type");
        PlaceTypes placeTypesLocal = linkedPlaceTypes.get(idPlaceTypeRemote);
        if (placeTypesLocal == null) {
            throw new RuntimeException("Не найден тип места с идентификатором "
                    + idPlaceTypeRemote + " в справочнике типов, полученном удаленно");
        }
        String guid = placeInfoJSON.getString("guid");
        Places place = findPlace(guid);
        boolean placeExists;
        if (place == null) {
            placeExists = false;
            place = createPlace(placeInfoJSON, placeTypesLocal);
            //еще не сохранено в базе
        } else {
            placeExists = true;
        }

        JSONArray imagesJSONArrayForPlace = placeInfoJSON.getJSONArray("images");
        if (imagesJSONArrayForPlace.length() > 0) {
            Set<Integer> remoteImagesIdsForPlace = getRemoteImagesIdsForPlace(imagesJSONArrayForPlace);

            Map<Integer, Images> remoteImagesForPlace
                    = getImagesByIds(allRemoteImages, remoteImagesIdsForPlace);

            Collection<ImagesForPlace> imagesForPlace = place.getImagesForPlaceCollection();
            if (imagesForPlace == null || imagesForPlace.isEmpty()) {

                List<ImagesForPlace> newImagesForPlace
                        = createNewImagesForPlace(place, remoteImagesForPlace.values());
                place.setImagesForPlaceCollection(newImagesForPlace);
            } else {

                Set<Images> newRemoteImagesIds
                        = getOnlyNewImages(imagesForPlace, remoteImagesForPlace);
                if (!newRemoteImagesIds.isEmpty()) {
                    List<ImagesForPlace> newImageForPlaces
                            = createNewImagesForPlace(place, newRemoteImagesIds);
                    imagesForPlace.addAll(newImageForPlaces);
                }

            }
        }
        if (placeExists) {
            entityManager.refresh(place);
        } else {
            entityManager.persist(place);
        }
    }

    @NotNull
    private Set<Integer> getRemoteImagesIdsForPlace(JSONArray imagesJSONArray) throws JSONException {
        Set<Integer> imagesIds = new HashSet<>(imagesJSONArray.length());
        for (int j = 0; j < imagesJSONArray.length(); ++j) {
            int imageId = imagesJSONArray.getInt(j);
            imagesIds.add(imageId);
        }
        return imagesIds;
    }

    @NotNull
    private Map<Integer, Images> getImages(JSONArray jsonArray)
            throws JSONException, UnsupportedEncodingException {
        Map<Integer, Images> result = new HashMap<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject imageJSON = jsonArray.getJSONObject(i);
            Images image = new Images(imageJSON);
            result.put(image.getIdImage(), image);
        }
        return result;
    }

    @NotNull
    private Set<Images> getOnlyNewImages(
            Collection<ImagesForPlace> imagesForPlaceExists, Map<Integer, Images> imagesForPlaceFromRequest) {
        Set<Images> result = new HashSet<>();
        Map<String, Images> existImages = new HashMap<>();
        for (ImagesForPlace ifp : imagesForPlaceExists) {
            existImages.put(ifp.getIdImage().getGuid().toString(), ifp.getIdImage());
        }

        for (Images image : imagesForPlaceFromRequest.values()) {
            Images imageWithSameRemoteId = existImages.get(image.getGuid().toString());
            if (imageWithSameRemoteId == null) {
                result.add(image);
            } else {
                //такое изображение уже есть в базе
            }
        }
        return result;
    }

    @NotNull
    private Map<Integer, Images> getImagesByIds(Map<Integer, Images> allRemoteImages,
            Set<Integer> remoteImagesIdsForPlace) {
        Map<Integer, Images> result = new HashMap<>();
        for (Integer idRemoteImage : remoteImagesIdsForPlace) {
            result.put(idRemoteImage, allRemoteImages.get(idRemoteImage));
        }
        return result;
    }

    public void handleReceiveThings(JSONObject jSONObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String handleGetPlaces() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String handleGetThings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
