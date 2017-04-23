package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ru.alexey_ovcharov.greenguide.mobile.R;

public class PlacesActivity extends Activity {

    private Button bShowList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        bShowList = (Button)findViewById(R.id.aPlaces_bAsList);
        bShowList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, PlacesCategoriesActivity.class);
                startActivity(intent);
            }
        });
    }
}
