package ru.alexey_ovcharov.greenguide.mobile.persist;


import android.util.Base64;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;

/**
 * Created by Алексей on 23.04.2017.
 */
@Entity
public class Place {

    @Override
    public String toString() {
        return "Place{" +
                "idPlace=" + idPlace +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", dateCreate=" + dateCreate +
                ", idPlaceType=" + idPlaceType +
                ", idCountry=" + idCountry +
                '}';
    }

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
            "   ON DELETE RESTRICT ON UPDATE CASCADE);";

    public static final String IMAGE_FOR_PLACES_TABLE_NAME = "images_for_place";
    public static final String ID_IMAGE_FOR_PLACE_COLUMN = "id_image_for_place";
    public static final String IMAGE_COLUMN = "image";
    public static final String IMAGE_FOR_PLACES_DROP_SCRIPT = "DROP TABLE IF EXISTS " + IMAGE_FOR_PLACES_TABLE_NAME;
    public static final String IMAGE_FOR_PLACES_CREATE_SCRIPT = "CREATE TABLE " + IMAGE_FOR_PLACES_TABLE_NAME + " (" +
            ID_IMAGE_FOR_PLACE_COLUMN + " INTEGER PRIMARY KEY NOT NULL, " +
            ID_PLACE_COLUMN + " INTEGER NOT NULL REFERENCES places (id_place) " +
            "   ON DELETE RESTRICT ON UPDATE CASCADE, " +
            IMAGE_COLUMN + " VARCHAR NOT NULL);";

    private int idPlace;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String dateCreate;
    private int idPlaceType;
    private Integer idCountry;

    public List<String> getImagesInfo() {

        return imagesInfo;
    }

    public void addImageUrl(String imageUrl) {

        imagesInfo.add("URL:" + imageUrl);
    }

    public void addImageBytes(byte[] bytes){
        imagesInfo.add("Data:"+ Base64.encodeToString(bytes, Base64.DEFAULT));
    }

    private List<String> imagesInfo = new ArrayList<>(1);

    public Place() {

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

    public void setDateCreate(String dateYYYY_MM_DD) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Commons.SQL_DATE_FORMAT);
        dateFormat.setLenient(true);
        dateFormat.parse(dateYYYY_MM_DD);
        this.dateCreate = dateYYYY_MM_DD;
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
}
