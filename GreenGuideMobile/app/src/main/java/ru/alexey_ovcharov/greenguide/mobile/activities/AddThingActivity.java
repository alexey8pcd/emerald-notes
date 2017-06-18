package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Thing;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class AddThingActivity extends Activity {

    private static final int CAMERA_REQUEST = 89;
    private static final int PICK_IMAGE_REQUEST = 88;
    private int idCategory;
    private DbHelper dbHelper;
    private EditText etName;
    private EditText etDesc;
    private Spinner spCountries;
    private final List<String> countries = new CopyOnWriteArrayList<>();
    private EditText etDecomposition;
    private SeekBar sbDanger;
    private Uri tempImageUri;
    private ImageView imageView;
    private String selectedImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_add_thing);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        Intent intent = getIntent();
        idCategory = intent.getIntExtra(Thing.ID_CATEGORY_COLUMN, -1);

        etName = (EditText) findViewById(R.id.aAddThing_etName);
        etDesc = (EditText) findViewById(R.id.aAddThing_etDesc);
        etDecomposition = (EditText) findViewById(R.id.aAddThing_etDestrTime);
        spCountries = (Spinner) findViewById(R.id.aAddThing_spCountry);
        spCountries.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, countries));
        final TextView tvDangerLabel = (TextView) findViewById(R.id.aAddThing_tvDanger);

        imageView = (ImageView) findViewById(R.id.aAddThing_ivImage);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        sbDanger = (SeekBar) findViewById(R.id.aAddThing_sbDanger);
        sbDanger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String label = Thing.DANGER_LABELS[progress];
                tvDangerLabel.setText(label);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        fillCountriesAsync();

        Button bAdd = (Button) findViewById(R.id.aAddThing_bAdd);
        bAdd.setOnClickListener(new ChooseImagesDialogMaker());

        Button bCreate = (Button) findViewById(R.id.aAddThing_bCreate);
        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToDb();
            }
        });
    }

    private void fillCountriesAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Map<Integer, String> countriesMap = dbHelper.getCountriesSorted();
                    synchronized (countries) {
                        countries.clear();
                        countries.add("Не задано");
                        countries.addAll(countriesMap.values());
                    }
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ((BaseAdapter) spCountries.getAdapter()).notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                if (requestCode == PICK_IMAGE_REQUEST) {
                    if (data != null) {
                        Uri imageUrl = data.getData();
                        selectedImageURI = imageUrl.toString();
                        Log.d(APP_NAME, "Выбрано изображение на устройстве: "
                                + selectedImageURI);
                        imageView.setImageURI(imageUrl);
                    }
                } else if (requestCode == CAMERA_REQUEST) {
                    if (tempImageUri != null) {
                        selectedImageURI = tempImageUri.toString();
                        Log.d(APP_NAME, "Сделан снимок с камеры: " + selectedImageURI);
                        imageView.setImageURI(tempImageUri);
                    }
                }

            } catch (Exception e) {
                Log.e(APP_NAME, e.toString(), e);
            }
        }

    }

    private void addToDb() {
        try {
            String name = etName.getText().toString();
            String description = etDesc.getText().toString();
            if (StringUtils.isNotBlank(selectedImageURI)) {
                if (StringUtils.isNoneBlank(name, description)) {
                    Thing thing = new Thing();
                    thing.setName(name);
                    thing.setDescription(description);
                    thing.setIdCategory(idCategory);

                    int selectedCountry = spCountries.getSelectedItemPosition();
                    Integer idCountry = null;
                    if (selectedCountry >= 0 && selectedCountry < countries.size()) {
                        if (selectedCountry > 0) {
                            String country = countries.get(selectedCountry);
                            idCountry = getCountryByName(country);
                        }
                    }
                    thing.setIdCountry(idCountry);

                    Editable text = etDecomposition.getText();
                    Integer decompositionTime = null;
                    if (StringUtils.isNotBlank(text)) {
                        decompositionTime = NumberUtils.createInteger(text.toString());
                    }

                    thing.setDecompositionTime(decompositionTime);
                    int dangerForEnv = sbDanger.getProgress();
                    thing.setDangerForEnvironment(dangerForEnv);

                    Image image = dbHelper.addImage(selectedImageURI);
                    thing.addImage(image);
                    dbHelper.addThing(thing);
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(this, "Название и описание должно быть указано",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Изображение не выбрано", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e(APP_NAME, ex.toString(), ex);
            setResult(Activity.RESULT_FIRST_USER);
        }
        finish();
    }

    private Integer getCountryByName(String country) throws PersistenceException {
        Map<Integer, String> countriesSorted = dbHelper.getCountriesSorted();
        Map<String, Integer> countriesMapInverted = Commons.invertMap(countriesSorted);
        return countriesMapInverted.get(country);
    }

    private class ChooseImagesDialogMaker implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder ad = new AlertDialog.Builder(AddThingActivity.this);
            ad.setTitle("Выбор источника");
            ad.setMessage("Откуда требуется получить изображение?");
            ad.setPositiveButton("Снимок с камеры", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    File photo = Commons.getTempPhotoFile();
                    if (photo != null) {
                        tempImageUri = Uri.fromFile(photo);
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    } else {
                        Toast.makeText(AddThingActivity.this, "Не удалось создать изображения, " +
                                        "возможно нет доступа к карте памяти!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            ad.setNegativeButton("Изображение из галереи", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    Intent intent;
                    if (Build.VERSION.SDK_INT < 19) {
                        intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    } else {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }
                }
            });
            ad.setCancelable(true);
            ad.show();
        }
    }
}
