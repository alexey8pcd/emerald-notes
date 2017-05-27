package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.Mapper;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.CategoryOfThing;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class ReferencesActivity extends Activity {

    private DbHelper dbHelper;
    private ListView lvCategories;
    private List<String> referencesList = new CopyOnWriteArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_references);
        dbHelper = DbHelper.getInstance(this);
        Button bAddReference = (Button) findViewById(R.id.aRefer_bAddRefer);
        bAddReference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCategory();
            }
        });

        lvCategories = (ListView) findViewById(R.id.aRefer_lvReferences);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, referencesList);
        lvCategories.setAdapter(adapter);
        getCategoriesAsync();
    }

    private void getCategoriesAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    List<CategoryOfThing> allCategoryOfThings = dbHelper.getAllCategoryOfThingsSorted();
                    List<String> categoryNames = Commons.listToStringArray(allCategoryOfThings, new Mapper<CategoryOfThing>() {
                        @Override
                        public String map(CategoryOfThing item) {
                            return item.getCategory();
                        }
                    });
                    referencesList.clear();
                    referencesList.addAll(categoryNames);
                } catch (Exception ex) {
                    Log.e(APP_NAME, ex.toString(), ex);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ((BaseAdapter) lvCategories.getAdapter()).notifyDataSetChanged();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void addNewCategory() {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("Введите название раздела");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        ad.setView(input);
        ad.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String refName = input.getText().toString();
                if (StringUtils.isNotEmpty(refName)) {
                    saveRef(refName);
                    getCategoriesAsync();
                    Toast.makeText(ReferencesActivity.this,
                            "Раздел добавлен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReferencesActivity.this,
                            "Название раздела не может быть пустым", Toast.LENGTH_SHORT).show();
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

    private void saveRef(String refName) {
        try {
            dbHelper.saveCategoryOfThing(refName);
        } catch (Exception ex) {
            Log.e(APP_NAME, ex.toString(), ex);
        }
    }
}
