package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.services.PublicationService;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.SERVER_URL;

public class PlacesActivity extends Activity {

    private DbHelper dbHelper;
    private TextView tvPlacesInfo;
    private static final int REQUEST_CODE_NEW_CATEGORY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        Button bShowList = (Button) findViewById(R.id.aPlaces_bAsList);
        bShowList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, CategoriesOfPlacesActivity.class);
                startActivity(intent);
            }
        });
        Button bNewCategory = (Button) findViewById(R.id.aPlaces_bNewCategory);
        bNewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCategory();
            }
        });
        Button bPublicAll = (Button) findViewById(R.id.aPlaces_bPublicAll);
        bPublicAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publicPlacesReference();

            }
        });
        Button bPlacesOnMap = (Button) findViewById(R.id.aPlaces_bAsMap);
        bPlacesOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlacesActivity.this, PlacesMapActivity.class));
            }
        });
        tvPlacesInfo = (TextView) findViewById(R.id.aPlaces_tvPlacesInfo);
        showPlacesCountFromDb();
    }

    private void publicPlacesReference() {
        AlertDialog.Builder ad = new AlertDialog.Builder(PlacesActivity.this);
        ad.setTitle("Разрешение на отправку");
        ad.setMessage("Публикация информации о местах может потребовать передачи " +
                "большого объема данных на сервер, продолжить?");
        ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(PlacesActivity.this, "Процесс отправки данных запущен",
                        Toast.LENGTH_LONG).show();
                startPublicationService();
            }
        });
        ad.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                //ничего не делаем
            }
        });
        ad.show();
    }

    private void addNewCategory() {
        AlertDialog.Builder ad = new AlertDialog.Builder(PlacesActivity.this);
        ad.setTitle("Введите название категории");
        final EditText input = new EditText(PlacesActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        ad.setView(input);
        ad.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = input.getText().toString();
                if (StringUtils.isNotEmpty(categoryName)) {
                    saveCategory(categoryName);
                    Toast.makeText(PlacesActivity.this,
                            "Категория добавлена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlacesActivity.this,
                            "Название категории не может быть пустым", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ad.show();
    }

    private void showPlacesCountFromDb() {
        new AsyncTask<Void, Void, Void>() {

            private String text;

            @Override
            protected Void doInBackground(Void... params) {
                DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
                try {
                    int placesCount = dbHelper.getPlacesCount();
                    text = "Всего мест в базе: " + placesCount;

                    if (dbHelper.getSettingByName(SERVER_URL) == null) {
                        dbHelper.putSetting(SERVER_URL, "http://192.168.1.33:8080");
                    }
                } catch (PersistenceException e) {
                    text = "Не удалсь получить количество мест из базы";
                    e.log();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                tvPlacesInfo.setText(text);
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void startPublicationService() {
        startService(new Intent(this, PublicationService.class));
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
