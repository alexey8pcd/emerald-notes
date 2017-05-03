package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;

public class PlacesActivity extends Activity {

    private Button bShowList;
    private Button bNewCategory;
    private DbHelper dbHelper;
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
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
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
