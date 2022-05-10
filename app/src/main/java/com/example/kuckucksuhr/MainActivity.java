package com.example.kuckucksuhr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch alarmSwitch1, alarmSwitch2;
    private Button syncButton;
    private TextView textViewTime, textViewDate, textViewAlarm1, textViewAlarm2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(getApplicationContext(), BtService.class));
        startService(new Intent(getApplicationContext(), ClockService.class));

        BtService.begin(getApplicationContext());
        BtService.setMainActivity(this);
        BtService.connect();

        textViewTime = findViewById(R.id.textViewTime);
        textViewDate = findViewById(R.id.textViewDate);
        textViewAlarm1 = findViewById(R.id.alarmTime1);
        textViewAlarm2 = findViewById(R.id.alarmTime2);

        alarmSwitch1 = (Switch) findViewById(R.id.switch1);
        alarmSwitch2 = (Switch) findViewById(R.id.switch2);
        syncButton = (Button) findViewById(R.id.button12);

        setConnection(false);
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    public void enterDateTimeActivity(View view) {
        Intent intent = new Intent(this, DateTimeActivity.class);
        startActivity(intent);
    }

    public void enterSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void showTimePickerDialogAlarm1(View v) {
        TimePickerFragment newFragment = TimePickerFragment.newInstance(1);
        newFragment.show(getSupportFragmentManager(), "timePicker");
        newFragment.setActivity(this);
    }

    public void showTimePickerDialogAlarm2(View v) {
        TimePickerFragment newFragment = TimePickerFragment.newInstance(2);
        newFragment.show(getSupportFragmentManager(), "timePicker");
        newFragment.setActivity(this);
    }

    public void requestUpdate(View view) {
        BtService.sendUpdateRequest();
        setConnection(false);
        Toast.makeText(this, "syncing..", Toast.LENGTH_LONG).show();
    }

    public void updateData() {
        textViewTime.setText(ClockService.getTimeText());
        textViewDate.setText(ClockService.getDateText());

        alarmSwitch1.setOnCheckedChangeListener(null);
        alarmSwitch2.setOnCheckedChangeListener(null);
        alarmSwitch1.setChecked(ClockService.getAlarmState(0));
        alarmSwitch2.setChecked(ClockService.getAlarmState(1));
        setChangeListeners();

        textViewAlarm1.setText(ClockService.getAlarmText(0));
        textViewAlarm2.setText(ClockService.getAlarmText(1));
        setConnection(true);
    }

    public void setConnection(boolean state) {
        textViewAlarm1.setClickable(state);
        textViewAlarm2.setClickable(state);
        alarmSwitch1.setClickable(state);
        alarmSwitch2.setClickable(state);
        syncButton.setClickable(state);
    }

    private void setChangeListeners() {
        alarmSwitch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ClockService.setAlarmState(0, true);
                BtService.sendAlarmState(0, true);
            } else {
                ClockService.setAlarmState(0, false);
                BtService.sendAlarmState(0, false);
            }
            setConnection(false);
        });

        alarmSwitch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ClockService.setAlarmState(1, true);
                BtService.sendAlarmState(1, true);

            } else {
                ClockService.setAlarmState(1, false);
                BtService.sendAlarmState(1, false);
            }
            setConnection(false);
        });
    }

}