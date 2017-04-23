package ru.alexey_ovcharov.greenguide.mobile.entities;

/**
 * Created by Алексей on 23.04.2017.
 */

public class PlaceType {
    private int idPlaceType;
    private String type;

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
