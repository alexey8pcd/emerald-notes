package ru.alexey_ovcharov.greenguide.mobile.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.network.DataPackage;
import ru.alexey_ovcharov.greenguide.mobile.network.HttpClient;
import ru.alexey_ovcharov.greenguide.mobile.network.InteractStatus;
import ru.alexey_ovcharov.greenguide.mobile.persist.CategoryOfThing;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.ImagesGallery;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;
import ru.alexey_ovcharov.greenguide.mobile.persist.Thing;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PublicationService extends Service {

    public static final String SEND_COMMAND = "/send";
    public static final String IMAGE_GUID = "image-guid";
    public static final String TYPE = "type";
    public static final String TYPE_THINGS = "things";
    public static final String TYPE_PLACES = "places";
    private DbHelper dbHelper;

    public PublicationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = DbHelper.getInstance(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String type = intent.getStringExtra(TYPE);
        publicDataAsync(type);
        return super.onStartCommand(intent, flags, startId);
    }

    private void publicDataAsync(final String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverUrl = null;
                try {
                    serverUrl = dbHelper.getSettingByName(Commons.SERVER_URL);
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }
                if (serverUrl != null) {
                    switch (type) {
                        case TYPE_PLACES:
                            publicPlaces(serverUrl);
                            break;
                        case TYPE_THINGS:
                            publicThings(serverUrl);
                            break;

                    }
                }

            }
        }).start();
    }

    private void publicThings(String serverUrl) {
        Log.d(APP_NAME, "Выполняю публикацию объектов энциклопедии");
        HttpClient httpClientThings = null;
        try {
            String thingsUrlParts = Commons.URL_REFERENCES + SEND_COMMAND + Commons.THINGS_DATA_URL_PART;
            httpClientThings = new HttpClient(serverUrl + thingsUrlParts);
            ContentResolver contentResolver = getContentResolver();
            List<CategoryOfThing> thingTypes = dbHelper.getAllCategoryOfThingsSorted();
            List<Thing> things = getAllThings(thingTypes);
            Set<Image> imagesData = createImagesData(things);
            String textRequest = prepareThingsTextRequest(thingTypes, things);
            InteractStatus networkStatus = InteractStatus.CLIENT_ERROR;
            if (textRequest != null) {
                networkStatus = httpClientThings.sendJSON(textRequest);
                if (networkStatus == InteractStatus.SUCCESS) {
                    networkStatus = sendImages(serverUrl, contentResolver, imagesData);
                }
            }
            NotificationsHelper.sendPublicNotify(networkStatus,
                    getApplicationContext(), "Результат публикации справочников");
        } catch (Exception ex) {
            Log.e(APP_NAME, ex.toString(), ex);
        } finally {
            IOUtils.closeQuietly(httpClientThings);
            stopSelf();
        }
    }

    @NonNull
    private InteractStatus sendImages(String serverUrl, ContentResolver contentResolver,
                                      Set<Image> imagesData) throws IOException {
        String imagesUrlParts = Commons.URL_REFERENCES + SEND_COMMAND + Commons.IMAGES_PART;
        InteractStatus networkStatus = InteractStatus.UNKNOWN;
        try (HttpClient httpClientImages = new HttpClient(serverUrl + imagesUrlParts)) {

            for (Image image : imagesData) {
                Uri imageUri = Uri.parse(image.getUrl());
                DataPackage dataPackage;
                try (InputStream inputStream = contentResolver.openInputStream(imageUri)) {
                    dataPackage = new DataPackage(inputStream);
                }
                dataPackage.addHeader(IMAGE_GUID, image.getGuid());
                networkStatus = httpClientImages.sendBinaryData(dataPackage);
                int count = 0;
                while (networkStatus == InteractStatus.CORRUPT_DATA && count++ < 3) {
                    networkStatus = httpClientImages.sendBinaryData(dataPackage);
                }
                if (networkStatus != InteractStatus.SUCCESS) {
                    break;
                }
            }
        }
        return networkStatus;
    }

    private String prepareThingsTextRequest(List<CategoryOfThing> thingTypes, List<Thing> things) {
        try {
            JSONObject requestJSON = new JSONObject();
            JSONArray thingsCategoriesJsonArray = createCategoriesOfThings(thingTypes);
            JSONArray thingsJsonArray = createThings(things);
            requestJSON.put(Thing.TABLE_NAME, thingsJsonArray);
            requestJSON.put(CategoryOfThing.TABLE_NAME, thingsCategoriesJsonArray);
            requestJSON.put(DbHelper.DATABASE_ID, dbHelper.getSettingByName(DbHelper.DATABASE_ID));
            return requestJSON.toString();
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
        return null;
    }

    @NonNull
    private JSONArray createThings(List<Thing> things) throws PersistenceException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Map<Integer, String> countries = dbHelper.getCountriesSorted();
        for (Thing thing : things) {
            JSONObject jsonObject = thing.toJsonObject(countries);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    @NonNull
    private List<Thing> getAllThings(List<CategoryOfThing> thingTypes) throws PersistenceException {
        List<Thing> thingsRes = new ArrayList<>();
        for (CategoryOfThing categoryOfThing : thingTypes) {
            List<Thing> things = dbHelper.getThingsByCategory(categoryOfThing.getIdCategory());
            for (Thing thing : things) {
                thingsRes.add(thing);
            }
        }
        return thingsRes;
    }

    private void publicPlaces(String serverUrl) {
        Log.d(APP_NAME, "Выполняю публикацию мест");
        HttpClient httpClientPlaces = null;
        try {
            String placesUrlParts = Commons.URL_REFERENCES + SEND_COMMAND + Commons.PLACES_DATA_URL_PART;
            httpClientPlaces = new HttpClient(serverUrl + placesUrlParts);
            ContentResolver contentResolver = getContentResolver();
            List<PlaceType> placesTypes = dbHelper.getPlacesTypesSorted();
            List<Place> places = getAllPlaces(placesTypes);
            Set<Image> imagesData = createImagesData(places);
            String textRequest = preparePlacesTextRequest(placesTypes, places);
            InteractStatus networkStatus = InteractStatus.CLIENT_ERROR;
            if (textRequest != null) {
                networkStatus = httpClientPlaces.sendJSON(textRequest);
                if (networkStatus == InteractStatus.SUCCESS) {
                    networkStatus = sendImages(serverUrl, contentResolver, imagesData);
                }
            }
            NotificationsHelper.sendPublicNotify(networkStatus,
                    getApplicationContext(), "Результат публикации справочников");
        } catch (Exception ex) {
            Log.e(APP_NAME, ex.toString(), ex);
        } finally {
            IOUtils.closeQuietly(httpClientPlaces);
            stopSelf();
        }
    }


    @Nullable
    private String preparePlacesTextRequest(List<PlaceType> placesTypes, List<Place> places) {
        try {
            JSONObject requestJSON = new JSONObject();
            JSONArray placeTypesJsonArray = createPlaceTypes(placesTypes);
            JSONArray placesJsonArray = createPlaces(places);
            requestJSON.put(Place.TABLE_NAME, placesJsonArray);
            requestJSON.put(PlaceType.TABLE_NAME, placeTypesJsonArray);
            requestJSON.put(DbHelper.DATABASE_ID, dbHelper.getSettingByName(DbHelper.DATABASE_ID));
            return requestJSON.toString();
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
        return null;
    }

    @NonNull
    private Set<Image> createImagesData(List<? extends ImagesGallery> imagesGalleries)
            throws PersistenceException, IOException, JSONException {
        Set<Image> images = new HashSet<>();
        for (ImagesGallery imagesGallery : imagesGalleries) {
            List<Image> imagesIdsInt = imagesGallery.getImagesInfo();
            images.addAll(imagesIdsInt);
        }
        return images;
    }

    @NonNull
    private JSONArray createPlaces(List<Place> places) throws PersistenceException,
            FileNotFoundException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Map<Integer, String> countries = dbHelper.getCountriesSorted();
        for (Place place : places) {
            JSONObject jsonObject = place.toJsonObject(countries);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    @NonNull
    private List<Place> getAllPlaces(List<PlaceType> placesTypes) throws PersistenceException {

        List<Place> allPlaces = new ArrayList<>();
        for (PlaceType placeType : placesTypes) {
            List<Place> places = dbHelper.getPlacesByType(placeType.getIdPlaceType(), true);
            for (Place place : places) {
                allPlaces.add(place);
            }
        }
        return allPlaces;
    }

    @NonNull
    private JSONArray createPlaceTypes(List<PlaceType> placesTypes) throws JSONException {

        JSONArray jsonArray = new JSONArray();
        for (PlaceType placeType : placesTypes) {
            JSONObject placeTypeObject = placeType.toJSONObject();
            jsonArray.put(placeTypeObject);
        }
        return jsonArray;
    }

    @NonNull
    private JSONArray createCategoriesOfThings(List<CategoryOfThing> categoryOfThings) throws JSONException {

        JSONArray jsonArray = new JSONArray();
        for (CategoryOfThing categoryOfThing : categoryOfThings) {
            JSONObject categoryOfThingsObject = categoryOfThing.toJSONObject();
            jsonArray.put(categoryOfThingsObject);
        }
        return jsonArray;
    }


}
