package ru.alexey_ovcharov.greenguide.mobile.entities;


/**
 * Created by Алексей on 23.04.2017.
 */

public class Place {
    private int idPlace;
    private String description;
    private String address;
    private int idCountry;
    private int idPlaceType;

    public Place() {

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

    public int getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(int idCountry) {
        this.idCountry = idCountry;
    }

    public int getIdPlaceType() {
        return idPlaceType;
    }

    public void setIdPlaceType(int idPlaceType) {
        this.idPlaceType = idPlaceType;
    }
}
