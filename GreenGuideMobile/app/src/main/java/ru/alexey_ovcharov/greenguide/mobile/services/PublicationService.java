package ru.alexey_ovcharov.greenguide.mobile.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PublicationService extends Service {

    public static final String SEND_COMMAND = "/send";
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
                try {
                    Log.d(APP_NAME, "Начинаю отправку данных на сервер");
                    String data = prepareTextRequest();
                    InteractStatus networkStatus = InteractStatus.CLIENT_ERROR;
                    if (data != null) {
                        networkStatus = sendData(data);
                    }
                    NotificationsHelper.sendPublicNotify(networkStatus, getApplicationContext(), "Результат публикации справочников");
                } catch (Exception ex) {
                    Log.e(APP_NAME, ex.toString(), ex);
                } finally {
                    stopSelf();
                }

            }
        }).start();
    }



    @Nullable
    private String prepareTextRequest() {
        try {
            JSONObject requestJSON = new JSONObject();
            List<PlaceType> placesTypes = dbHelper.getPlacesTypesSorted();
            JSONArray placeTypesJsonArray = createPlaceTypes(placesTypes);
            List<Place> places = getAllPlaces(placesTypes);
            JSONArray placesJsonArray = createPlaces(places);
            requestJSON.put(Place.TABLE_NAME, placesJsonArray);
            requestJSON.put(PlaceType.TABLE_NAME, placeTypesJsonArray);
            JSONArray imagesData = createImagesData(places);
            requestJSON.put(Image.TABLE_NAME, imagesData);
            requestJSON.put(DbHelper.DATABASE_ID, dbHelper.getSettingByName(DbHelper.DATABASE_ID));
            return requestJSON.toString();
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
        return null;
    }

    @NonNull
    private JSONArray createImagesData(List<Place> places) throws PersistenceException, IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Set<Integer> imageIds = new HashSet<>();
        for (Place place : places) {
            List<Integer> imagesIdsInt = place.getImagesIds();
            imageIds.addAll(imagesIdsInt);
        }
        List<Image> allImages = dbHelper.getImageData(imageIds);
        ContentResolver contentResolver = getContentResolver();
        for (Image image : allImages) {
            JSONObject imageObject = image.toJsonObject(contentResolver);
            jsonArray.put(imageObject);
        }
        return jsonArray;
    }


    @NonNull
    private JSONArray createPlaces(List<Place> places) throws PersistenceException,
            FileNotFoundException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Map<Integer, String> countries = dbHelper.getCountries();
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

    private InteractStatus sendData(String data) {
        try {
            String serviceUrl = dbHelper.getSettingByName(Commons.SERVER_URL);
            URL url = new URL(serviceUrl + Commons.URL_REFERENCES + SEND_COMMAND + Commons.PLACES_DATA_URL_PART);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(250000);
            conn.setConnectTimeout(25000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Android(" + APP_NAME + ")");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setDoOutput(true);
            Log.d(APP_NAME, "Отправляю запрос на сервер: " + data);
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(data.getBytes("utf-8"));
            outputStream.flush();
            int responseCode = conn.getResponseCode();
            Log.d(APP_NAME, "Http код ответа: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return InteractStatus.SUCCESS;
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                return InteractStatus.CLIENT_ERROR;
            } else {
                return InteractStatus.SERVER_ERROR;
            }
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
            return InteractStatus.UNKNOWN;
        }
    }
}
