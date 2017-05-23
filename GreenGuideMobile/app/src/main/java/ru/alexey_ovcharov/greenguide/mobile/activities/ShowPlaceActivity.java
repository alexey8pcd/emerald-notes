package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class ShowPlaceActivity extends Activity {

    private Intent intent;
    public static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private RecyclerView imagesView;
    private Uri tempImageUri;
    private TextView tvPhotos;

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.custom_image);
            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog builder = new Dialog(ShowPlaceActivity.this);
                    builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    builder.getWindow().setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            //nothing;
                        }
                    });

                    ImageView showView = new ImageView(ShowPlaceActivity.this);
                    Drawable drawable = imageView.getDrawable();
                    showView.setImageDrawable(drawable);
                    builder.addContentView(showView, new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    builder.show();
                }
            });
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private final Context context;
        private final List<Image.ImageDataWrapper<Bitmap>> objects;

        public ImageAdapter(@NonNull Context context,
                            @NonNull List<Image.ImageDataWrapper<Bitmap>> objects) {
            this.context = context;
            this.objects = objects;
        }


        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View layout = inflater.inflate(R.layout.custom_layout, parent, false);
            ImageViewHolder imageViewHolder = new ImageViewHolder(layout);
            return imageViewHolder;
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            if (bitmaps != null && position < bitmaps.size()) {
                Image.ImageDataWrapper<Bitmap> wrapper = bitmaps.get(position);
                holder.imageView.setImageBitmap(wrapper.getImageData());
            }
        }

        @Override
        public int getItemCount() {
            return bitmaps != null ? bitmaps.size() : 0;
        }
    }

    private DbHelper dbHelper;
    private int selectedPlaceId;
    private Place placeWithImages;
    private List<Image.ImageDataWrapper<Bitmap>> bitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_place);
        intent = getIntent();
        tvPhotos = (TextView) findViewById(R.id.aShowPlace_tvTitlePhotos);
        GridLayoutManager layoutManager;
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == ORIENTATION_LANDSCAPE) {
            layoutManager = new GridLayoutManager(this, 3);
        } else {
            layoutManager = new GridLayoutManager(this, 2);
        }
        imagesView = (RecyclerView) findViewById(R.id.aShowPlace_rvImages);
        imagesView.setLayoutManager(layoutManager);

        selectedPlaceId = intent.getIntExtra(Place.ID_PLACE_COLUMN, 0);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        Button bAddImage = (Button) findViewById(R.id.aShowPlaces_bAddImage);
        bAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ShowPlaceActivity.this);
                ad.setTitle("Выбор источника");
                ad.setMessage("Откуда требуется получить изображение?");
                ad.setPositiveButton("Снимок с камеры", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        File photo = Commons.getTempPhotoFile();
                        if (photo != null) {
                            tempImageUri = Uri.fromFile(photo);
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        } else {
                            Toast.makeText(ShowPlaceActivity.this, "Не удалось создать изображения, " +
                                            "возможно нет доступа к карте памяти!",
                                    Toast.LENGTH_SHORT).show();
                        }
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
                        long idImage = dbHelper.addImage(selectedImageURI);
                        placeWithImages.addImageId((int) idImage);

                    }
                } else if (requestCode == CAMERA_REQUEST) {
                    if (tempImageUri != null) {
                        String imageUrl = tempImageUri.toString();
                        Log.d(APP_NAME, "Сделан снимок с камеры: " + imageUrl);
                        long idImage = dbHelper.addImage(imageUrl);
                        placeWithImages.addImageId((int) idImage);
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
                    tvPhotos.setText("Фотографии места, количество: " + bitmaps.size());
                    imagesView.setAdapter(new ImageAdapter(this, bitmaps));
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
