package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.Image;
import ru.alexey_ovcharov.greenguide.mobile.persist.Thing;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;

public class ShowItemsActivity extends Activity {

    private static final int ADD_REQUEST = 1;
    private List<Thing> thingList = new ArrayList<>();
    private DbHelper dbHelper;
    private int idCategory;
    private ListView lvItems;
    private volatile Map<Integer, String> countries = Collections.EMPTY_MAP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_items);
        Intent intent = getIntent();
        idCategory = intent.getIntExtra(Thing.ID_CATEGORY_COLUMN, 0);

        Button bAdd = (Button) findViewById(R.id.aShowItem_bAdd);
        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ShowItemsActivity.this, AddThingActivity.class);
                in.putExtra(Thing.ID_CATEGORY_COLUMN, idCategory);
                startActivityForResult(in, ADD_REQUEST);
            }
        });

        lvItems = (ListView) findViewById(R.id.aShowItems_lvItems);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        lvItems.setAdapter(new ThingArrayAdapter(this, thingList));
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ShowItemsActivity.this);
                ad.setTitle("Удаление объекта");
                ad.setMessage("Удалить выбранной объект?");
                ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        int idThing = thingList.get(position).getIdThing();
                        try {
                            dbHelper.deleteThingById(idThing);
                            Toast.makeText(ShowItemsActivity.this, "Удалено успещно", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Log.e(APP_NAME, ex.toString(), ex);
                        }
                        fillItemsAsync();
                    }
                });
                ad.setNegativeButton("Нет", null);
                ad.show();
                return true;
            }
        });
        fillItemsAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_REQUEST && resultCode == RESULT_OK) {
            fillItemsAsync();
        }
    }

    private void fillItemsAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    List<Thing> things = dbHelper.getThingsByCategory(idCategory);
                    synchronized (thingList) {
                        thingList.clear();
                        thingList.addAll(things);
                    }
                    countries = dbHelper.getCountriesSorted();
                } catch (Exception e) {
                    Log.e(APP_NAME, e.toString(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ((BaseAdapter) lvItems.getAdapter()).notifyDataSetChanged();
            }
        }.execute();
    }

    private class ThingArrayAdapter extends BaseAdapter {

        private Context context;
        private List<Thing> thingList;
        private LayoutInflater inflater;

        public ThingArrayAdapter(Context context, List<Thing> thingList) {
            this.context = context;
            this.thingList = thingList;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return thingList.size();
        }

        @Override
        public Object getItem(int position) {
            return thingList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null) {
                vi = inflater.inflate(R.layout.custom_layout2, null);
            }
            LinearLayout itemLayout = (LinearLayout) vi.findViewById(R.id.item_row);

            Thing thing = thingList.get(position);
            Image image = thing.getFirstImage();

            ImageView imageView = (ImageView) itemLayout.findViewById(R.id.ir_image);
            try {
                imageView.setImageURI(Uri.parse(image.getUrl()));
            } catch (Exception e) {
                Log.e(APP_NAME, e.toString(), e);
            }

            TextView tvTitle = (TextView) itemLayout.findViewById(R.id.ir_title);
            tvTitle.setText(thing.getName());


            TextView tvDescr = (TextView) itemLayout.findViewById(R.id.ir_description);
            tvDescr.setText(thing.getDescription());

            TextView countryManufact = (TextView) itemLayout.findViewById(R.id.ir_country);
            int idCountry = thing.getIdCountry();
            String countryName = countries.get(idCountry);
            countryManufact.setText(StringUtils.defaultString(countryName));
            TextView tvDanger = (TextView) itemLayout.findViewById(R.id.ir_dangerous_for_environment);
            tvDanger.setText(Thing.DANGER_LABELS[thing.getDangerForEnvironment()]);
            TextView tvDesctruct = (TextView) itemLayout.findViewById(R.id.ir_destruction_time);
            Integer decompositionTime = thing.getDecompositionTime();
            String decompTimeStr = "";
            if (decompositionTime != null) {
                int years = decompositionTime / 12;
                int month = decompositionTime % 12;
                if (years != 0) {
                    decompTimeStr += "лет: " + years;
                }
                if (month != 0) {
                    if (years != 0) {
                        decompTimeStr += ", ";
                    }
                    decompTimeStr += "месяцев: " + month;
                }
            } else {
                decompTimeStr = "нет данных";
            }
            tvDesctruct.setText(decompTimeStr);
            return vi;
        }
    }
}
