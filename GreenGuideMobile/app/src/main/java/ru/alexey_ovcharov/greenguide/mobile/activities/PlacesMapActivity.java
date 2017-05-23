package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PlacesMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private DbHelper dbHelper;
    private int mapOpenType;
    private Map<Marker, Place> markerPlaceMap = new HashMap<>();

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
        markerPlaceMap.clear();
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
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(PlacesMapActivity.this,
                        String.format("Lt: %f, Ln: %f", latLng.latitude, latLng.longitude),
                        Toast.LENGTH_SHORT).show();
            }
        });
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                } else {
                    marker.showInfoWindow();
                }
                return true;
            }
        });
        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Place place = markerPlaceMap.get(marker);
                Intent intent = new Intent(PlacesMapActivity.this, ShowPlaceActivity.class);
                intent.putExtra(Place.ID_PLACE_COLUMN, place.getIdPlace());
                startActivity(intent);
            }
        });
        this.googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context context = PlacesMapActivity.this;

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(PlacesMapActivity.this);
                title.setText(StringUtils.left(marker.getTitle(), 20));
                title.setTextColor(Color.BLACK);

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());
                /*
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setMaxWidth(80);
                imageView.setMaxHeight(80);
                try {
                    Place place = markerPlaceMap.get(marker);
                    Integer idImage = place.getImagesIds().get(0);
                    List<Image> imageData = dbHelper.getImageData(Arrays.asList(idImage));
                    Image image = imageData.get(0);
                    String url = image.getUrl();
                    ContentResolver contentResolver = getContentResolver();
                    Uri parsedUri = Uri.parse(url);
                    InputStream inputStream = contentResolver.openInputStream(parsedUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 80, 80, false));
                    info.addView(imageView);
                } catch (Exception ex) {
                    Log.e(APP_NAME, ex.toString(), ex);
                }
*/
                info.addView(title);
                info.addView(snippet);
                return info;
            }

        });
        try {
            List<PlaceType> placesTypes = dbHelper.getPlacesTypes();
            int dSize = 360 / (placesTypes.size() + 1);
            int index = 0;
            for (PlaceType placeType : placesTypes) {
                float color = (index++ * dSize) % 360;
                List<Place> placesByType = dbHelper.getPlacesByType(placeType.getIdPlaceType(), true);
                for (Place place : placesByType) {
                    LatLng latLng = place.getLocation();
                    if (latLng != null) {
                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
                        markerOptions.position(latLng);
                        markerOptions.title(place.getDescription());
                        String address = StringUtils.isNotBlank(place.getAddress())
                                ? "Адрес: " + place.getAddress() + "\n" : "";
                        markerOptions.snippet(address + "Категория: " + placeType.getType() + "\n"
                                + place.getDescription());
                        Marker marker = this.googleMap.addMarker(markerOptions);
                        markerPlaceMap.put(marker, place);
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
