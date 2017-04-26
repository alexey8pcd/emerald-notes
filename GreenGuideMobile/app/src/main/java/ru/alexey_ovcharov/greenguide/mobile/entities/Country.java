package ru.alexey_ovcharov.greenguide.mobile.entities;

/**
 * Created by Алексей on 26.04.2017.
 */

@Entity
public class Country {
    private int idCountry;
    private String countryName;
    public static final String TABLE_NAME = "countries";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String ID_COUNTRY_COLUMN = "id_country";
    public static final String COUNTRY_COLUMN = "country";
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME + " ("
            + ID_COUNTRY_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            COUNTRY_COLUMN + " VARCHAR (50) NOT NULL UNIQUE);";

    public Country() {
    }

    public Country(int idCountry, String countryName) {
        this.idCountry = idCountry;
        this.countryName = countryName;
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