package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;

public class PlacesActivity extends Activity {

    private Button bShowList;
    private Button bNewCategory;
    private DbHelper dbHelper;
    private TextView tvPlacesInfo;
    private static final int REQUEST_CODE_NEW_CATEGORY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        dbHelper = new DbHelper(getApplicationContext());
        bShowList = (Button) findViewById(R.id.aPlaces_bAsList);
        bShowList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, PlacesCategoriesActivity.class);
                startActivity(intent);
            }
        });
        bNewCategory = (Button) findViewById(R.id.aPlaces_bNewCategory);
        bNewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, DialogActivity.class);
                intent.putExtra(Commons.DIALOG_TITLE, "Новая категория мест");

                startActivityForResult(intent, REQUEST_CODE_NEW_CATEGORY);
            }
        });
        tvPlacesInfo = (TextView) findViewById(R.id.aPlaces_tvPlacesInfo);
        AsyncTask<Void, Void, Void> placesInfoLoadTask = new AsyncTask<Void, Void, Void>() {
            @SuppressWarnings("WrongThread")
            @Override
            protected Void doInBackground(Void... params) {
                int placesCount = 0;
                try {
                    placesCount = dbHelper.getPlacesCount();
                    String text = "Всего мест в базе: " + placesCount;
                    tvPlacesInfo.setText(text);
                } catch (PersistenceException e) {
                    tvPlacesInfo.setText("Не удалсь получить количество мест из базы");
                    e.log();
                }
                return null;
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        } else {
            if (requestCode == REQUEST_CODE_NEW_CATEGORY) {
                Bundle extras = data.getExtras();
                String categoryName = data.getStringExtra(Commons.DIALOG_RESULT);
                if (Commons.isNotEmpty(categoryName))
                    saveCategory(categoryName);

            } else {
                Toast.makeText(this, "Категория не создана", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveCategory(final String categoryName) {
        AsyncTask<Void, Void, Void> saveCategoryTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    dbHelper.addPlaceType(categoryName);
                } catch (PersistenceException e) {
                    e.log();
                }
                return null;
            }
        }.execute();
    }

}
