package com.example.songapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RingtoneActivity extends AppCompatActivity {

    Button btnPlay, btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone);

        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);

        btnPlay.setOnClickListener(v -> {
            startService(new Intent(RingtoneActivity.this, RingtoneService.class));
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(RingtoneActivity.this, RingtoneService.class));
        });
    }
}