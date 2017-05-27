package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.database.Cursor;

import static ru.alexey_ovcharov.greenguide.mobile.persist.Entity.GUID_COLUMN_NAME;

/**
 * Created by Алексей on 26.04.2017.
 */

@Entity
public class Country {

    public static final String TABLE_NAME = "countries";
    public static final String ID_COUNTRY_COLUMN = "id_country";
    public static final String COUNTRY_COLUMN = "country";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME + " ("
            + ID_COUNTRY_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COUNTRY_COLUMN + " VARCHAR (50) NOT NULL UNIQUE, " +
            GUID_COLUMN_NAME + " VARCHAR (36) NOT NULL UNIQUE)";

    private int idCountry;
    private String countryName;
    private String guid;

    public Country() {
    }

    public Country(Cursor cursor) {
    }

    public String getGuid() {
        return guid;
    }

    public int getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(int idCountry) {
        this.idCountry = idCountry;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

}