package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.entities.PlaceType;

public class PlacesCategoriesActivity extends Activity {

    private class ListViewPlaceTypesOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(placeTypes != null && position >= 0 && position < placeTypes.size()){
                int idPlaceType = placeTypes.get(position).getIdPlaceType();
                Intent intent = new Intent(PlacesCategoriesActivity.this, PlacesListActivity.class);
                intent.putExtra(Commons.PLACE_TYPE_ID, idPlaceType);
                startActivity(intent);
            }
        }
    }

    private ListView lvPlaceTypes;
    private DbHelper dbHelper = DbHelper.getInstance();
    private List<PlaceType> placeTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_categories);
        lvPlaceTypes = (ListView)findViewById(R.id.aPlacesCategories_lvCategories);
        placeTypes = dbHelper.getPlacesTypes();
        String[] placeTypeNames = Commons.listToStringArray(placeTypes, new Mapper<PlaceType>() {
            @Override
            public String map(PlaceType item) {
                return item.getType();
            }
        });
        ListAdapter listAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, placeTypeNames);
        lvPlaceTypes.setAdapter(listAdapter);
        lvPlaceTypes.setOnItemClickListener(new ListViewPlaceTypesOnItemClickListener());
    }

}
