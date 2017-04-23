package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ru.alexey_ovcharov.greenguide.mobile.R;

public class MainActivity extends Activity{

    private Button bExit;
    private Button bPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bExit = (Button) findViewById(R.id.aMain_bExit);
        bExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        bPlaces = (Button)findViewById(R.id.aMain_bPlaces);
        bPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlacesActivity.class);
                startActivity(intent);
            }
        });
    }
}
