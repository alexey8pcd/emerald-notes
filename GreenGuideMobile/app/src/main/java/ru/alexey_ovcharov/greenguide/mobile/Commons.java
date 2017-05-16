package ru.alexey_ovcharov.greenguide.mobile;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Алексей on 21.04.2017.
 */

public class Commons {
    public static final String APP_NAME = "Зеленый гид";
    public static final String PLACE_TYPE_ID = "placeTypeId";
    public static final String WORK_DIR = "/Android/data/ru.alexey_ovcharov.greenguide/";
    public static final String DIALOG_TITLE = "dialogTitle";
    public static final String DIALOG_RESULT = "dialogResult";
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String PLACE_ID = "placeId";
    public static final String SERVER_URL = "server_url";

    @NonNull
    public static <T> String[] listToStringArray(@NonNull List<T> list, @NonNull Mapper<T> mapper) {
        String[] result = new String[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = mapper.map(list.get(i));
        }
        return result;
    }

    public static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
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

}
