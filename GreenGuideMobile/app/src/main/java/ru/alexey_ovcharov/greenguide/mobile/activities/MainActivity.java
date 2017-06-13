package ru.alexey_ovcharov.greenguide.mobile.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ru.alexey_ovcharov.greenguide.mobile.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bExit = (Button) findViewById(R.id.aMain_bExit);
        bExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        Button bPlaces = (Button) findViewById(R.id.aMain_bPlaces);
        bPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlacesActivity.class);
                startActivity(intent);
            }
        });
        Button settings = (Button) findViewById(R.id.aMain_bSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        Button bReferences = (Button) findViewById(R.id.aMain_bReferences);
        bReferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EncyclopediaActivity.class));
            }
        });

        Button bUsefulInformation = (Button) findViewById(R.id.aMain_bNotes);
        bUsefulInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UsefulInformationActivity.class));
            }
        });

        Button bLogin = (Button) findViewById(R.id.aMain_bLogin);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Функция еще не поддерживается", Toast.LENGTH_SHORT).show();
            }
        });

        Button bAbout = (Button) findViewById(R.id.aMain_bAbout);
        bAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                ad.setTitle("О программе \"Зеленый гид\"");
                ad.setMessage("Полезная информация об аспектах «зеленого»\n" +
                        "образа жизни в интерактивном виде, информация о загрязнении окружающей среды.\n" +
                        "\nИнформацию об ошибках присылать на почту: alexey8rus@mail.ru");
                ad.setPositiveButton("Закрыть", null);
                ad.show();
            }
        });
    }

}