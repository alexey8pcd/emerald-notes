package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    private class ButtonTakePhotoOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(APP_NAME, "Пользователь выбрал сохранение снимка");
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
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
            if (cbSaveCoordinates.isChecked()) {
                etLongitude.setVisibility(View.VISIBLE);
                etLatitude.setVisibility(View.VISIBLE);
            } else {
                etLongitude.setVisibility(View.INVISIBLE);
                etLatitude.setVisibility(View.INVISIBLE);
            }
        }
    }

    private EditText etDescription;
    private EditText etAddress;
    private EditText etLatitude;
    private EditText etLongitude;
    private ImageView ivImagePreview;
    private CheckBox cbSaveAddress;
    private CheckBox cbSaveCoordinates;
    private String selectedImageURI;
    private DbHelper dbHelper;
    private int placeTypeId;
    private byte[] imageBytes;
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
        if (cbSaveCoordinates.isChecked()) {
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
                    etLatitude.setText(String.valueOf(latitude));
                    double longitude = locationNetwork.getLongitude();
                    etLongitude.setText(String.valueOf(longitude));
                } else {
                    Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (locationGps != null) {
                        double latitude = locationGps.getLatitude();
                        etLatitude.setText(String.valueOf(latitude));
                        double longitude = locationGps.getLongitude();
                        etLongitude.setText(String.valueOf(longitude));
                    }
                }
            } catch (Exception ex) {
                Log.e(APP_NAME, ex.toString(), ex);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent intent = getIntent();
        placeTypeId = intent.getIntExtra(Commons.PLACE_TYPE_ID, 0);
        etDescription = (EditText) findViewById(R.id.aAddPlace_etDescription);
        dbHelper = new DbHelper(getApplicationContext());
        etAddress = (EditText) findViewById(R.id.aAddPlace_etAddress);
        etLatitude = (EditText) findViewById(R.id.aAddPlace_etLatitude);
        etLongitude = (EditText) findViewById(R.id.aAddPlace_etLongitude);
        cbSaveCoordinates = (CheckBox) findViewById(R.id.aAddPlace_cbSaveCoordinates);
        cbSaveCoordinates.setOnClickListener(new CheckBoxSaveCoordinatesOnClickListener());
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
    }

    private void savePlace() {
        try {
            String description = etDescription.getText().toString();
            String address = cbSaveAddress.isChecked() ? etAddress.getText().toString() : null;

            if (Commons.isNotEmpty(description)) {
                final Place place = new Place();
                place.setDescription(description);
                place.setAddress(address);
                place.setDateCreate(new Date());
                place.setIdPlaceType(placeTypeId);
                if (cbSaveCoordinates.isChecked()) {
                    if (etLatitude.getText() != null && etLongitude.getText() != null
                            && Commons.isNotEmpty(etLatitude.getText().toString())
                            && Commons.isNotEmpty(etLongitude.getText().toString())) {

                        BigDecimal latitude = new BigDecimal(etLatitude.getText().toString());
                        BigDecimal longitude = new BigDecimal(etLongitude.getText().toString());
                        place.setLatitude(latitude);
                        place.setLongitude(longitude);
                    }
                }
                if (Commons.isNotEmpty(selectedImageURI) || imageBytes != null) {
                    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                long idImage = dbHelper.addImage(imageBytes, selectedImageURI);
                                if (idImage != -1) {
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
                }
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
                    InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(imageUrl);
                    ivImagePreview.setImageURI(imageUrl);
                    ivImagePreview.invalidate();
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }

            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                Log.d(APP_NAME, "Сделан снимок с камеры: " + bitmap);
                if (bitmap != null) {
                    ivImagePreview.setImageBitmap(bitmap);
                    try {
                        imageBytes = Commons.bitmapToBytesPng(bitmap);
                    } catch (IOException e) {
                        Log.e(APP_NAME, e.toString(), e);
                    }
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
