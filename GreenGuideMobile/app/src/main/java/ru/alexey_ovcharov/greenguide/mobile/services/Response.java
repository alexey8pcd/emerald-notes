package ru.alexey_ovcharov.greenguide.mobile.services;

/**
 * Created by Admin on 28.05.2017.
 */

public class Response {
    private final InteractStatus networkStatus;
    private String data;

    public Response(InteractStatus networkStatus) {
        this.networkStatus = networkStatus;
    }

    public InteractStatus getNetworkStatus() {
        return networkStatus;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
