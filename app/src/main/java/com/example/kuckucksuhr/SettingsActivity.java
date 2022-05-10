package com.example.kuckucksuhr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch hourlySwitch, nightmodeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BtService.begin(getApplicationContext());
        BtService.setSettingsActivity(this);
        BtService.connect();

        hourlySwitch = findViewById(R.id.switch4);
        nightmodeSwitch = findViewById(R.id.switch5);
        hourlySwitch.setChecked(ClockService.getHourly());
        nightmodeSwitch.setChecked(ClockService.getNightmode());

        hourlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ClockService.setHourly(true);
                BtService.sendHourly(true);
            } else {
                ClockService.setHourly(false);
                BtService.sendHourly(false);
            }
        });
        nightmodeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ClockService.setNightmode(true);
                BtService.sendNightmode(true);
            } else {
                ClockService.setNightmode(false);
                BtService.sendNightmode(false);
            }
        });
        setConnection(false);
    }

    public void setConnection(boolean state) {
        if (state) {
            hourlySwitch.setClickable(true);
            nightmodeSwitch.setClickable(true);
        } else {
            hourlySwitch.setClickable(false);
            nightmodeSwitch.setClickable(false);
        }
    }
}