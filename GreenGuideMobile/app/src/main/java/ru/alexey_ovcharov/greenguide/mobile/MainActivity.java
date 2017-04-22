package ru.alexey_ovcharov.greenguide.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ru.alexey_ovcharov.greenguide.mobile.R;

public class MainActivity extends Activity{

    Button bExit;

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
    }
}
