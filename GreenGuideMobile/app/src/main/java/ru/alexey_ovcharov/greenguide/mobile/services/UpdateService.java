package ru.alexey_ovcharov.greenguide.mobile.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.network.HttpClient;
import ru.alexey_ovcharov.greenguide.mobile.network.InteractStatus;
import ru.alexey_ovcharov.greenguide.mobile.network.Response;
import ru.alexey_ovcharov.greenguide.mobile.persist.Country;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class UpdateService extends Service {

    private static final String GET_COMMAND = "/get";
    private DbHelper dbHelper;
    private static final Semaphore SEMAPHORE = new Semaphore(1);

    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = DbHelper.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateDataAsync();
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateDataAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        SEMAPHORE.acquire();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Log.d(APP_NAME, "Выполняю получение информации о местах с удаленного сервера");
                    String serviceUrl = dbHelper.getSettingByName(Commons.SERVER_URL);
                    String urlGetPlaces = serviceUrl + Commons.URL_REFERENCES + GET_COMMAND + Commons.PLACES_DATA_URL_PART;
                    HttpClient httpClientPlaces = new HttpClient(urlGetPlaces);
                    Response response = httpClientPlaces.receiveData();
                    InteractStatus networkStatus = response.getNetworkStatus();
                    if (networkStatus == InteractStatus.SUCCESS) {
                        String urlGetImages = serviceUrl + Commons.URL_REFERENCES
                                + GET_COMMAND + Commons.IMAGES_PART;
                        try (HttpClient imagesHttpClient = new HttpClient(urlGetImages)) {
                            Map<Place, List<String>> placeListMap = loadPlaces(response.getData());
                            for (Map.Entry<Place, List<String>> entry : placeListMap.entrySet()) {
                                Place place = entry.getKey();
                                List<String> externalImagesGuids = entry.getValue();
                                Set<Image> images = dbHelper.getImagesByGuids(externalImagesGuids);
                                Set<Image> newIdImages = Collections.EMPTY_SET;
                                if (images.size() < externalImagesGuids.size()) {
                                    Set<String> imageGuidsToDownload
                                            = getImagesGuidToDownLoad(externalImagesGuids, images);
                                    Map<String, byte[]> downloadImagesForPlace
                                            = downloadImages(imageGuidsToDownload, imagesHttpClient);
                                    Map<String, String> guidesAndUrls =
                                            Commons.saveImagesOnSdCard(downloadImagesForPlace);
                                    newIdImages = dbHelper.createImages(guidesAndUrls);
                                    images.addAll(newIdImages);
                                }
                                int idPlace = dbHelper.getIdPlaceByGuid(place.getGuid());
                                if (idPlace != DbHelper.ROW_NOT_EXIST) {
                                    place.setIdPlace(idPlace);
                                    place.addImages(newIdImages);
                                    dbHelper.updatePlace(place);
                                } else {
                                    place.addImages(images);
                                    dbHelper.addPlace(place);
                                }
                            }
                            NotificationsHelper.sendUpdateNotify(InteractStatus.SUCCESS,
                                    getApplicationContext(), "Результат обновления");
                        } catch (Exception ex) {
                            Log.e(APP_NAME, ex.toString(), ex);
                            NotificationsHelper.sendUpdateNotify(InteractStatus.DB_ERROR,
                                    getApplicationContext(), "Результат обновления");
                        }
                    } else {
                        NotificationsHelper.sendUpdateNotify(networkStatus,
                                getApplicationContext(), "Результат обновления");
                    }
                } catch (Exception ex) {
                    Log.e(APP_NAME, ex.toString(), ex);
                } finally {
                    SEMAPHORE.release();
                    stopSelf();
                }
            }
        }).start();
    }


    @NonNull
    private Map<String, byte[]> downloadImages(
            Collection<String> guidsImagesToDownload, HttpClient httpClient)
            throws IOException, JSONException {
        Pair<String, String>[] request = new Pair[guidsImagesToDownload.size()];
        int index = 0;
        for (String guid : guidsImagesToDownload) {
            request[index++] = new ImmutablePair<>("guid", guid);
        }
        Response response = httpClient.receiveData(request);
        if (response.getNetworkStatus() == InteractStatus.SUCCESS) {
            String data = response.getData();
            JSONArray imagesArray = new JSONArray(data);
            Map<String, byte[]> map = new HashMap<>();
            for (int i = 0; i < imagesArray.length(); i++) {
                JSONObject imageData = imagesArray.getJSONObject(i);
                String guid = imageData.getString("guid");
                String binaryData = imageData.getString("binary_data");
                byte[] bytes = Base64.decode(binaryData, Base64.DEFAULT);
                map.put(guid, bytes);
            }
            return map;
        } else {
            throw new IOException(response.getNetworkStatus().toString());
        }
    }

    @NonNull
    private Set<String> getImagesGuidToDownLoad(Collection<String> externalImagesGuids,
                                                Collection<Image> images) {
        Set<String> missingImagesGuids = new HashSet<>();
        Set<String> existingImagesGuids = new HashSet<>(images.size());
        for (Image image : images) {
            existingImagesGuids.add(image.getGuid());
        }
        for (String externalImageGuid : externalImagesGuids) {
            if (!existingImagesGuids.contains(externalImageGuid)) {
                missingImagesGuids.add(externalImageGuid);
            }
        }
        return missingImagesGuids;
    }

    @NonNull
    private Map<Place, List<String>> loadPlaces(String data) throws JSONException,
            ParseException, PersistenceException {
        JSONObject jsonObject = new JSONObject(data);
        JSONArray places = jsonObject.getJSONArray("places");
        List<PlaceType> placeTypes = dbHelper.getPlacesTypesSorted();
        Map<String, Integer> placeTypesMap = getPlaceTypesMap(placeTypes);

        Map<Integer, String> countriesById = dbHelper.getCountriesSorted();
        Map<String, Integer> countriesByName = Commons.invertMap(countriesById);
        Map<Place, List<String>> result = new HashMap<>();
        for (int i = 0; i < places.length(); i++) {
            JSONObject placeJson = places.getJSONObject(i);
            Place place = new Place(placeJson);

            String placeType = placeJson.getString(PlaceType.TYPE_COLUMN);
            Integer idPlaceType = placeTypesMap.get(placeType);
            if (idPlaceType == null && StringUtils.isNotBlank(placeType)) {
                dbHelper.addPlaceType(placeType);
                placeTypes = dbHelper.getPlacesTypesSorted();
                placeTypesMap = getPlaceTypesMap(placeTypes);
                idPlaceType = placeTypesMap.get(placeType);
            }
            place.setIdPlaceType(idPlaceType);

            String country = placeJson.optString(Country.COUNTRY_COLUMN);
            Integer idCountry = countriesByName.get(country);
            if (idCountry == null && StringUtils.isNotBlank(country)) {
                dbHelper.addCountry(country);
                countriesById = dbHelper.getCountriesSorted();
                countriesByName = Commons.invertMap(countriesById);
                idCountry = countriesByName.get(country);
            }
            place.setIdCountry(idCountry);

            JSONArray imagesGuides = placeJson.getJSONArray(Image.TABLE_NAME);
            List<String> imageGuides = new ArrayList<>(imagesGuides.length());
            for (int j = 0; j < imagesGuides.length(); j++) {
                String imageGuid = imagesGuides.getString(j);
                imageGuides.add(imageGuid);
            }
            result.put(place, imageGuides);
        }
        return result;
    }

    @NonNull
    private Map<String, Integer> getPlaceTypesMap(List<PlaceType> placeTypes) {
        Map<String, Integer> placeTypesMap = new HashMap<>();
        for (PlaceType placeType : placeTypes) {
            placeTypesMap.put(placeType.getType(), placeType.getIdPlaceType());
        }
        return placeTypesMap;
    }

}
