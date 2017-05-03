package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Intent intent = getIntent();
        placeTypeId = intent.getIntExtra(Commons.PLACE_TYPE_ID, 0);

        etDescription = (EditText) findViewById(R.id.aAddPlace_etDescription);
        dbHelper = new DbHelper(getApplicationContext());
        etAddress = (EditText) findViewById(R.id.aAddPlace_etAddress);
        etLatitude = (EditText) findViewById(R.id.aAddPlace_etLatitude);
        etLongitude = (EditText) findViewById(R.id.aAddPlace_etLongitude);

        cbSaveCoordinates = (CheckBox) findViewById(R.id.aAddPlace_cbSaveCoordinates);
        cbSaveCoordinates.setOnClickListener(new View.OnClickListener() {
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
        });

        ivImagePreview = (ImageView) findViewById(R.id.aAddPlace_ivPreviewImage);

        cbSaveAddress = (CheckBox) findViewById(R.id.aAddPlace_cbSaveAddress);
        cbSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbSaveAddress.isChecked()) {
                    etAddress.setVisibility(View.VISIBLE);
                } else {
                    etAddress.setVisibility(View.INVISIBLE);
                }
            }

        });
        Button bSave = (Button) findViewById(R.id.aAddPlace_bSave);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlace();
            }
        });

        Button bChoosePhoto = (Button) findViewById(R.id.aAddPlace_bChoosePhoto);
        bChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Выбрать изображения"), PICK_IMAGE_REQUEST);
            }
        });

        Button bTakePhoto = (Button) findViewById(R.id.aAddPlace_bPhotoFromCamera);
        bTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(APP_NAME, "Пользователь выбрал сохранение снимка");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    private void savePlace() {
        try {
            String description = etDescription.getText().toString();
            String address = cbSaveAddress.isChecked() ? etAddress.getText().toString() : null;
            BigDecimal latitude = cbSaveCoordinates.isChecked()
                    ? new BigDecimal(etLatitude.getText().toString()) : null;
            BigDecimal longitude = cbSaveCoordinates.isChecked()
                    ? new BigDecimal(etLongitude.getText().toString()) : null;
            if (Commons.isNotEmpty(description)) {
                final Place place = new Place();
                place.setDescription(description);
                place.setAddress(address);
                place.setDateCreate(new Date());
                place.setIdPlaceType(placeTypeId);
                place.setLatitude(latitude);
                place.setLongitude(longitude);
                if (Commons.isNotEmpty(selectedImageURI)) {
                    place.addImageUrl(selectedImageURI);
                } else if (imageBytes != null) {
                    place.addImageBytes(imageBytes);
                } else {
                    Toast.makeText(getApplicationContext(), "Не выбрано изображение", Toast.LENGTH_SHORT).show();
                    return;
                }
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            dbHelper.addPlace(place);
                        } catch (PersistenceException ex) {
                            ex.log();
                        }
                        return null;
                    }
                }.execute();
                finish();
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
                } catch (FileNotFoundException e) {
                    Log.e(APP_NAME, e.toString(), e);
                }

            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                Log.d(APP_NAME, "Сделан снимок с камеры: " + bitmap);
                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    boolean res = bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    if (res) {
                        imageBytes = baos.toByteArray();
                    }
                }

            }
        }
    }

}
