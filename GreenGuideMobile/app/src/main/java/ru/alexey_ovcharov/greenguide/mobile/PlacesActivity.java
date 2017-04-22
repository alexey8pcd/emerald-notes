package ru.alexey_ovcharov.greenguide.mobile;

import android.app.Activity;
import android.os.Bundle;

import ru.alexey_ovcharov.greenguide.mobile.R;

public class PlacesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        getActionBar().setTitle(Commons.APP_NAME);
    }
}
