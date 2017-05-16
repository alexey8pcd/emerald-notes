package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class ShowPlaceActivity extends Activity {

    public static final int IMAGES_PREVIEW_SIZE = 100;
    private Intent intent;
    public static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private GridView gvImages;

    private class ImageAdapter extends ArrayAdapter<Image.ImageDataWrapper<Bitmap>> {


        private final Context context;
        private final List<Image.ImageDataWrapper<Bitmap>> objects;

        public ImageAdapter(@NonNull Context context, @NonNull List<Image.ImageDataWrapper<Bitmap>> objects) {
            super(context, -1, objects.toArray(new Image.ImageDataWrapper[0]));
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.custom_layout, parent, false);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.custom_image);
            imageView.setMaxWidth(IMAGES_PREVIEW_SIZE);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(objects.get(position).getImageData());
            return rowView;
        }
    }

    private DbHelper dbHelper;
    private int selectedPlaceId;
    private Place placeWithImages;
    private List<Image.ImageDataWrapper<Bitmap>> bitmaps;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_place);
        intent = getIntent();
        selectedPlaceId = intent.getIntExtra(Commons.PLACE_ID, 0);
        dbHelper = new DbHelper(getApplicationContext());
        gvImages = (GridView) findViewById(R.id.aShowPlaces_gvImages);
        gvImages.setNumColumns(GridView.AUTO_FIT);
        gvImages.setColumnWidth(IMAGES_PREVIEW_SIZE);
        gvImages.setVerticalSpacing(5);
        gvImages.setHorizontalSpacing(5);
        Button bAddImage = (Button) findViewById(R.id.aShowPlaces_bAddImage);
        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ShowPlaceActivity.this);
                ad.setTitle("Выбор источника");
                ad.setMessage("Откуда требуется получить изображение?");
                ad.setPositiveButton("Снимок с камеры", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                });
                ad.setNegativeButton("Изображение из галереи", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
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
                });
                ad.setCancelable(true);
                ad.show();
            }
        });
        setPlaceInfoFromDb();
        loadImages();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                if (requestCode == PICK_IMAGE_REQUEST) {
                    if (data != null) {
                        Uri imageUrl = data.getData();
                        Log.d(APP_NAME, "Выбрано изображение на устройстве: " + imageUrl);
                        String selectedImageURI = imageUrl.toString();
                        long idImage = dbHelper.addImage(null, selectedImageURI);
                        placeWithImages.addImageId((int) idImage);

                    }
                } else if (requestCode == CAMERA_REQUEST) {
                    if (data != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        Log.d(APP_NAME, "Сделан снимок с камеры: " + bitmap);
                        if (bitmap != null) {
                            long idImage = dbHelper.addImage(Commons.bitmapToBytesPng(bitmap), null);
                            placeWithImages.addImageId((int) idImage);
                        }
                    }
                }
                dbHelper.updatePlace(placeWithImages);
                loadImages();
            } catch (Exception e) {
                Log.e(APP_NAME, e.toString(), e);
            }
        }

    }


    private void setPlaceInfoFromDb() {
        try {
            placeWithImages = dbHelper.getPlaceWithImages(selectedPlaceId);

            TextView view = (TextView) findViewById(R.id.aShowPlace_tvPlaceDescription);
            view.setText(placeWithImages.getDescription());

            TextView tvAddress = (TextView) findViewById(R.id.aShowPlace_tvAddress);
            String address = placeWithImages.getAddress();
            if (address == null || "null".equals(address) || address.isEmpty()) {
                address = "Адрес не указан";
            }
            tvAddress.setText(address);
        } catch (PersistenceException e) {
            e.log();
        }
    }

    private void loadImages() {
        if (placeWithImages != null) {
            bitmaps = placeWithImages.getImagesBitmaps(dbHelper, getContentResolver());
            try {
                if (bitmaps != null) {
                    gvImages.setAdapter(new ImageAdapter(this, bitmaps));
                    if (bitmaps.size() != placeWithImages.getImagesIds().size()) {
                        Toast.makeText(this, "Не все изображения были открыть успешно", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e(Commons.APP_NAME, e.toString(), e);
            }

        }
    }

}
