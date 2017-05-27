package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.alexey_ovcharov.greenguide.mobile.Commons;

import static ru.alexey_ovcharov.greenguide.mobile.persist.Entity.GUID_COLUMN_NAME;

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
            URL_COLUMN + " VARCHAR, " +
            GUID_COLUMN_NAME + " VARCHAR (36) NOT NULL UNIQUE)";

    private int idImage;
    private String url;
    private String guid;

    public Image(Cursor cursor) {
        idImage = cursor.getInt(cursor.getColumnIndex(ID_IMAGE_COLUMN));
        url = cursor.getString(cursor.getColumnIndex(URL_COLUMN));
        guid = cursor.getString(cursor.getColumnIndex(Entity.GUID_COLUMN_NAME));
    }

    public String getGuid() {
        return guid;
    }

    public Image() {
    }

    public int getIdImage() {
        return idImage;
    }

    public void setIdImage(int idImage) {
        this.idImage = idImage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject toJsonObject(ContentResolver contentResolver) throws IOException, JSONException {
        String base64 = encodeDataAsBase64(contentResolver);
        JSONObject imageData = new JSONObject();
        imageData.put(Image.ID_IMAGE_COLUMN, idImage);
        imageData.put(Image.BINARY_DATA_COLUMN, base64);
        imageData.put(Entity.GUID_COLUMN_NAME, guid);
        return imageData;
    }

    private String encodeDataAsBase64(ContentResolver contentResolver) throws IOException {
        if (url != null) {
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
