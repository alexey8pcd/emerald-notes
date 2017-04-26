package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.alexey_ovcharov.greenguide.mobile.Commons;
import ru.alexey_ovcharov.greenguide.mobile.R;

public class DialogActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        TextView tvTitle = (TextView)findViewById(R.id.aDialog_tvTitle);
        Intent intent = getIntent();
        String title = intent.getStringExtra(Commons.DIALOG_TITLE);
        tvTitle.setText(title);

        Button bApply = (Button) findViewById(R.id.aDialog_bApply);
        final EditText etValue = (EditText) findViewById(R.id.aDialog_etValue);
        bApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentRes = new Intent();
                String dialogValue = etValue.getText().toString();
                intentRes.putExtra(Commons.DIALOG_RESULT, dialogValue);
                setResult(RESULT_OK, intentRes);
                finish();
            }
        });
    }
}
