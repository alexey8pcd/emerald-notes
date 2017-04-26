package ru.alexey_ovcharov.greenguide.mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.entities.CategoryOfThing;
import ru.alexey_ovcharov.greenguide.mobile.entities.Place;
import ru.alexey_ovcharov.greenguide.mobile.entities.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

/**
 * Created by Алексей on 23.04.2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "green_guide_db";
    private static final int VERSION = 4;
    private static final String CREATE_SCRIPT = "PRAGMA foreign_keys = off;" +
            "-- Таблица: images_for_place" +
            "DROP TABLE IF EXISTS images_for_place;" +
            "   CREATE TABLE images_for_place (id_image_for_place INTEGER PRIMARY KEY NOT NULL, " +
            "   id_place INTEGER NOT NULL REFERENCES places (id_place) ON DELETE RESTRICT ON UPDATE CASCADE, " +
            "   image VARCHAR NOT NULL);" +
            "-- Таблица: images_for_thing" +
            "DROP TABLE IF EXISTS images_for_thing;" +
            "CREATE TABLE images_for_thing (id_image_for_thing INTEGER NOT NULL, " +
            "   id_thing INTEGER NOT NULL REFERENCES things (id_thing) ON DELETE RESTRICT ON UPDATE CASCADE, " +
            "   image VARCHAR NOT NULL);" +
            "-- Таблица: note_types" +
            "DROP TABLE IF EXISTS note_types;" +
            "CREATE TABLE note_types (id_note_type INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "   note_type VARCHAR (100) NOT NULL);" +
            "-- Таблица: notes" +
            "DROP TABLE IF EXISTS notes;" +
            "CREATE TABLE notes (id_note INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "   note_text TEXT NOT NULL, id_note_type INTEGER NOT NULL REFERENCES note_types (id_note_type) " +
            "   ON DELETE RESTRICT ON UPDATE CASCADE);" +
            "-- Таблица: places" +
            "DROP TABLE IF EXISTS places;" +
            "CREATE TABLE places (id_place INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "   description TEXT, latitude DECIMAL (15, 6), longitude DECIMAL (15, 6), " +
            "   address VARCHAR (100), date_create DATETIME, " +
            "   id_place_type INTEGER NOT NULL REFERENCES place_types (id_place_type) " +
            "   ON DELETE RESTRICT ON UPDATE CASCADE, id_country INTEGER REFERENCES " +
            "   countries (id_country) ON DELETE RESTRICT ON UPDATE CASCADE);" +
            "-- Таблица: things" +
            "DROP TABLE IF EXISTS things;" +
            "CREATE TABLE things (id_thing INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "   name VARCHAR (100) NOT NULL, description TEXT NOT NULL, " +
            "   id_danger_for_environment INTEGER NOT NULL, decomposition_time INTEGER, " +
            "   id_country INTEGER REFERENCES countries (id_country) " +
            "   ON DELETE RESTRICT ON UPDATE CASCADE, id_category INTEGER NOT NULL " +
            "   REFERENCES categories_of_things (id_category) ON DELETE RESTRICT ON UPDATE CASCADE);" +
            "COMMIT TRANSACTION;" +
            "PRAGMA foreign_keys = on;";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        Log.d(APP_NAME, "new" + DbHelper.class.getSimpleName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(APP_NAME, "onCreate");
        try {
            db.execSQL(CategoryOfThing.CREATE_SCRIPT);
            db.execSQL(PlaceType.CREATE_SCRIPT);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(APP_NAME, "onUpgrade");
        try {
            db.execSQL(CategoryOfThing.DROP_SCRIPT);
            db.execSQL(PlaceType.DROP_SCRIPT);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
        onCreate(db);
    }

    @NonNull
    public List<PlaceType> getPlacesTypes() {
        Log.d(APP_NAME, "Выбираю типы мест из базы");
        List<PlaceType> placeTypes = Collections.EMPTY_LIST;
        try {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(
                    "select * " +
                    "from place_types", null);
            if (cursor.moveToFirst()) {
                placeTypes = new ArrayList<>();
                do {
                    int idPlaceType = cursor.getInt(cursor.getColumnIndex(PlaceType.ID_PLACE_TYPE_COLUMN));
                    String type = cursor.getString(cursor.getColumnIndex(PlaceType.TYPE_COLUMN));
                    placeTypes.add(new PlaceType(idPlaceType, type));
                } while (cursor.moveToNext());

            }
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
        Log.d(APP_NAME, "Выбрал типов: " + placeTypes.size());
        return placeTypes;
    }

    public boolean addPlaceType(String placeType) {
        Log.d(APP_NAME, "Добавляю новый тип мест: " + placeType);
        try {
            SQLiteDatabase database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PlaceType.TYPE_COLUMN, placeType);
            long l = database.insert(PlaceType.TABLE_NAME, null, values);
            Log.d(APP_NAME, "Ид созданной строки: " + l);
            return l != -1;
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
            return false;
        }
    }

    public List<Place> getPlacesByType(int placeTypeId) {
        switch (placeTypeId) {
            case 1:
                Place p1 = new Place();
                p1.setAddress("Барнаул, ул. Малахова, 55");
                return Arrays.asList(p1);
            case 2:
                Place p2 = new Place();
                p2.setAddress("Барнаул, ул. Взлетная, 12");
                return Arrays.asList(p2);
            case 3:
                Place p3 = new Place();
                p3.setAddress("Барнаул, ул. Сухэ-Батора, 13");
                return Arrays.asList(p3);
            case 4:
                Place p4 = new Place();
                p4.setAddress("Барнаул, пр-т Космонавтов, 1");
                return Arrays.asList(p4);
        }
        return Collections.EMPTY_LIST;
    }


    public boolean addCategoryOfThing(String categoryName) {
        Log.d(APP_NAME, "Создаю новую категорию вещей: " + categoryName);
        try {
            SQLiteDatabase database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CategoryOfThing.CATEGORY_COLUMN, categoryName);
            long l = database.insert(CategoryOfThing.TABLE_NAME,
                    CategoryOfThing.ID_CATEGORY_COLUMN, values);
            Log.d(APP_NAME, "Ид созданной строки: " + l);
            return l != -1;
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
            return false;
        }
    }
}
