package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

/**
 * Created by Алексей on 23.04.2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "green_guide_db";
    private static final int VERSION = 6;
    public static final int ROW_NOT_INSERTED = -1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        Log.d(APP_NAME, "new" + DbHelper.class.getSimpleName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(APP_NAME, "onCreate");
        try {
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

    public List<Place> getPlacesByType(int placeTypeId) {
        Log.d(APP_NAME, "Выбираю места из базы");
        List<Place> places = Collections.EMPTY_LIST;
        try {
            SQLiteDatabase database = getReadableDatabase();
            Cursor cursor = database.rawQuery("select *" +
                    " from places where id_place_type = " + placeTypeId, null);
            if (cursor.moveToFirst()) {
                places = new ArrayList<>();
                do {
                    Place place = new Place();
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

                    places.add(place);
                } while (cursor.moveToNext());
            }
            Log.d(APP_NAME, "Выбрано мест: " + places.size());
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }
        return places;
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
}
