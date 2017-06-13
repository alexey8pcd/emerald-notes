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
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PublicationService extends Service {

    public static final String SEND_COMMAND = "/send";
    public static final String IMAGE_GUID = "image-guid";
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
        publicDataAsync();
        return super.onStartCommand(intent, flags, startId);
    }

    private void publicDataAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClientPlaces = null;
                try {
                    String serverUrl = dbHelper.getSettingByName(Commons.SERVER_URL);

                    String placesUrlParts = Commons.URL_REFERENCES + SEND_COMMAND + Commons.PLACES_DATA_URL_PART;
                    httpClientPlaces = new HttpClient(serverUrl + placesUrlParts);

                    ContentResolver contentResolver = getContentResolver();

                    Log.d(APP_NAME, "Начинаю отправку данных на сервер");
                    List<PlaceType> placesTypes = dbHelper.getPlacesTypesSorted();
                    List<Place> places = getAllPlaces(placesTypes);
                    Set<Image> imagesData = createImagesData(places);

                    String textRequest = prepareTextRequest(placesTypes, places);
                    InteractStatus networkStatus = InteractStatus.CLIENT_ERROR;
                    if (textRequest != null) {
                        networkStatus = httpClientPlaces.sendJSON(textRequest);
                        if (networkStatus == InteractStatus.SUCCESS) {
                            String imagesUrlParts = Commons.URL_REFERENCES + SEND_COMMAND + Commons.IMAGES_PART;
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
        }).start();
    }


    @Nullable
    private String prepareTextRequest(List<PlaceType> placesTypes, List<Place> places) {
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
    private Set<Image> createImagesData(List<Place> places) throws PersistenceException, IOException, JSONException {
        Set<Image> images = new HashSet<>();
        for (Place place : places) {
            List<Image> imagesIdsInt = place.getImages();
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
            JSONObject jsonObject = place.toJsonObject(countries, getApplicationContext());
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


}
