package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

/**
 * Created by Алексей on 23.04.2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "green_guide_db";
    private static final int VERSION = 12;
    public static final int ROW_NOT_INSERTED = -1;
    public static final String SETTINGS_TABLE_NAME = "settings";
    public static final String SETTINGS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SETTINGS_TABLE_NAME + "(name VARCHAR PRIMARY KEY NOT NULL UNIQUE, value TEXT)";
    public static final String DATABASE_ID = "database_id";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(APP_NAME, "onCreate");
        try {
            db.execSQL(SETTINGS_TABLE_CREATE);
            db.execSQL("delete from " + SETTINGS_TABLE_NAME);
            ContentValues values = new ContentValues();
            values.put(DATABASE_ID, UUID.randomUUID().toString());
            db.insert(SETTINGS_TABLE_NAME, null, values);
            db.execSQL(Image.CREATE_SCRIPT);
            db.execSQL(Country.CREATE_SCRIPT);
            db.execSQL(CategoryOfThing.CREATE_SCRIPT);
            db.execSQL(PlaceType.CREATE_SCRIPT);
            db.execSQL(Place.CREATE_SCRIPT);
            db.execSQL(Place.IMAGE_FOR_PLACES_CREATE_SCRIPT);
            db.execSQL(NoteType.CREATE_SCRIPT);
            db.execSQL(Note.CREATE_SCRIPT);
            db.execSQL(Thing.CREATE_SCRIPT);
            db.execSQL(Thing.IMAGE_FOR_THING_CREATE_SCRIPT);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(APP_NAME, "onUpgrade");
        try {
            db.execSQL(Thing.IMAGE_FOR_THING_DROP_SCRIPT);
            db.execSQL(Thing.DROP_SCRIPT);
            db.execSQL(Note.DROP_SCRIPT);
            db.execSQL(NoteType.DROP_SCRIPT);
            db.execSQL(Place.IMAGE_FOR_PLACES_DROP_SCRIPT);
            db.execSQL(Place.DROP_SCRIPT);
            db.execSQL(PlaceType.DROP_SCRIPT);
            db.execSQL(CategoryOfThing.DROP_SCRIPT);
            db.execSQL(Country.DROP_SCRIPT);
            db.execSQL(Image.DROP_SCRIPT);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
        onCreate(db);
    }

    @Nullable
    public String getSettingByName(String name) {
        try {
            SQLiteDatabase database = getReadableDatabase();
            try (Cursor cursor = database.rawQuery("select value from " + SETTINGS_TABLE_NAME
                    + " where name='" + name.replaceAll("'", "\"") + "'", null)) {
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex("value"));
                }
            }
        } catch (Exception ex) {
            Log.e(APP_NAME, ex.toString(), ex);
        }
        return null;
    }

    public void putSetting(String name, String value) {
        try {
            name = name.replaceAll("'", "\"");
            value = value.replaceAll("'", "\"");
            SQLiteDatabase database = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("value", value);
            if (getSettingByName(name) != null) {
                int updatedRowsCount = database.update(SETTINGS_TABLE_NAME, cv, "name = ?", new String[]{name});
            } else {
                cv.put("name", name);
                long insertRowId = database.insert(SETTINGS_TABLE_NAME, null, cv);
            }
        } catch (Exception ex) {
            Log.e(APP_NAME, ex.toString(), ex);
        }
    }

    @NonNull
    public List<PlaceType> getPlacesTypes() {
        Log.d(APP_NAME, "Выбираю типы мест из базы");
        List<PlaceType> placeTypes = Collections.EMPTY_LIST;
        try {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(
                    "select * from place_types", null);
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

    public void addPlaceType(String placeType) throws PersistenceException {
        Log.d(APP_NAME, "Добавляю новый тип мест: " + placeType);
        try {
            SQLiteDatabase database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PlaceType.TYPE_COLUMN, placeType);
            long l = database.insert(PlaceType.TABLE_NAME, null, values);
            Log.d(APP_NAME, "Ид созданной строки: " + l);
        } catch (Exception e) {
            throw new PersistenceException("Не удалось добавить тип мест ", e);
        }
    }

    @NonNull
    public List<Place> getPlacesByTypeWithImages(int placeTypeId) throws PersistenceException {
        List<Place> places = getPlacesByTypeWithoutImages(placeTypeId);
        Log.d(APP_NAME, "Выбираю связанные изображения для мест");
        try {
            int index = 0;
            for (Place place : places) {
                SQLiteDatabase database = getReadableDatabase();
                Cursor cursor = database.rawQuery("select image " +
                        "from images_for_place where id_place = " + place.getIdPlace(), null);
                if (cursor.moveToFirst()) {
                    do {
                        String imageData = cursor.getString(cursor.getColumnIndex(Place.IMAGE_COLUMN));
                        ++index;
                        place.addImageInfo(imageData);
                    } while (cursor.moveToNext());
                }
            }
            Log.d(APP_NAME, "Готово, добавлено изображений: " + index);
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении мест", e);
        }

        return places;
    }

    @NonNull
    public List<Place> getPlacesByTypeWithoutImages(int placeTypeId) throws PersistenceException {
        Log.d(APP_NAME, "Выбираю места из базы");
        List<Place> places = Collections.EMPTY_LIST;
        try {
            SQLiteDatabase database = getReadableDatabase();
            Cursor cursor = database.rawQuery("select *" +
                    " from places where id_place_type = " + placeTypeId, null);
            if (cursor.moveToFirst()) {
                places = new ArrayList<>();
                do {
                    Place place = selectPlace(cursor);
                    places.add(place);
                } while (cursor.moveToNext());
            }
            Log.d(APP_NAME, "Выбрано мест: " + places.size());
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении мест", e);
        }
        return places;
    }

    public int getPlacesCount() throws PersistenceException {
        Log.d(APP_NAME, "Выполняю получение количество мест в базе");
        try {
            int placesCount;
            SQLiteDatabase database = getReadableDatabase();
            try (Cursor cursor = database.rawQuery("select count(*) as places_count from " + Place.TABLE_NAME, null)) {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(cursor.getColumnIndex("places_count"));
                } else {
                    throw new SQLException("Ошибка при получении количества мест в базе");
                }
            }
        } catch (Exception e) {
            throw new PersistenceException("Не удалось получить количество мест ", e);
        }
    }

    public Map<Integer, String> getCountries() throws PersistenceException {
        Log.d(APP_NAME, "Получаю список стран");
        try {
            Map<Integer, String> countries = new HashMap<>();
            SQLiteDatabase database = getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from " + Country.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                do {
                    String countryName = cursor.getString(cursor.getColumnIndex(Country.COUNTRY_COLUMN));
                    int idCountry = cursor.getInt(cursor.getColumnIndex(Country.ID_COUNTRY_COLUMN));
                    countries.put(idCountry, countryName);
                } while (cursor.moveToNext());
            }
            Log.d(APP_NAME, "Найдено стран: " + countries.size());
            return countries;
        } catch (Exception ex) {
            throw new PersistenceException("Не удалось получить список стран", ex);
        }
    }

    public long addImage(byte[] imageBytes) throws PersistenceException {
        Log.d(APP_NAME, "Вставляю изображение, размер(байт):  " + imageBytes.length);
        try {
            SQLiteDatabase database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Image.BINARY_DATA_COLUMN, imageBytes);
            long rowNum = database.insert(Image.TABLE_NAME, null, values);
            if (rowNum != -1) {
                Log.d(APP_NAME, "Изображение вставлено");
            }
            return rowNum;
        } catch (Exception ex) {
            throw new PersistenceException("Не вставить изображение", ex);
        }
    }

    @NonNull
    public List<Image> getImageData(List<String> idsImage) throws PersistenceException {
        Log.d(APP_NAME, "Получаю данные изображения по ид: " + idsImage);
        try {
            SQLiteDatabase database = getReadableDatabase();
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (String idImage : idsImage) {
                if (!first) {
                    builder.append(",");
                } else {
                    first = false;
                }
                builder.append(idImage);
            }
            Cursor cursor = database.rawQuery("select * from "
                    + Image.TABLE_NAME + " where "
                    + Image.ID_IMAGE_COLUMN + " in (" + builder.toString() + ")", null);

            List<Image> imageList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    int idImage = cursor.getInt(cursor.getColumnIndex(Image.ID_IMAGE_COLUMN));
                    byte[] imageData = cursor.getBlob(cursor.getColumnIndex(Image.BINARY_DATA_COLUMN));
                    Image image = new Image();
                    image.setIdImage(idImage);
                    image.setBinaryData(imageData);
                    imageList.add(image);
                } while (cursor.moveToNext());
            }
            Log.d(APP_NAME, "Выбрано изображений: " + imageList.size());
            return imageList;
        } catch (Exception ex) {
            throw new PersistenceException("Не удалось получить изображения", ex);
        }
    }

    @NonNull
    private Place selectPlace(Cursor cursor) throws ParseException {
        Place place = new Place();
        int placeTypeId = cursor.getInt(cursor.getColumnIndex(Place.ID_PLACE_TYPE_COLUMN));
        place.setIdPlaceType(placeTypeId);
        int placeId = cursor.getInt(cursor.getColumnIndex(Place.ID_PLACE_COLUMN));
        place.setIdPlace(placeId);

        int columnIndexAddress = cursor.getColumnIndex(Place.ADDRESS_COLUMN);
        String address = cursor.getString(columnIndexAddress);
        if (!"null".equals(address)) {
            place.setAddress(address);
        }
        String description = cursor.getString(cursor.getColumnIndex(Place.DESCRIPTION_COLUMN));
        place.setDescription(description);
        String dateCreate = cursor.getString(cursor.getColumnIndex(Place.DATE_CREATE_COLUMN));
        place.setDateCreate(dateCreate);
        int columnIndexCountry = cursor.getColumnIndex(Place.ID_COUNTRY_COLUMN);
        Integer idCountry = cursor.getInt(columnIndexCountry);
        if (cursor.isNull(columnIndexCountry)) {
            idCountry = null;
        }
        place.setIdCountry(idCountry);

        BigDecimal latitude;
        int columnIndexLatitude = cursor.getColumnIndex(Place.LATITUDE_COLUMN);
        if (cursor.isNull(columnIndexLatitude)) {
            latitude = null;
        } else {
            latitude = new BigDecimal(cursor.getString(columnIndexLatitude));
        }
        place.setLatitude(latitude);

        BigDecimal longitude;
        int columnIndexLongitude = cursor.getColumnIndex(Place.LONGITUDE_COLUMN);
        if (cursor.isNull(columnIndexLongitude)) {
            longitude = null;
        } else {
            longitude = new BigDecimal(cursor.getString(columnIndexLongitude));
        }
        place.setLatitude(longitude);
        return place;
    }

    public void addPlace(Place place) throws PersistenceException {
        Log.d(APP_NAME, "Добавляю новое место: " + place);
        SQLiteDatabase database = null;
        try {
            database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Place.ADDRESS_COLUMN, place.getAddress());
            values.put(Place.DATE_CREATE_COLUMN, place.getDateCreate());
            values.put(Place.DESCRIPTION_COLUMN, place.getDescription());
            values.put(Place.ID_COUNTRY_COLUMN, place.getIdCountry());
            values.put(Place.ID_PLACE_TYPE_COLUMN, place.getIdPlaceType());
            if (place.getLatitude() != null) {
                values.put(Place.LATITUDE_COLUMN, place.getLatitude().toString());
            }
            if (place.getLongitude() != null) {
                values.put(Place.LONGITUDE_COLUMN, place.getLongitude().toString());
            }
            List<String> imagesUrls = place.getImagesInfo();
            database.beginTransaction();
            long idPlace = database.insert(Place.TABLE_NAME, null, values);
            if (idPlace != ROW_NOT_INSERTED) {
                for (String imageUrl : imagesUrls) {
                    ContentValues imageCv = new ContentValues(2);
                    imageCv.put(Place.IMAGE_COLUMN, imageUrl);
                    imageCv.put(Place.ID_PLACE_COLUMN, idPlace);
                    long insert = database.insert(Place.IMAGE_FOR_PLACES_TABLE_NAME, null, imageCv);
                    if (insert == ROW_NOT_INSERTED) {
                        throw new Exception("Не удалось добавить изображение для места с ид: "
                                + idPlace + ", выполняю откат");
                    }
                }
                database.setTransactionSuccessful();
                Log.d(APP_NAME, "Место добавлено, ид: " + idPlace);
            }
        } catch (Exception ex) {
            throw new PersistenceException("Не удалось добавить место", ex);
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
    }


    public void addCategoryOfThing(String categoryName) throws PersistenceException {
        Log.d(APP_NAME, "Создаю новую категорию вещей: " + categoryName);
        try {
            SQLiteDatabase database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CategoryOfThing.CATEGORY_COLUMN, categoryName);
            long l = database.insert(CategoryOfThing.TABLE_NAME,
                    CategoryOfThing.ID_CATEGORY_COLUMN, values);
            Log.d(APP_NAME, "Ид созданной строки: " + l);
        } catch (Exception e) {
            throw new PersistenceException("Не удалось добавить категорию вещей", e);
        }
    }

    public Place getPlaceWithImages(int placeId) throws PersistenceException {
        Log.d(APP_NAME, "Выбираю место с изображениями, ид: " + placeId);
        try {
            SQLiteDatabase database = getReadableDatabase();
            try (Cursor cursorPlace = database.rawQuery("select *" +
                    " from places where id_place = " + placeId, null)) {
                if (cursorPlace.moveToFirst()) {
                    Place place = selectPlace(cursorPlace);
                    try (Cursor cursorImages = database.rawQuery("select image " +
                            "from images_for_place where id_place = " + placeId, null)) {
                        if (cursorImages.moveToFirst()) {
                            do {
                                String imageInfo = cursorImages.getString(cursorImages.getColumnIndex(Place.IMAGE_COLUMN));
                                place.addImageInfo(imageInfo);
                            } while (cursorImages.moveToNext());
                        }
                    }
                    Log.d(APP_NAME, "Выбрано изображений: " + place.getImagesInfo().size());
                    return place;
                } else {
                    throw new PersistenceException("Не найдено место с ид: " + placeId);
                }
            }
        } catch (Exception e) {
            throw new PersistenceException("Не удалось получить информацию для места с ид: " + placeId, e);
        }
    }
}
