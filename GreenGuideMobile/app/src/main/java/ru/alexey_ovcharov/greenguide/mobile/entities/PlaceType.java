package ru.alexey_ovcharov.greenguide.mobile.entities;

/**
 * Created by Алексей on 23.04.2017.
 */
@Entity
public class PlaceType {
    private int idPlaceType;
    private String type;
    public static final String TABLE_NAME = "place_types";
    public static final String ID_PLACE_TYPE_COLUMN = "id_place_type";
    public static final String TYPE_COLUMN = "type";
    public static final String DROP_SCRIPT = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String CREATE_SCRIPT = "CREATE TABLE " + TABLE_NAME +
            "(" + ID_PLACE_TYPE_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            TYPE_COLUMN + " VARCHAR (40) NOT NULL);";

    public PlaceType(int idPlaceType, String type) {
        this.idPlaceType = idPlaceType;
        this.type = type;
    }

    public int getIdPlaceType() {
        return idPlaceType;
    }

    public void setIdPlaceType(int idPlaceType) {

        this.idPlaceType = idPlaceType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
