package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PlacesListInChoosenCategoryActivity extends Activity {

    private static final int ADD_PLACE_REQUEST = 1;

    private class ButtonAddPlaceOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intentAddPlace = new Intent(PlacesListInChoosenCategoryActivity.this, AddPlaceActivity.class);
            intentAddPlace.putExtra(Commons.PLACE_TYPE_ID, placeTypeId);
            startActivityForResult(intentAddPlace, ADD_PLACE_REQUEST);
        }
    }

    private int placeTypeId;
    private ListView lvPlaces;
    private DbHelper dbHelper;
    private List<Place> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);
        final Intent intent = getIntent();
        dbHelper = new DbHelper(getApplicationContext());

        String placeTypeName = intent.getStringExtra(PlaceType.TYPE_COLUMN);
        TextView tvTitle = (TextView) findViewById(R.id.aPlacesList_tvTitle);
        tvTitle.setText("Просмотр мест в категории " + placeTypeName);

        lvPlaces = (ListView) findViewById(R.id.aPlacesList_lvPlaces);
        lvPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showPlaceIntent = new Intent(PlacesListInChoosenCategoryActivity.this, ShowPlaceActivity.class);
                showPlaceIntent.putExtra(Commons.PLACE_ID, places.get(position).getIdPlace());
                startActivity(showPlaceIntent);
            }
        });
        placeTypeId = intent.getIntExtra(PlaceType.ID_PLACE_TYPE_COLUMN, -1);
        updatePlacesList();

        Button addPlace = (Button) findViewById(R.id.aPlacesList_bAddPlace);
        addPlace.setOnClickListener(new ButtonAddPlaceOnClickListener());
    }

    private void updatePlacesList() {
        try {
            places = dbHelper.getPlacesByType(placeTypeId, false);
            String[] placeAddresses = Commons.listToStringArray(places, new Mapper<Place>() {
                @Override
                public String map(Place item) {
                    String address = item.getAddress();
                    if (address != null) {
                        return item.getDescription() + ", " + address;
                    } else {
                        return item.getDescription();
                    }
                }
            });
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, placeAddresses);
            lvPlaces.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(APP_NAME, e.toString(), e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PLACE_REQUEST) {
            updatePlacesList();
        }
    }
}
