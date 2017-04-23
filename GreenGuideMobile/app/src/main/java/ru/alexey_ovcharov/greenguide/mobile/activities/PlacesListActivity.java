package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.entities.Place;

public class PlacesListActivity extends Activity {

    private int placeTypeId;
    private ListView lvPlaces;
    private DbHelper dbHelper = DbHelper.getInstance();
    private List<Place> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);
        Intent intent = getIntent();
        placeTypeId = intent.getIntExtra(Commons.PLACE_TYPE_ID, -1);
        Toast.makeText(this, String.valueOf(placeTypeId), Toast.LENGTH_SHORT).show();
        lvPlaces = (ListView) findViewById(R.id.aPlacesList_lvPlaces);
        places = dbHelper.getPlacesByType(placeTypeId);
        String[] placeAddresses = Commons.listToStringArray(places, new Mapper<Place>() {
            @Override
            public String map(Place item) {
                return item.getAddress();
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, placeAddresses);
        lvPlaces.setAdapter(adapter);
    }
}
