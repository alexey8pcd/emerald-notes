package ru.alexey_ovcharov.webserver.logic;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
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
public class ClientReferencesHandler {

    private static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    private Logger logger = LoggerFactory.createConsoleLogger("referenceHandler");
    private InitialContext initialContext;

    public ClientReferencesHandler() throws NamingException {
        initialContext = new InitialContext();
    }

    public void handle(JSONObject requestAsJSON) throws Exception {
        logger.debug("Обрабатываю запрос слияния справочников с удаленного устройства");
        UserTransaction userTransaction = null;
        try {
            userTransaction = (UserTransaction) initialContext.lookup("java:comp/UserTransaction");
            userTransaction.begin();
            EntityManager entityManager;
            Map<Integer, PlaceTypes> linkedPlaceTypes = getPlacesTypesBinding(requestAsJSON);
            JSONArray jsonArrayImages = requestAsJSON.getJSONArray("images");
            Map<Integer, byte[]> images = getImages(jsonArrayImages);

            JSONArray placesJSONArray = requestAsJSON.getJSONArray("places");
            for (int i = 0; i < placesJSONArray.length(); ++i) {
                JSONObject placeInfoJSON = placesJSONArray.getJSONObject(i);
                parsePlaceInfo(placeInfoJSON, linkedPlaceTypes, images);
            }
            userTransaction.commit();
        } catch (Exception ex) {
            logger.error(ex, ex);
            if (userTransaction != null) {
                userTransaction.rollback();
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
        logger.debug("Готово");
        return linkedPlaceTypes;
    }

    @NotNull
    private PlaceTypes findOrCreatePlaceTypeByName(@NotNull String placeType) {
//        TypedQuery<PlaceTypes> typedQuery = entityManager.createQuery("SELECT p "
//                + "FROM PlaceTypes p WHERE p.type= :placeType", PlaceTypes.class);
//        typedQuery.setParameter("placeType", placeType);
//        List<PlaceTypes> resultList = typedQuery.getResultList();
//        if (!resultList.isEmpty()) {
//            return resultList.get(0);
//        } else {
//            PlaceTypes placeTypes = new PlaceTypes();
//            placeTypes.setType(placeType);
//            entityManager.persist(placeTypes);
//            return placeTypes;
//        }
        throw new UnsupportedOperationException();
    }

    @Nullable
    private Places findPlace(String description,
            Date dateCreate) throws ParseException, JSONException {
//        logger.debug("Выполняю поиск места по дате и описанию");
//        TypedQuery<Places> query = entityManager.createQuery("SELECT p FROM Places p WHERE "
//                + "p.description= :description AND p.dateCreate= :dateCreate", Places.class);
//        query.setParameter("description", description);
//        query.setParameter("dateCreate", dateCreate);
//        List<Places> foundPlaces = query.getResultList();
//        Places place = null;
//        if (!foundPlaces.isEmpty()) {
//            place = foundPlaces.get(0);
//            logger.debug("Место уже существует");
//        } else {
//            logger.debug("Такого места не найдено");
//        }
//        return place;
        throw new UnsupportedOperationException();
    }

    @NotNull
    private Places createPlace(String description, Date dateCreate,
            JSONObject placeInfoJSON, PlaceTypes placeTypesLocal) throws JSONException {
        logger.debug("Создаю новое место");
        Places place = new Places();
        place.setDescription(description);
        place.setDateCreate(dateCreate);
        place.setIdPlaceType(placeTypesLocal);

        String address = placeInfoJSON.getString("address");
        place.setAddress(address);

        @Nullable
        BigDecimal latitude = LangUtil.toDecimalOrNullIfEmpty(placeInfoJSON.optString("latitude"));
        place.setLatitude(latitude);

        @Nullable
        BigDecimal longitude = LangUtil.toDecimalOrNullIfEmpty(placeInfoJSON.optString("longitude"));
        place.setLongitude(longitude);

        @Nullable
        Countries country = findCountryByName(placeInfoJSON.optString("country"));
        place.setIdCountry(country);

//        entityManager.persist(place);
        logger.debug("Создано");
        return place;

    }

    @Nullable
    private Countries findCountryByName(String countryName) {
//        if (countryName != null) {
//            TypedQuery<Countries> typedQuery = entityManager.createQuery(
//                    "SELECT c FROM Countries c WHERE C.country= :countryName", Countries.class);
//            typedQuery.setParameter("countryName", countryName);
//            Countries countries = typedQuery.getSingleResult();
//            return countries;
//        } else {
//            return null;
//        }
        throw new UnsupportedOperationException();
    }

    @NotNull
    private List<ImagesForPlace> createNewImagesForPlace(Places place, List<byte[]> imagesBinary) {
        List<ImagesForPlace> imagesForPlaces = new ArrayList<>(imagesBinary.size());
        for (byte[] bs : imagesBinary) {
            ImagesForPlace imagesForPlace = new ImagesForPlace();
            imagesForPlace.setIdPlace(place);
            Images images = new Images();
            images.setImageData(bs);
            imagesForPlace.setIdImage(images);
            imagesForPlaces.add(imagesForPlace);
        }
        return imagesForPlaces;
    }

    @NotNull
    private List<byte[]> getOnlyNewImages(Collection<ImagesForPlace> imagesForPlaces, List<byte[]> imagesBinary) {
        List<byte[]> res = new ArrayList<>(imagesBinary);
        for (Iterator<byte[]> iterator = res.iterator(); iterator.hasNext();) {
            byte[] bytes = iterator.next();
            for (ImagesForPlace imageForPlace : imagesForPlaces) {
                if (Arrays.equals(bytes, imageForPlace.getIdImage().getImageData())) {
                    iterator.remove();
                    break;
                }
            }
        }
        return res;
    }

    private void parsePlaceInfo(JSONObject placeInfoJSON, Map<Integer, PlaceTypes> linkedPlaceTypes, Map<Integer, byte[]> images) throws Exception {
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
        if (place == null) {
            place = createPlace(description, dateCreate, placeInfoJSON, placeTypesLocal);
        }

        JSONArray imagesJSONArray = placeInfoJSON.getJSONArray("images");
        if (imagesJSONArray.length() > 0) {
            List<byte[]> imagesBinary = new ArrayList<>(imagesJSONArray.length());
            for (int j = 0; j < imagesJSONArray.length(); ++j) {
                String encodedImageBase64Binary = imagesJSONArray.getString(j);
                byte[] imageBytes = Base64.getDecoder().decode(encodedImageBase64Binary);
                imagesBinary.add(imageBytes);
            }

            Collection<ImagesForPlace> imagesForPlace = place.getImagesForPlaceCollection();
            if (imagesForPlace == null || imagesForPlace.isEmpty()) {
                List<ImagesForPlace> existsImagesForPlace = createNewImagesForPlace(place, imagesBinary);
                place.setImagesForPlaceCollection(existsImagesForPlace);
            } else {
                List<byte[]> newImages = getOnlyNewImages(imagesForPlace, imagesBinary);
                List<ImagesForPlace> createdImagesForPlace = createNewImagesForPlace(place, newImages);
                imagesForPlace.addAll(createdImagesForPlace);
            }
//            entityManager.refresh(place);
            throw new UnsupportedOperationException();
        }
    }

    @NotNull
    private Map<Integer, byte[]> getImages(JSONArray jsonArray) throws JSONException {
        Map<Integer, byte[]> result = new HashMap<>(jsonArray.length());
        Base64.Decoder decoder = Base64.getDecoder();
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject imageJSON = jsonArray.getJSONObject(i);
            int idImage = imageJSON.getInt("id_image");
            String decodedBytes = imageJSON.getString("image_data");
            byte[] imageBytes = decoder.decode(decodedBytes);
            result.put(idImage, imageBytes);
        }
        return result;
    }

}
