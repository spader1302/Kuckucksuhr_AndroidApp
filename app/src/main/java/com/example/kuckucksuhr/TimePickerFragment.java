package com.example.kuckucksuhr;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private final Handler handler = new Handler();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TIME_TYPE = "timeType";

    // TODO: Rename and change types of parameters
    private int _timeType;
    private MainActivity mainActivity;
    private boolean bDataWritten = false;

    public TimePickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment TimePickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimePickerFragment newInstance(int timeType) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt(TIME_TYPE, timeType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _timeType = getArguments().getInt(TIME_TYPE);
        }

        int hour = ClockService.getHour();
        int minute = ClockService.getSecond();

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_picker, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainActivity != null) {
            mainActivity.updateData();
        }
        if (bDataWritten) {
            BtService.setConnection(false);
        }
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        // Do something with the date chosen by the user
        bDataWritten = true;
        switch (_timeType) {
            case 0:
                ClockService.setHour(hour);
                ClockService.setMinute(minute);
                ClockService.setSecond(0);
                BtService.syncTime();
                break;

            case 1:
                ClockService.setAlarmHour(0, hour);
                ClockService.setAlarmMinute(0, minute);
                BtService.syncAlarmTime(0);
                break;

            case 2:
                ClockService.setAlarmHour(1, hour);
                ClockService.setAlarmMinute(1, minute);
                BtService.syncAlarmTime(1);
                break;
        }
    }

    public void setActivity(MainActivity activity) {
        mainActivity = activity;
    }
}