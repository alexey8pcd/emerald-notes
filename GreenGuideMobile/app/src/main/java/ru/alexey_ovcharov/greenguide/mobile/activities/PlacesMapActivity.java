package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PlacesMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private DbHelper dbHelper;
    private int mapOpenType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_map);
        Intent intent = getIntent();
        mapOpenType = intent.getIntExtra(Commons.MAP_OPEN_TYPE, -1);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Log.d(APP_NAME, "Начинаю загрузку карты");
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(APP_NAME, "Карта загрузилась");
        this.googleMap = googleMap;
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(APP_NAME, "Пользователь выбрал координаты на карте: " + latLng);
                if (mapOpenType == Commons.OPEN_TYPE_CHOOSE_LOCATION && latLng != null) {
                    Intent intent = new Intent();
                    intent.putExtra(Place.LATITUDE_COLUMN, latLng.latitude);
                    intent.putExtra(Place.LONGITUDE_COLUMN, latLng.longitude);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        this.googleMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                Toast.makeText(getApplicationContext(), "Clicked: " +
                                poi.name + "\nPlace ID:" + poi.placeId +
                                "\nLatitude:" + poi.latLng.latitude +
                                " Longitude:" + poi.latLng.longitude,
                        Toast.LENGTH_SHORT).show();
            }
        });
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(PlacesMapActivity.this,
                        String.format("Lt: %f, Ln: %f", latLng.latitude, latLng.longitude),
                        Toast.LENGTH_SHORT).show();
            }
        });
        try {
            List<PlaceType> placesTypes = dbHelper.getPlacesTypes();
            for (PlaceType placeType : placesTypes) {
                List<Place> placesByType = dbHelper.getPlacesByType(placeType.getIdPlaceType(), false);
                for (Place place : placesByType) {
                    LatLng latLng = place.getLocation();
                    if (latLng != null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        float color = (placeType.getIdPlaceType() * 20) % 360;
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
                        markerOptions.position(latLng).title(place.getDescription());
                        this.googleMap.addMarker(markerOptions);
                    }
                }
            }
            LatLng lastKnownCoordinates = dbHelper.getLastKnownCoordinates();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(lastKnownCoordinates);
            this.googleMap.moveCamera(cameraUpdate);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(11);
            this.googleMap.animateCamera(zoom);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }

    }
}
