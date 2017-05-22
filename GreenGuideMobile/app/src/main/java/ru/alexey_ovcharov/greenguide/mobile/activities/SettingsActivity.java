package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import ru.alexey_ovcharov.greenguide.mobile.R;
import ru.alexey_ovcharov.greenguide.mobile.persist.DbHelper;

import static ru.alexey_ovcharov.greenguide.mobile.Commons.APP_NAME;
import static ru.alexey_ovcharov.greenguide.mobile.Commons.SERVER_URL;

public class SettingsActivity extends Activity {

    private DbHelper dbHelper;
    private EditText etServerUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dbHelper = DbHelper.getInstance(getApplicationContext());
        etServerUrl = (EditText) findViewById(R.id.aSettings_etServerAddress);

        String serverUrl = dbHelper.getSettingByName(SERVER_URL);
        if (serverUrl != null) {
            etServerUrl.setText(serverUrl);
        }

        Button bApply = (Button) findViewById(R.id.aSettings_bApply);
        bApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverUrlNew = etServerUrl.getText().toString();
                if (serverUrlNew != null) {
                    Log.d(APP_NAME, "Изменился URL сервера: " + serverUrlNew);
                    dbHelper.putSetting(SERVER_URL, serverUrlNew);
                }
                finish();
            }
        });
    }
}
