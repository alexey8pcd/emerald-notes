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

        showPlacesCountFromDb();

        tvPlacesInfo = (TextView) findViewById(R.id.aPlaces_tvPlacesInfo);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int placesCount = 0;
                try {
                    placesCount = dbHelper.getPlacesCount();
                    String text = "Всего мест в базе: " + placesCount;
                    tvPlacesInfo.setText(text);
                } catch (PersistenceException e) {
                    tvPlacesInfo.setText("Не удалсь получить количество мест из базы");
                    e.log();
                }
            }
        });
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
                if (Commons.isNotEmpty(categoryName)) {
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

            @Override
            protected Void doInBackground(Void... params) {
                DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
                if (dbHelper.getSettingByName(SERVER_URL) == null) {
                    dbHelper.putSetting(SERVER_URL, "http://192.168.1.33:8080");
                }
                return null;
            }
        }.execute();
    }

    private void startPublicationService() {
        startService(new Intent(this, PublicationService.class));
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
