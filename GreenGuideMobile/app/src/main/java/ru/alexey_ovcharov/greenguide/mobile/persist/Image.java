package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.IOException;

import ru.alexey_ovcharov.greenguide.mobile.Commons;

/**
 * Created by Алексей on 05.05.2017.
 */

public class Image {
    public static final String TABLE_NAME = "images";
    public static final String ID_IMAGE_COLUMN = "id_image";
    public static final String URL_COLUMN = "image_url";
    public static final String BINARY_DATA_COLUMN = "binary_data";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME + " ("
            + ID_IMAGE_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            BINARY_DATA_COLUMN + " BLOB, " +
            URL_COLUMN + " VARCHAR);";

    private int idImage;
    private byte[] binaryData;
    private String url;

    public Image(int idImage, byte[] bytes, String url) {
        this.idImage = idImage;
        this.binaryData = bytes;
        this.url = url;
    }

    public Image() {
    }

    public int getIdImage() {
        return idImage;
    }

    public void setIdImage(int idImage) {
        this.idImage = idImage;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String encodeDataAsBase64(ContentResolver contentResolver) throws IOException {
        if(url == null && binaryData != null){
            return Base64.encodeToString(binaryData, Base64.DEFAULT);
        } else if (url != null){
            Uri imageUrl = Uri.parse(url);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUrl);
            byte[] bytes = Commons.bitmapToBytesPng(bitmap);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } else {
            throw new IOException("Изображение не содержит данных");
        }
    }

    public static class ImageDataWrapper<T> {
        private Integer idImage;
        private T imageData;

        public ImageDataWrapper(T imageData) {
            this.imageData = imageData;
        }

        public ImageDataWrapper(Integer idImage, T imageData) {
            this.idImage = idImage;
            this.imageData = imageData;
        }

        public Integer getIdImage() {
            return idImage;
        }

        public T getImageData() {
            return imageData;
        }

        @Override
        public String toString() {
            return "ImageDataWrapper{" +
                    "idImage=" + idImage +
                    ", imageData=" + imageData +
                    '}';
        }
    }
}
