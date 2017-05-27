package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;
import ru.alexey_ovcharov.greenguide.mobile.persist.PlaceType;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class PlacesListInChosenCategoryActivity extends Activity {

    private static final int ADD_PLACE_REQUEST = 1;
    private List<String> placeList = new CopyOnWriteArrayList<>();
    private int placeTypeId;
    private ListView lvPlaces;
    private DbHelper dbHelper;
    private List<Place> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);
        final Intent intent = getIntent();
        dbHelper = DbHelper.getInstance(getApplicationContext());

        String placeTypeName = intent.getStringExtra(PlaceType.TYPE_COLUMN);
        TextView tvTitle = (TextView) findViewById(R.id.aPlacesList_tvTitle);
        tvTitle.setText("Просмотр мест в категории " + placeTypeName);

        lvPlaces = (ListView) findViewById(R.id.aPlacesList_lvPlaces);
        lvPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showPlaceIntent = new Intent(PlacesListInChosenCategoryActivity.this,
                        ShowPlaceActivity.class);
                showPlaceIntent.putExtra(Place.ID_PLACE_COLUMN, places.get(position).getIdPlace());
                startActivity(showPlaceIntent);
            }
        });
        lvPlaces.setOnItemLongClickListener(new ListViewPlacesOnItemLongClickListener());
        placeTypeId = intent.getIntExtra(PlaceType.ID_PLACE_TYPE_COLUMN, -1);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, placeList);
        lvPlaces.setAdapter(adapter);

        SearchView svPlaceName = (SearchView) findViewById(R.id.aPlacesList_svFindPlace);
        svPlaceName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (StringUtils.isNotEmpty(query)) {
                    adapter.getFilter().filter(query);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isBlank(newText)) {
                    adapter.getFilter().filter("");
                    return true;
                } else {
                    return false;
                }
            }
        });

        Button addPlace = (Button) findViewById(R.id.aPlacesList_bAddPlace);
        addPlace.setOnClickListener(new ButtonAddPlaceOnClickListener());

        updatePlacesListAsync();
    }

    private void updatePlacesListAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    places = dbHelper.getPlacesByType(placeTypeId, false);
                    List<String> placeListLocal = Commons.listToStringArray(places, new Mapper<Place>() {
                        @Override
                        public String map(Place item) {
                            return item.getDescription();
                        }
                    });
                    placeList.clear();
                    placeList.addAll(placeListLocal);
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ((BaseAdapter) lvPlaces.getAdapter()).notifyDataSetChanged();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private class ButtonAddPlaceOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intentAddPlace = new Intent(PlacesListInChosenCategoryActivity.this, AddPlaceActivity.class);
            intentAddPlace.putExtra(PlaceType.ID_PLACE_TYPE_COLUMN, placeTypeId);
            startActivityForResult(intentAddPlace, ADD_PLACE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PLACE_REQUEST) {
            updatePlacesListAsync();
        }
    }

    private class ListViewPlacesOnItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlertDialog.Builder ad = new AlertDialog.Builder(PlacesListInChosenCategoryActivity.this);
            ad.setTitle("Удаление места");
            ad.setMessage("Удалить выбранное место?");
            ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    int idPlace = places.get(position).getIdPlace();
                    try {
                        dbHelper.deletePlaceById(idPlace);
                    } catch (Exception ex) {
                        Log.e(APP_NAME, ex.toString(), ex);
                    }
                    updatePlacesListAsync();
                }
            });
            ad.setNegativeButton("Нет", null);
            ad.show();
            return true;
        }
    }
}
