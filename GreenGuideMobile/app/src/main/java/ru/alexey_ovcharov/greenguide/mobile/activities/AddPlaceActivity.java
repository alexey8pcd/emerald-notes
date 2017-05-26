package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class AddPlaceActivity extends Activity {

    public static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int GET_COORDINATES_FROM_MAP_REQUEST = 3;
    public static final int PLACE_TYPE_ID_UNDEFINED = 0;
    private Uri tempImageUri;
    private Button bChooseAddressOnMap;
    private EditText etDescription;
    private EditText etAddress;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private ImageView ivImagePreview;
    private CheckBox cbSaveAddressManual;
    private CheckBox cbUseCurrentCoordinates;
    private String selectedImageURI;
    private DbHelper dbHelper;
    private int placeTypeId;
    private boolean gpsCurrentCoordinatesReceived;
    private boolean onMapCoordinatesReceived;
    private boolean allowGetSelfCoordinatesFromGpsOrNetwork;
    private LocationManager locationManager;
    private Spinner spCategory;
    private List<PlaceType> placesTypes;
    private List<String> categories = new CopyOnWriteArrayList<>();
    private volatile double latitude;
    private volatile double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        tvLatitude = (TextView) findViewById(R.id.aAddPlace_tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.aAddPlace_tvLongitude);
        cbUseCurrentCoordinates = (CheckBox) findViewById(R.id.aAddPlace_cbSaveCoordinates);
        cbUseCurrentCoordinates.setOnClickListener(new CheckBoxSaveCoordinatesOnClickListener());
        etDescription = (EditText) findViewById(R.id.aAddPlace_etDescription);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        etAddress = (EditText) findViewById(R.id.aAddPlace_etAddress);
        ivImagePreview = (ImageView) findViewById(R.id.aAddPlace_ivPreviewImage);
        cbSaveAddressManual = (CheckBox) findViewById(R.id.aAddPlace_cbSaveAddress);
        cbSaveAddressManual.setOnClickListener(new CheckBoxSaveAddressOnClickListener());
        spCategory = (Spinner) findViewById(R.id.aAddPlace_spCategory);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        Intent currentIntent = getIntent();
        placeTypeId = currentIntent.getIntExtra(PlaceType.ID_PLACE_TYPE_COLUMN, PLACE_TYPE_ID_UNDEFINED);
        latitude = currentIntent.getDoubleExtra(Place.LATITUDE_COLUMN, Double.NaN);
        longitude = currentIntent.getDoubleExtra(Place.LONGITUDE_COLUMN, Double.NaN);
        if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
            cbSaveAddressManual.setChecked(false);
            cbUseCurrentCoordinates.setChecked(false);
            cbUseCurrentCoordinates.setVisibility(View.INVISIBLE);
            onMapCoordinatesReceived = true;
            tvLatitude.setText(String.valueOf(latitude));
            tvLongitude.setText(String.valueOf(longitude));
            allowGetSelfCoordinatesFromGpsOrNetwork = false;
            getAddressAsync();
        } else {
            allowGetSelfCoordinatesFromGpsOrNetwork = true;
        }
        Button bSave = (Button) findViewById(R.id.aAddPlace_bSave);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlace();
            }
        });

        Button bChoosePhoto = (Button) findViewById(R.id.aAddPlace_bChoosePhoto);
        bChoosePhoto.setOnClickListener(new ButtonChoosePhotoOnClickListener());

        Button bTakePhoto = (Button) findViewById(R.id.aAddPlace_bPhotoFromCamera);
        bTakePhoto.setOnClickListener(new ButtonTakePhotoOnClickListener());

        bChooseAddressOnMap = (Button) findViewById(R.id.aAddPlace_bChooseAddressOnMap);
        bChooseAddressOnMap.setEnabled(false);
        bChooseAddressOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddPlaceActivity.this, PlacesMapActivity.class);
                intent.putExtra(Commons.MAP_OPEN_TYPE, Commons.OPEN_TYPE_CHOOSE_LOCATION);
                startActivityForResult(intent, GET_COORDINATES_FROM_MAP_REQUEST);
            }
        });

        getCategoriesAsync();
    }

    private void getAddressAsync() {
        new AsyncTask<Void, Void, Void>() {

            private String addressComplete;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Geocoder geocoder = new Geocoder(AddPlaceActivity.this, Locale.getDefault());
                    if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        Address addressRec = addresses.get(0);
                        String address = addressRec.getAddressLine(0);
                        String city = addressRec.getLocality();
                        String state = addressRec.getAdminArea();
                        String country = addressRec.getCountryName();
                        String knownName = addressRec.getFeatureName();
                        addressComplete = country + ", " + state + ", " + city
                                + ", " + address + ", (" + knownName + ")";
                    }
                } catch (Exception ex) {
                    Log.e(APP_NAME, ex.toString(), ex);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                etAddress.setVisibility(View.VISIBLE);
                etAddress.setText(addressComplete);
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void getCategoriesAsync() {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    placesTypes = dbHelper.getPlacesTypesSorted();
                    if (placeTypeId != PLACE_TYPE_ID_UNDEFINED) {
                        Iterator<PlaceType> iterator = placesTypes.iterator();
                        while (iterator.hasNext()) {
                            PlaceType placeType = iterator.next();
                            if (placeType.getIdPlaceType() != placeTypeId) {
                                iterator.remove();
                            }
                        }
                    }
                    List<String> placeTypesNames = Commons.listToStringArray(placesTypes, new Mapper<PlaceType>() {
                        @Override
                        public String map(PlaceType item) {
                            return item.getType();
                        }
                    });
                    categories.clear();
                    categories.addAll(placeTypesNames);
                } catch (Exception ex) {
                    Log.e(APP_NAME, ex.toString(), ex);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    ((BaseAdapter) spCategory.getAdapter()).notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void savePlace() {
        try {
            String description = etDescription.getText().toString();
            if (StringUtils.isNotEmpty(description)) {
                String addressString = etAddress.getText().toString();
                final Place place = new Place();
                place.setDescription(description);
                if (StringUtils.isNotEmpty(addressString)) {
                    place.setAddress(addressString);
                }
                place.setDateCreate(new Date());
                if (placeTypeId == PLACE_TYPE_ID_UNDEFINED) {
                    placeTypeId = placesTypes.get(spCategory.getSelectedItemPosition()).getIdPlaceType();
                }
                place.setIdPlaceType(placeTypeId);
                if (gpsCurrentCoordinatesReceived || onMapCoordinatesReceived) {
                    if (tvLatitude.getText() != null && tvLongitude.getText() != null
                            && StringUtils.isNotEmpty(tvLatitude.getText().toString())
                            && StringUtils.isNotEmpty(tvLongitude.getText().toString())) {

                        BigDecimal latitude = new BigDecimal(tvLatitude.getText().toString());
                        BigDecimal longitude = new BigDecimal(tvLongitude.getText().toString());
                        place.setLatitude(latitude);
                        place.setLongitude(longitude);
                    }
                }
                if (StringUtils.isNotEmpty(selectedImageURI)) {
                    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                long idImage = dbHelper.addImage(selectedImageURI);
                                if (idImage != DbHelper.ROW_NOT_INSERTED) {
                                    place.addImageId((int) idImage);
                                    dbHelper.addPlace(place);
                                }
                            } catch (PersistenceException ex) {
                                ex.log();
                            }
                            return null;
                        }
                    }.execute();
                    finish();
                } else {
                    Toast.makeText(this, "Изображение не выбрано", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Описание не указано", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUrl = data.getData();
                Log.d(APP_NAME, "Выбрано изображение на устройстве: " + imageUrl);
                selectedImageURI = imageUrl.toString();
                try {
                    ivImagePreview.setImageURI(imageUrl);
                    ivImagePreview.invalidate();
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }

            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (tempImageUri != null) {
                selectedImageURI = tempImageUri.toString();
                Log.d(APP_NAME, "Сделан снимок с камеры: " + selectedImageURI);
                ivImagePreview.setImageURI(tempImageUri);
                ivImagePreview.invalidate();
            }
        } else if (requestCode == GET_COORDINATES_FROM_MAP_REQUEST && resultCode == Activity.RESULT_OK) {
            onMapCoordinatesReceived = false;
            if (data != null) {
                latitude = data.getDoubleExtra(Place.LATITUDE_COLUMN, Double.NaN);
                longitude = data.getDoubleExtra(Place.LONGITUDE_COLUMN, Double.NaN);
                Log.d(APP_NAME, String.format("Выбраны координаты на карте: Lt: %f, Ln: %f", latitude, longitude));
                if (!(Double.isNaN(latitude) || Double.isNaN(longitude))) {
                    onMapCoordinatesReceived = true;
                    tvLatitude.setText(String.valueOf(latitude));
                    tvLongitude.setText(String.valueOf(longitude));
                    tvLatitude.setVisibility(View.VISIBLE);
                    tvLongitude.setVisibility(View.VISIBLE);
                    getAddressAsync();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!allowGetSelfCoordinatesFromGpsOrNetwork ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000 * 5, 10, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000 * 5, 10,
                    locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private class ButtonTakePhotoOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(APP_NAME, "Пользователь выбрал сохранение снимка");
            File photo = Commons.getTempPhotoFile();
            if (photo != null) {
                tempImageUri = Uri.fromFile(photo);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(AddPlaceActivity.this, "Не удалось создать изображения, возможно " +
                                "нет доступа к карте памяти!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ButtonChoosePhotoOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            } else {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        }
    }

    private class CheckBoxSaveAddressOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (cbSaveAddressManual.isChecked()) {
                etAddress.setVisibility(View.VISIBLE);
            } else {
                etAddress.setVisibility(View.INVISIBLE);
            }
        }

    }

    private class CheckBoxSaveCoordinatesOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (cbUseCurrentCoordinates.isChecked()) {
                tvLongitude.setVisibility(View.VISIBLE);
                tvLatitude.setVisibility(View.VISIBLE);
                bChooseAddressOnMap.setEnabled(false);
            } else {
                tvLongitude.setVisibility(View.INVISIBLE);
                tvLatitude.setVisibility(View.INVISIBLE);
                bChooseAddressOnMap.setEnabled(true);
            }
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status == LocationProvider.AVAILABLE) {
                updateLocation();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            updateLocation();
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void updateLocation() {
        if (allowGetSelfCoordinatesFromGpsOrNetwork && cbUseCurrentCoordinates.isChecked()) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (locationNetwork != null) {
                    double latitude = locationNetwork.getLatitude();
                    double longitude = locationNetwork.getLongitude();

                    tvLatitude.setText(String.valueOf(latitude));
                    tvLongitude.setText(String.valueOf(longitude));
                    saveCoordinatesAsync(latitude, longitude);
                    gpsCurrentCoordinatesReceived = true;
                } else {
                    Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (locationGps != null) {
                        double latitude = locationGps.getLatitude();
                        double longitude = locationGps.getLongitude();

                        tvLatitude.setText(String.valueOf(latitude));
                        tvLongitude.setText(String.valueOf(longitude));
                        saveCoordinatesAsync(latitude, longitude);
                        gpsCurrentCoordinatesReceived = true;
                    }
                }
            } catch (Exception ex) {
                gpsCurrentCoordinatesReceived = false;
                Log.e(APP_NAME, ex.toString(), ex);
            }
        } else {
            gpsCurrentCoordinatesReceived = false;
        }
    }

    private void saveCoordinatesAsync(final double latitude, final double longitude) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    dbHelper.putSetting(Place.LATITUDE_COLUMN, String.valueOf(latitude));
                    dbHelper.putSetting(Place.LONGITUDE_COLUMN, String.valueOf(longitude));
                } catch (Exception ex) {
                    Log.w(APP_NAME, "Не могу сохранить координаты: " + ex, ex);
                }
                return null;
            }
        }.execute();
    }
}
