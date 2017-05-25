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
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

public class CategoriesOfPlacesActivity extends Activity {

    private class ListViewPlaceTypesOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(placeTypes != null && position >= 0 && position < placeTypes.size()){
                PlaceType placeType = placeTypes.get(position);
                int idPlaceType = placeType.getIdPlaceType();
                String placeTypeName = placeType.getType();
                Intent intent = new Intent(CategoriesOfPlacesActivity.this,
                        PlacesListInChosenCategoryActivity.class);
                intent.putExtra(PlaceType.ID_PLACE_TYPE_COLUMN, idPlaceType);
                intent.putExtra(PlaceType.TYPE_COLUMN, placeTypeName);
                startActivity(intent);
            }
        }
    }

    private ListView lvPlaceTypes;
    private DbHelper dbHelper;
    private List<PlaceType> placeTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_categories);
        lvPlaceTypes = (ListView)findViewById(R.id.aPlacesCategories_lvCategories);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        placeTypes = dbHelper.getPlacesTypesSorted();
        List<String> placeTypeNames = Commons.listToStringArray(placeTypes, new Mapper<PlaceType>() {
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
