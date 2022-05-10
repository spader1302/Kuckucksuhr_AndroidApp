package com.example.kuckucksuhr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class DateTimeActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private Button syncButton;
    private Button timeButton;
    private Button dateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time);

        syncButton = (Button) findViewById(R.id.button11);
        timeButton = (Button) findViewById(R.id.button9);
        dateButton = (Button) findViewById(R.id.button10);

        BtService.begin(getApplicationContext());
        BtService.setDateTimeActivity(this);
        BtService.connect();
    }

    public void setConnection(boolean state) {
        if (state) {
            syncButton.setClickable(true);
            timeButton.setClickable(true);
            dateButton.setClickable(true);
        } else {
            syncButton.setClickable(false);
            timeButton.setClickable(false);
            dateButton.setClickable(false);
        }
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = TimePickerFragment.newInstance(0);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void syncDateTime(View view) {
        ClockService.syncWithRealTime();
        BtService.syncDateTime();
    }
}