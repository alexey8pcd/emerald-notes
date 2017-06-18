package ru.alexey_ovcharov.webserver.logic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
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
import ru.alexey_ovcharov.webserver.common.util.LoggerFactory;
import ru.alexey_ovcharov.webserver.common.util.NotNull;
import ru.alexey_ovcharov.webserver.common.util.Nullable;
import ru.alexey_ovcharov.webserver.persist.CategoriesOfThings;
import ru.alexey_ovcharov.webserver.persist.ImagesForThing;
import ru.alexey_ovcharov.webserver.persist.Things;

/**
 * @author Alexey
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ClientReferencesSaver {

    private static final String GUID = "guid";
    private static final String IMAGES = "images";
    private static final String COUNTRY = "country";

    private final Logger logger
            = LoggerFactory.createConsoleLogger(getClass().getSimpleName());

    @PersistenceContext(unitName = "ru.alexey_ovcharov_webserver_war_1.0PU")
    private EntityManager entityManager;

    @Resource
    private EJBContext context;

    public ClientReferencesSaver() {
    }

    public void savePlaces(JSONObject placesJSON) throws Exception {
        logger.debug("Обрабатываю запрос слияния справочников с удаленного устройства");
        UserTransaction transaction = context.getUserTransaction();
        try {
            transaction.begin();
            Map<Integer, PlaceTypes> linkedPlaceTypes = getPlacesTypesBinding(placesJSON);
            JSONArray placesJSONArray = placesJSON.getJSONArray("places");
            for (int i = 0; i < placesJSONArray.length(); ++i) {
                JSONObject placeInfoJSON = placesJSONArray.getJSONObject(i);
                savePlaceInfo(placeInfoJSON, linkedPlaceTypes);
            }
            transaction.commit();
            logger.debug("Завершено успешно");
        } catch (Exception ex) {
            logger.error(ex, ex);
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }
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
        logger.debug("Связывание мест завершено: " + linkedPlaceTypes.size() + " типов");
        return linkedPlaceTypes;
    }

    @NotNull
    private PlaceTypes findOrCreatePlaceTypeByName(@NotNull String placeType,
            @NotNull String guid) {
        entityManager.flush();
        entityManager.clear();
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

    private void savePlaceInfo(JSONObject placeInfoJSON,
            Map<Integer, PlaceTypes> linkedPlaceTypes) throws Exception {

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
            place = new Places(placeInfoJSON);
            place.setIdPlaceType(placeTypesLocal);
            String countryName = placeInfoJSON.optString(COUNTRY);
            Countries country = findCountryByName(countryName);
            place.setIdCountry(country);
            //еще не сохранено в базе
        } else {
            placeExists = true;
        }
        JSONArray imagesJSONArrayForPlace = placeInfoJSON.getJSONArray(IMAGES);
        if (imagesJSONArrayForPlace.length() > 0) {
            Set<UUID> remoteImagesUUIDSForPlace = new HashSet<>();
            for (int i = 0; i < imagesJSONArrayForPlace.length(); i++) {
                String guidImg = imagesJSONArrayForPlace.getString(i);
                remoteImagesUUIDSForPlace.add(UUID.fromString(guidImg));
            }
            Collection<ImagesForPlace> imagesForPlace = place.getImagesForPlaceCollection();
            if (imagesForPlace == null || imagesForPlace.isEmpty()) {
                List<ImagesForPlace> newImagesForPlace
                        = createNewImagesForPlace(place, remoteImagesUUIDSForPlace);
                place.setImagesForPlaceCollection(newImagesForPlace);
            } else {
                Set<UUID> newRemoteImagesIds
                        = getOnlyNewImages(imagesForPlace, remoteImagesUUIDSForPlace);
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

    @Nullable
    private Places findPlace(String guid) throws ParseException, JSONException {
        logger.debug("Выполняю поиск места guid: " + guid);
        entityManager.flush();
        entityManager.clear();
        TypedQuery<Places> query = entityManager.createQuery("SELECT p FROM "
                + "Places p WHERE p.guid= :guidp", Places.class);
        query.setParameter("guidp", UUID.fromString(guid));
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

    public void saveThings(JSONObject thingsJSON) throws Exception {
        logger.debug("Обрабатываю запрос слияния энциклопедии с удаленного устройства");
        UserTransaction transaction = context.getUserTransaction();
        try {
            transaction.begin();
            Map<Integer, CategoriesOfThings> linkedThingsTypes = getThingsTypesBinding(thingsJSON);
            JSONArray thingJSONArray = thingsJSON.getJSONArray("things");
            for (int i = 0; i < thingJSONArray.length(); ++i) {
                JSONObject thingsInfoJSON = thingJSONArray.getJSONObject(i);
                saveThingInfo(thingsInfoJSON, linkedThingsTypes);
            }
            transaction.commit();
            logger.debug("Завершено успешно");
        } catch (Exception ex) {
            logger.error(ex, ex);
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }
    }

    @NotNull
    private Map<Integer, CategoriesOfThings> getThingsTypesBinding(JSONObject thingsJSON) throws JSONException {
        logger.debug("Выполняю связывание по типам объектов энциклопедии");
        JSONArray thingsTypesJSON = thingsJSON.getJSONArray("categories_of_things");
        Map<Integer, CategoriesOfThings> linkedCategoriesMap = new HashMap<>();
        for (int i = 0; i < thingsTypesJSON.length(); i++) {
            JSONObject categoryJSON = thingsTypesJSON.getJSONObject(i);
            String categoryName = categoryJSON.getString("category");
            int idCategory = categoryJSON.getInt("id_category");
            String guid = categoryJSON.getString(GUID);
            CategoriesOfThings categoryOfThingsLocal = findOrCreateCategoryOfThingByName(categoryName, guid);
            linkedCategoriesMap.put(idCategory, categoryOfThingsLocal);
        }
        entityManager.flush();
        logger.debug("Связывание категорий завершено: " + linkedCategoriesMap.size() + " категорий");
        return linkedCategoriesMap;
    }

    private void saveThingInfo(JSONObject thingsInfoJSON,
            Map<Integer, CategoriesOfThings> linkedThingsTypes) throws JSONException {
        int idCategotyOfThingRemote = thingsInfoJSON.getInt("id_category");
        CategoriesOfThings categoryOfThingLocal = linkedThingsTypes.get(idCategotyOfThingRemote);
        if (categoryOfThingLocal == null) {
            throw new RuntimeException("Не найдена категория объекта энциклопедии с идентификатором "
                    + idCategotyOfThingRemote + " в справочнике категория объекта энциклопедии, полученном удаленно");
        }
        String guid = thingsInfoJSON.getString(GUID);
        Things thing = findThing(guid);
        boolean thingExists;
        if (thing == null) {
            thingExists = false;
            thing = new Things(thingsInfoJSON);
            thing.setIdCategory(categoryOfThingLocal);
            String countryName = thingsInfoJSON.optString(COUNTRY);
            Countries country = findCountryByName(countryName);
            thing.setIdCountry(country);
            //еще не сохранено в базе
        } else {
            thingExists = true;
        }
        JSONArray imagesJSONArrayForThing = thingsInfoJSON.getJSONArray(IMAGES);
        if (imagesJSONArrayForThing.length() > 0) {
            Set<UUID> remoteImagesUUIDSForThing = new HashSet<>();
            for (int i = 0; i < imagesJSONArrayForThing.length(); i++) {
                String guidImg = imagesJSONArrayForThing.getString(i);
                remoteImagesUUIDSForThing.add(UUID.fromString(guidImg));
            }
            Collection<ImagesForThing> imagesForThings = thing.getImagesForThingCollection();
            if (imagesForThings == null || imagesForThings.isEmpty()) {
                List<ImagesForThing> newImagesForThing
                        = createNewImagesForThing(thing, remoteImagesUUIDSForThing);
                thing.setImagesForThingCollection(newImagesForThing);
            } else {
                Set<UUID> newRemoteImagesIds
                        = getOnlyNewImagesForThing(imagesForThings, remoteImagesUUIDSForThing);
                if (!newRemoteImagesIds.isEmpty()) {
                    List<ImagesForThing> newImageForThing
                            = createNewImagesForThing(thing, newRemoteImagesIds);
                    imagesForThings.addAll(newImageForThing);
                }
            }
        }
        if (thingExists) {
            entityManager.refresh(thing);
        } else {
            entityManager.persist(thing);
        }
    }

    @NotNull
    private CategoriesOfThings findOrCreateCategoryOfThingByName(String categoryName, String guid) {
        entityManager.flush();
        entityManager.clear();
        TypedQuery<CategoriesOfThings> typedQuery
                = entityManager.createQuery("SELECT c FROM CategoriesOfThings c "
                        + "WHERE c.category= :categoryName", CategoriesOfThings.class);
        typedQuery.setParameter("categoryName", categoryName);
        List<CategoriesOfThings> resultList = typedQuery.getResultList();
        if (!resultList.isEmpty()) {
            return resultList.get(0);
        } else {
            CategoriesOfThings cot = new CategoriesOfThings();
            cot.setCategory(categoryName);
            cot.setGuid(UUID.fromString(guid));
            entityManager.persist(cot);
            return cot;
        }
    }

    @NotNull
    private List<ImagesForPlace> createNewImagesForPlace(Places place,
            Collection<UUID> remoteImagesUUIDSForPlace) {

        List<ImagesForPlace> imagesForPlaces = new ArrayList<>(remoteImagesUUIDSForPlace.size());
        for (UUID uuid : remoteImagesUUIDSForPlace) {
            Images images = new Images();
            images.setGuid(uuid);
            images.setImageData(new byte[0]);

            ImagesForPlace imagesForPlace = new ImagesForPlace();
            imagesForPlace.setIdPlace(place);
            imagesForPlace.setIdImage(images);
            imagesForPlaces.add(imagesForPlace);
        }
        return imagesForPlaces;
    }

    public void saveNotes(JSONObject notesJSON) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveImage(byte[] imageData, String imageGuid) throws Exception {
        logger.debug("Отрабатываю запрос на слияние изображений");
        UserTransaction transaction = context.getUserTransaction();
        try {
            transaction.begin();
            entityManager.flush();
            entityManager.clear();
            TypedQuery<Images> query = entityManager.createQuery(
                    "SELECT i FROM Images i WHERE i.guid=:guidp", Images.class);
            UUID uuid = UUID.fromString(imageGuid);
            query.setParameter("guidp", uuid);
            List<Images> resultList = query.getResultList();
            if (resultList.isEmpty()) {
                Images image = new Images();
                image.setGuid(uuid);
                image.setImageData(imageData);
                entityManager.persist(image);
            } else {
                Images image = resultList.get(0);
                image.setImageData(imageData);
                entityManager.merge(image);
            }
            entityManager.flush();
            transaction.commit();
            logger.debug("Завершено успешно");
        } catch (Exception ex) {
            logger.error(ex, ex);
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }

    }

    @NotNull
    private Set<UUID> getOnlyNewImages(
            Collection<ImagesForPlace> imagesForPlaceExists, Set<UUID> remoteImagesUUIDSForPlace) {
        Set<UUID> result = new HashSet<>();
        Map<String, Images> existImages = new HashMap<>();
        for (ImagesForPlace ifp : imagesForPlaceExists) {
            existImages.put(ifp.getIdImage().getGuid().toString(), ifp.getIdImage());
        }

        for (UUID uuid : remoteImagesUUIDSForPlace) {
            Images imageWithSameRemoteId = existImages.get(uuid.toString());
            if (imageWithSameRemoteId == null) {
                result.add(uuid);
            } else {
                //такое изображение уже есть в базе
            }
        }
        return result;
    }

    @Nullable
    private Countries findCountryByName(String countryName) {
        TypedQuery<Countries> query = entityManager.createNamedQuery(
                "Countries.findByCountry", Countries.class);
        query.setParameter(COUNTRY, countryName);
        List<Countries> countries = query.getResultList();
        if (!countries.isEmpty()) {
            return countries.get(0);
        } else {
            return null;
        }
    }

    private Things findThing(String guid) {
        logger.debug("Выполняю поиск объекта энциклопедии по guid: " + guid);
        entityManager.flush();
        entityManager.clear();
        TypedQuery<Things> query = entityManager.createQuery("SELECT p FROM "
                + "Things p WHERE p.guid= :guidp", Things.class);
        query.setParameter("guidp", UUID.fromString(guid));
        List<Things> foundThings = query.getResultList();
        Things thing = null;
        if (!foundThings.isEmpty()) {
            thing = foundThings.get(0);
            logger.debug("Объект уже существует");
        } else {
            logger.debug("Такого объекта не найдено");
        }
        return thing;
    }

    @NotNull
    private List<ImagesForThing> createNewImagesForThing(Things thing, Set<UUID> remoteImagesUUIDSForThing) {
        List<ImagesForThing> imagesForThings = new ArrayList<>(remoteImagesUUIDSForThing.size());
        for (UUID uuid : remoteImagesUUIDSForThing) {
            Images images = new Images();
            images.setGuid(uuid);
            images.setImageData(new byte[0]);

            ImagesForThing imagesForThing = new ImagesForThing();
            imagesForThing.setIdThing(thing);
            imagesForThing.setIdImage(images);
            imagesForThings.add(imagesForThing);
        }
        return imagesForThings;
    }

    private Set<UUID> getOnlyNewImagesForThing(Collection<ImagesForThing> 
            imagesForThings, Set<UUID> remoteImagesUUIDSForThing) {
        Set<UUID> result = new HashSet<>();
        Map<String, Images> existImages = new HashMap<>();
        for (ImagesForThing ift : imagesForThings) {
            existImages.put(ift.getIdImage().getGuid().toString(), ift.getIdImage());
        }

        for (UUID uuid : remoteImagesUUIDSForThing) {
            Images imageWithSameRemoteId = existImages.get(uuid.toString());
            if (imageWithSameRemoteId == null) {
                result.add(uuid);
            } else {
                //такое изображение уже есть в базе
            }
        }
        return result;
    }

}
