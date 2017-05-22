package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class AddPlaceActivity extends Activity {

    public static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int GET_COORDINATES_FROM_MAP_REQUEST = 3;

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
            if (cbSaveAddress.isChecked()) {
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

    private Uri tempImageUri;
    private Button bChooseAddressOnMap;
    private EditText etDescription;
    private EditText etAddress;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private ImageView ivImagePreview;
    private CheckBox cbSaveAddress;
    private CheckBox cbUseCurrentCoordinates;
    private String selectedImageURI;
    private DbHelper dbHelper;
    private int placeTypeId;
    private boolean gpsCurrentCoordinatesReceived;
    private boolean onMapCoordinatesReceived;
    private LocationManager locationManager;
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
        if (cbUseCurrentCoordinates.isChecked()) {
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
                    tvLatitude.setText(String.valueOf(latitude));
                    double longitude = locationNetwork.getLongitude();
                    tvLongitude.setText(String.valueOf(longitude));
                    gpsCurrentCoordinatesReceived = true;
                } else {
                    Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (locationGps != null) {
                        double latitude = locationGps.getLatitude();
                        tvLatitude.setText(String.valueOf(latitude));
                        double longitude = locationGps.getLongitude();
                        tvLongitude.setText(String.valueOf(longitude));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent currentIntent = getIntent();
        placeTypeId = currentIntent.getIntExtra(Commons.PLACE_TYPE_ID, 0);
        etDescription = (EditText) findViewById(R.id.aAddPlace_etDescription);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        etAddress = (EditText) findViewById(R.id.aAddPlace_etAddress);
        tvLatitude = (TextView) findViewById(R.id.aAddPlace_tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.aAddPlace_tvLongitude);
        cbUseCurrentCoordinates = (CheckBox) findViewById(R.id.aAddPlace_cbSaveCoordinates);
        cbUseCurrentCoordinates.setOnClickListener(new CheckBoxSaveCoordinatesOnClickListener());
        ivImagePreview = (ImageView) findViewById(R.id.aAddPlace_ivPreviewImage);
        cbSaveAddress = (CheckBox) findViewById(R.id.aAddPlace_cbSaveAddress);
        cbSaveAddress.setOnClickListener(new CheckBoxSaveAddressOnClickListener());
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
    }

    private void savePlace() {
        try {
            String description = etDescription.getText().toString();
            if (Commons.isNotEmpty(description)) {
                String address = cbSaveAddress.isChecked() ? etAddress.getText().toString() : null;
                final Place place = new Place();
                place.setDescription(description);
                place.setAddress(address);
                place.setDateCreate(new Date());
                place.setIdPlaceType(placeTypeId);
                if (gpsCurrentCoordinatesReceived || onMapCoordinatesReceived) {
                    if (tvLatitude.getText() != null && tvLongitude.getText() != null
                            && Commons.isNotEmpty(tvLatitude.getText().toString())
                            && Commons.isNotEmpty(tvLongitude.getText().toString())) {

                        BigDecimal latitude = new BigDecimal(tvLatitude.getText().toString());
                        BigDecimal longitude = new BigDecimal(tvLongitude.getText().toString());
                        place.setLatitude(latitude);
                        place.setLongitude(longitude);
                    }
                }
                if (Commons.isNotEmpty(selectedImageURI)) {
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
                double latitude = data.getDoubleExtra(Place.LATITUDE_COLUMN, Double.NaN);
                double longitude = data.getDoubleExtra(Place.LONGITUDE_COLUMN, Double.NaN);
                Log.d(APP_NAME, String.format("Выбраны координаты на карте: Lt: %f, Ln: %f", latitude, longitude));
                if (!(Double.isNaN(latitude) || Double.isNaN(longitude))) {
                    onMapCoordinatesReceived = true;
                    tvLatitude.setText(String.valueOf(latitude));
                    tvLongitude.setText(String.valueOf(longitude));
                    tvLatitude.setVisibility(View.VISIBLE);
                    tvLongitude.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 5, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 5, 10,
                locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }
}
