package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;
import ru.alexey_ovcharov.greenguide.mobile.persist.PersistenceException;
import ru.alexey_ovcharov.greenguide.mobile.persist.Place;

public class ShowPlaceActivity extends Activity {

    private class ImageAdapter extends ArrayAdapter<Bitmap> {


        private final Context context;
        private final List<Bitmap> objects;

        public ImageAdapter(@NonNull Context context, @NonNull List<Bitmap> objects) {
            super(context, -1, objects.toArray(new Bitmap[0]));
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.custom_layout, parent, false);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.custom_image);
            imageView.setImageBitmap(objects.get(position));
            return rowView;
        }
    }

    private DbHelper dbHelper;
    private int selectedPlaceId;
    private Place placeWithImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_place);
        Intent intent = getIntent();
        selectedPlaceId = intent.getIntExtra(Commons.PLACE_ID, 0);

        dbHelper = new DbHelper(getApplicationContext());
        GridView gridView = (GridView) findViewById(R.id.aShowPlace_gvImages);

        try {
            placeWithImages = dbHelper.getPlaceWithImages(selectedPlaceId);

            TextView view = (TextView) findViewById(R.id.aShowPlace_tvPlaceDescription);
            view.setText(placeWithImages.getDescription());

            TextView tvAddress = (TextView) findViewById(R.id.aShowPlace_tvAddress);
            String address = placeWithImages.getAddress();
            if (address == null || "null".equals(address) || address.isEmpty()) {
                address = "Адрес не указан";
            }
            tvAddress.setText(address);
        } catch (PersistenceException e) {
            e.log();
        }


        List<Bitmap> images = loadImages();
        gridView.setAdapter(new ImageAdapter(this, images));

    }

    private List<Bitmap> loadImages() {
        if (placeWithImages != null) {
            try {
                return placeWithImages.getImages(getApplicationContext());
            } catch (FileNotFoundException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        return Collections.EMPTY_LIST;
    }

}
