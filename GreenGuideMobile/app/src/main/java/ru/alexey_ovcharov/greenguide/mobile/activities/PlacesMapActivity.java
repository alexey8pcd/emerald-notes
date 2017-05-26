package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PlacesMapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final Comparator<PlaceType> PLACE_TYPE_COMPARATOR = new Comparator<PlaceType>() {
        @Override
        public int compare(PlaceType o1, PlaceType o2) {
            return o1.getType().compareTo(o2.getType());
        }
    };
    public static final int ALL_CATEGORIES_INDEX = Integer.MIN_VALUE;
    public static final int ALL_CATEGORIES_POSITION = 0;
    private static final int CREATE_PLACE_REQUEST = 1;
    private GoogleMap googleMap;
    private DbHelper dbHelper;
    private int mapOpenType;
    private Map<Marker, Place> markerPlaceMap = new HashMap<>();
    private List<PlaceType> placesTypes;
    private Spinner spCategory;
    private String searchedPlaceName;
    private int chosenPlaceTypeIndex = ALL_CATEGORIES_INDEX;
    private List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_map);
        Intent intent = getIntent();
        mapOpenType = intent.getIntExtra(Commons.MAP_OPEN_TYPE, -1);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapOpenType == Commons.OPEN_TYPE_CHOOSE_LOCATION) {
            LinearLayout menuLayout = (LinearLayout) findViewById(R.id.aPlacesMap_llMain);
            menuLayout.setVisibility(View.INVISIBLE);
        } else {
            spCategory = (Spinner) findViewById(R.id.aPlacesMap_spCategories);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCategory.setAdapter(adapter);
            spCategory.setOnItemSelectedListener(new SpinnerCategoriesOnItemSelectedListener());

            final SearchView svCategory = (SearchView) findViewById(R.id.aPlacesMap_svCategory);
            svCategory.setOnQueryTextListener(new SearchViewOnQueryTextListener(svCategory));
        }
        Log.d(APP_NAME, "Начинаю загрузку карты");
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(APP_NAME, "Карта загрузилась");
        this.googleMap = googleMap;
        markerPlaceMap.clear();
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                if (mapOpenType != Commons.OPEN_TYPE_CHOOSE_LOCATION) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(PlacesMapActivity.this);
                    ad.setTitle("Добавление места");
                    ad.setMessage("Создать запись о данном месте?");
                    ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            Intent intent = new Intent(PlacesMapActivity.this, AddPlaceActivity.class);
                            intent.putExtra(Place.LATITUDE_COLUMN, latLng.latitude);
                            intent.putExtra(Place.LONGITUDE_COLUMN, latLng.longitude);
                            startActivityForResult(intent, CREATE_PLACE_REQUEST);
                        }
                    });
                    ad.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            //ничего не делаем
                        }
                    });
                    ad.show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Place.LATITUDE_COLUMN, latLng.latitude);
                    intent.putExtra(Place.LONGITUDE_COLUMN, latLng.longitude);
                    setResult(RESULT_OK, intent);
                    finish();
                }

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
        this.googleMap.setInfoWindowAdapter(new MapInfoWindowAdapter());
        if (mapOpenType != Commons.OPEN_TYPE_CHOOSE_LOCATION) {
            fillMapAsync();
        }
        LatLng lastKnownCoordinates = dbHelper.getLastKnownCoordinates();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(lastKnownCoordinates);
        this.googleMap.moveCamera(cameraUpdate);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(11);
        this.googleMap.animateCamera(zoom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_PLACE_REQUEST && resultCode == RESULT_OK) {
            fillMapAsync();
        }
    }

    private void fillMapAsync() {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            private Map<MarkerOptions, Place> markerOptionsList = new HashMap<>();
            private LatLng lastKnownCoordinates;
            private List<String> categoriesLocal;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    placesTypes = dbHelper.getPlacesTypesSorted();
                    Collections.sort(placesTypes, PLACE_TYPE_COMPARATOR);
                    categoriesLocal = Commons.listToStringArray(placesTypes, new Mapper<PlaceType>() {
                        @Override
                        public String map(PlaceType item) {
                            return item.getType();
                        }
                    });
                    categoriesLocal.add(ALL_CATEGORIES_POSITION, "Все категории");

                    lastKnownCoordinates = dbHelper.getLastKnownCoordinates();
                    markerOptionsList.clear();
                    int dSize = 360 / (placesTypes.size() + 1);
                    int index = 0;
                    for (PlaceType placeType : placesTypes) {
                        if (chosenPlaceTypeIndex == ALL_CATEGORIES_INDEX
                                || placeType.getIdPlaceType() == chosenPlaceTypeIndex) {
                            float color = (index++ * dSize) % 360;
                            List<Place> placesByType = dbHelper.getPlacesByType(placeType.getIdPlaceType(), true);
                            for (Place place : placesByType) {
                                LatLng latLng = place.getLocation();
                                if (latLng != null) {
                                    String description = place.getDescription();
                                    if (StringUtils.isEmpty(searchedPlaceName)
                                            || StringUtils.containsIgnoreCase(description, searchedPlaceName)) {
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
                                        markerOptions.position(latLng);
                                        markerOptions.title(description);
                                        String address = StringUtils.isNotBlank(place.getAddress())
                                                ? "Адрес: " + place.getAddress() + "\n" : "";
                                        markerOptions.snippet(address + "Категория: " + placeType.getType() + "\n"
                                                + description);
                                        markerOptionsList.put(markerOptions, place);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    if (categoriesLocal != null) {
                        synchronized (this) {
                            categories.clear();
                            categories.addAll(categoriesLocal);
                        }
                        ((BaseAdapter) spCategory.getAdapter()).notifyDataSetChanged();
                    }
                    googleMap.clear();
                    googleMap.addCircle(new CircleOptions().center(lastKnownCoordinates)
                            .clickable(false).strokeColor(Color.BLUE).radius(35));
                    for (Map.Entry<MarkerOptions, Place> entry : markerOptionsList.entrySet()) {
                        Marker marker = googleMap.addMarker(entry.getKey());
                        markerPlaceMap.put(marker, entry.getValue());
                    }
                    super.onPostExecute(aVoid);
                } catch (Exception ex) {
                    Log.e(APP_NAME, ex.toString(), ex);
                }

            }
        }.execute();
    }

    private class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
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
            info.addView(title);
            info.addView(snippet);
            return info;
        }

    }

    private class SearchViewOnQueryTextListener implements SearchView.OnQueryTextListener {
        private final SearchView svCategory;

        public SearchViewOnQueryTextListener(SearchView svCategory) {
            this.svCategory = svCategory;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchedPlaceName = query;
            fillMapAsync();
            svCategory.clearFocus();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (StringUtils.isEmpty(newText)) {
                searchedPlaceName = null;
                fillMapAsync();
                svCategory.clearFocus();
                return true;
            } else {
                return false;
            }
        }
    }

    private class SpinnerCategoriesOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position != ALL_CATEGORIES_POSITION) {
                chosenPlaceTypeIndex = placesTypes.get(position - 1).getIdPlaceType();
                fillMapAsync();
            } else {
                chosenPlaceTypeIndex = ALL_CATEGORIES_INDEX;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            chosenPlaceTypeIndex = ALL_CATEGORIES_INDEX;
            fillMapAsync();
        }
    }
}
