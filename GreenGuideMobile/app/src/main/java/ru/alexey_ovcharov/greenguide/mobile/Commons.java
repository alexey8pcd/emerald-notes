package ru.alexey_ovcharov.greenguide.mobile;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Алексей on 21.04.2017.
 */

public class Commons {
    public static final String APP_NAME = "Green Guide";
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SERVER_URL = "server_url";
    public static final String MAP_OPEN_TYPE = "mapOpenType";
    public static final int OPEN_TYPE_CHOOSE_LOCATION = 1;
    public static final String SELECTED_IMAGE_URI = "selectedImageUri";
    public static final String URL_REFERENCES = "/greenserver/references";
    public static final String PLACES_DATA_URL_PART = "/places_data";
    public static final String IMAGES_PART = "/images_data";

    @NonNull
    public static <T> List<String> listToStringArray(@NonNull List<T> list, @NonNull Mapper<T> mapper) {
        List<String> result = new ArrayList<>(list.size());
        for (T item : list) {
            result.add(mapper.map(item));
        }
        return result;
    }

    @NonNull
    public static byte[] bitmapToBytesPng(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean res = bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        if (res) {
            return baos.toByteArray();
        }
        throw new IOException("Не удалось преобразовать изображение");
    }

    @Nullable
    public static File getTempPhotoFile() {
        File photo;
        try {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            File imagesDir = new File(externalStorageDirectory.getCanonicalPath() + "/Images/");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            photo = File.createTempFile("picture" + System.currentTimeMillis(), ".png", imagesDir);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
            return null;
        }
        return photo;
    }

    @NonNull
    public static Map<String, String> saveImagesOnSdCard(Map<String, byte[]> imagesDataMap) throws IOException {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File imagesDir = new File(externalStorageDirectory.getCanonicalPath() + "/Images/");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        Map<String, String> guidesAndUrls = new HashMap<>(imagesDataMap.size());
        long timeMillis = System.currentTimeMillis();
        for (Map.Entry<String, byte[]> entry : imagesDataMap.entrySet()) {
            String guid = entry.getKey();
            File tempFile = File.createTempFile("picture" + guid, ".png", imagesDir);
            IOUtils.write(entry.getValue(), new FileOutputStream(tempFile));
            Uri uri = Uri.fromFile(tempFile);
            guidesAndUrls.put(guid, uri.toString());
        }
        return guidesAndUrls;
    }

    @NonNull
    public static String readStreamToString(InputStream inputStream, String charset) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    public static boolean stringsAreBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return true;
        }
        for (String s : strings) {
            if (StringUtils.isNotBlank(s)) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    public static <K, V> Map<V, K> invertMap(Map<K, V> input) {
        Map<V, K> res = new HashMap<>(input.size());
        for (Map.Entry<K, V> entry : input.entrySet()) {
            res.put(entry.getValue(), entry.getKey());
        }
        return res;
    }
}
