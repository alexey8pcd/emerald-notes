package ru.alexey_ovcharov.greenguide.mobile.persist;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.alexey_ovcharov.greenguide.mobile.Commons;

import static ru.alexey_ovcharov.greenguide.mobile.persist.Entity.GUID_COLUMN_NAME;

/**
 * Created by Алексей on 23.04.2017.
 */
@Entity
public class Place extends ImagesGallery {

    public static final String ID_PLACE_COLUMN = "id_place";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String LATITUDE_COLUMN = "latitude";
    public static final String LONGITUDE_COLUMN = "longitude";
    public static final String ADDRESS_COLUMN = "address";
    public static final String DATE_CREATE_COLUMN = "date_create";
    public static final String ID_PLACE_TYPE_COLUMN = "id_place_type";
    public static final String ID_COUNTRY_COLUMN = "id_country";
    public static final String TABLE_NAME = "places";

    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME
            + " (" + ID_PLACE_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            DESCRIPTION_COLUMN + " TEXT, " +
            LATITUDE_COLUMN + " DECIMAL (15, 6), " +
            LONGITUDE_COLUMN + " DECIMAL (15, 6), " +
            ADDRESS_COLUMN + " VARCHAR (100), " +
            DATE_CREATE_COLUMN + " DATETIME, " +
            ID_PLACE_TYPE_COLUMN + " INTEGER NOT NULL REFERENCES " +
            "       place_types (id_place_type) ON DELETE RESTRICT ON UPDATE CASCADE, " +
            ID_COUNTRY_COLUMN + " INTEGER REFERENCES countries (id_country) " +
            "   ON DELETE RESTRICT ON UPDATE CASCADE, " +
            GUID_COLUMN_NAME + " VARCHAR (36) NOT NULL UNIQUE)";

    public static final String IMAGES_FOR_PLACE_TABLE_NAME = "images_for_place";
    public static final String ID_IMAGE_FOR_PLACE_COLUMN = "id_image_for_place";
    public static final String IMAGE_FOR_PLACES_DROP_SCRIPT = "DROP TABLE IF EXISTS " + IMAGES_FOR_PLACE_TABLE_NAME;
    public static final String IMAGE_FOR_PLACES_CREATE_SCRIPT = "CREATE TABLE "
            + IMAGES_FOR_PLACE_TABLE_NAME + " (" +
            ID_IMAGE_FOR_PLACE_COLUMN + " INTEGER PRIMARY KEY NOT NULL, " +
            ID_PLACE_COLUMN + " INTEGER NOT NULL " + "," +
            Image.ID_IMAGE_COLUMN + " INTEGER NOT NULL)";

    private int idPlace;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String dateCreate;
    private int idPlaceType;
    private Integer idCountry;
    private String guid;

    public Place(JSONObject placeJson) throws JSONException, ParseException {
        description = placeJson.getString(Place.DESCRIPTION_COLUMN);
        guid = placeJson.getString(Entity.GUID_COLUMN_NAME);
        address = placeJson.optString(Place.ADDRESS_COLUMN);
        if (StringUtils.isBlank(address)) {
            address = null;
        }
        dateCreate = placeJson.getString(Place.DATE_CREATE_COLUMN);
        if (placeJson.has(Place.LATITUDE_COLUMN)) {
            latitude = new BigDecimal(placeJson.getDouble(Place.LATITUDE_COLUMN));
        }
        if (placeJson.has(Place.LONGITUDE_COLUMN)) {
            longitude = new BigDecimal(placeJson.getDouble(Place.LONGITUDE_COLUMN));
        }
    }

    public Place() {

    }

    public Place(Cursor cursor) throws ParseException {
        idPlaceType = cursor.getInt(cursor.getColumnIndex(ID_PLACE_TYPE_COLUMN));
        idPlace = cursor.getInt(cursor.getColumnIndex(ID_PLACE_COLUMN));
        int columnIndexAddress = cursor.getColumnIndex(ADDRESS_COLUMN);
        address = cursor.getString(columnIndexAddress);
        if (cursor.isNull(columnIndexAddress) || "null".equals(address)) {
            address = null;
        }
        description = cursor.getString(cursor.getColumnIndex(Place.DESCRIPTION_COLUMN));
        dateCreate = cursor.getString(cursor.getColumnIndex(Place.DATE_CREATE_COLUMN));
        int columnIndexCountry = cursor.getColumnIndex(Place.ID_COUNTRY_COLUMN);
        idCountry = cursor.getInt(columnIndexCountry);
        if (cursor.isNull(columnIndexCountry)) {
            idCountry = null;
        }
        int columnIndexLatitude = cursor.getColumnIndex(Place.LATITUDE_COLUMN);
        if (cursor.isNull(columnIndexLatitude)) {
            latitude = null;
        } else {
            latitude = new BigDecimal(cursor.getString(columnIndexLatitude));
        }

        int columnIndexLongitude = cursor.getColumnIndex(Place.LONGITUDE_COLUMN);
        if (cursor.isNull(columnIndexLongitude)) {
            longitude = null;
        } else {
            longitude = new BigDecimal(cursor.getString(columnIndexLongitude));
        }
        guid = cursor.getString(cursor.getColumnIndex(Entity.GUID_COLUMN_NAME));
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    @Override
    public String toString() {
        return "Place{" +
                "idPlace=" + idPlace +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", dateCreate='" + dateCreate + '\'' +
                ", idPlaceType=" + idPlaceType +
                ", idCountry=" + idCountry +
                ", imagesInfo=" + imagesInfo +
                ", guid='" + guid + '\'' +
                '}';
    }

    public void setDateCreate(Date dateCreate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Commons.SQL_DATE_FORMAT);
        this.dateCreate = dateFormat.format(dateCreate);
    }

    public int getIdPlace() {
        return idPlace;
    }

    public void setIdPlace(int idPlace) {
        this.idPlace = idPlace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(Integer idCountry) {
        this.idCountry = idCountry;
    }

    public int getIdPlaceType() {
        return idPlaceType;
    }

    public void setIdPlaceType(int idPlaceType) {
        this.idPlaceType = idPlaceType;
    }


    public JSONObject toJsonObject(Map<Integer, String> countries) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DESCRIPTION_COLUMN, description);
        jsonObject.put(ADDRESS_COLUMN, address);
        jsonObject.put(Country.COUNTRY_COLUMN, countries.get(idCountry));
        jsonObject.put(LATITUDE_COLUMN, latitude);
        jsonObject.put(LONGITUDE_COLUMN, longitude);
        jsonObject.put(DATE_CREATE_COLUMN, dateCreate);
        jsonObject.put(ID_PLACE_TYPE_COLUMN, idPlaceType);
        jsonObject.put(Entity.GUID_COLUMN_NAME, guid);
        JSONArray imagesJsonArray = new JSONArray();
        for (Image image : imagesInfo) {
            imagesJsonArray.put(image.getGuid());
        }
        jsonObject.put(Image.TABLE_NAME, imagesJsonArray);
        return jsonObject;
    }

    @Nullable
    public LatLng getLocation() {
        if (latitude != null && longitude != null) {
            return new LatLng(latitude.doubleValue(), longitude.doubleValue());
        } else {
            return null;
        }
    }

    public String getGuid() {
        return guid;
    }
}
