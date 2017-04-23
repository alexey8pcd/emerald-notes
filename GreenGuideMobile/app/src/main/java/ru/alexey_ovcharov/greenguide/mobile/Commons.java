package ru.alexey_ovcharov.greenguide.mobile;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by Алексей on 21.04.2017.
 */

public class Commons {
    public static final String APP_NAME = "Зеленый гид";
    public static final String PLACE_TYPE_ID = "placeTypeId";

    @NonNull
    public static<T> String[] listToStringArray(@NonNull List<T> list, @NonNull Mapper<T> mapper){
        String[] result = new String[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = mapper.map(list.get(i));
        }
        return result;
    }
}
