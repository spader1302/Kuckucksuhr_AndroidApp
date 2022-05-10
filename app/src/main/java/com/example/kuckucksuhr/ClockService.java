package com.example.kuckucksuhr;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Calendar;

public class ClockService extends Service {

    private static final Handler handler = new Handler();

    private static LocalDateTime kuckuckDateTime = LocalDateTime.of(2000,1,1,0,0);

    private static boolean bHourly, bNightmode;

    private static boolean[] bAlarmState = {false, false};
    private static LocalTime[] alarmTime = {LocalTime.of(0,0), LocalTime.of(0,0)};

    public ClockService() {
    }

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String getTimeText() {
        return String.format("%02d:%02d", getHour(), getMinute());
    }

    public static String getDateText() {
        return String.format("%02d.%02d.%04d", getDay(), getMonth(), getYear());
    }

    public static String getAlarmText(int alarm_no) {
        return String.format("%02d:%02d", alarmTime[alarm_no].getHour(), alarmTime[alarm_no].getMinute());
    }

    public static void setHour(int hour) {
        kuckuckDateTime = LocalDateTime.of(kuckuckDateTime.getYear(), kuckuckDateTime.getMonthValue(), kuckuckDateTime.getDayOfMonth(), hour, kuckuckDateTime.getMinute(), kuckuckDateTime.getSecond());
    }

    public static void setMinute(int minute) {
        kuckuckDateTime = LocalDateTime.of(kuckuckDateTime.getYear(), kuckuckDateTime.getMonthValue(), kuckuckDateTime.getDayOfMonth(), kuckuckDateTime.getHour(), minute, kuckuckDateTime.getSecond());
    }

    public static void setSecond(int second) {
        kuckuckDateTime = LocalDateTime.of(kuckuckDateTime.getYear(), kuckuckDateTime.getMonthValue(), kuckuckDateTime.getDayOfMonth(), kuckuckDateTime.getHour(), kuckuckDateTime.getMinute(), second);
    }

    public static void setYear(int year) {
        kuckuckDateTime = LocalDateTime.of(year, kuckuckDateTime.getMonthValue(), kuckuckDateTime.getDayOfMonth(), kuckuckDateTime.getHour(), kuckuckDateTime.getMinute(), kuckuckDateTime.getSecond());
    }

    public static void setMonth(int month) {
        kuckuckDateTime = LocalDateTime.of(kuckuckDateTime.getYear(), month, kuckuckDateTime.getDayOfMonth(), kuckuckDateTime.getHour(), kuckuckDateTime.getMinute(), kuckuckDateTime.getSecond());
    }

    public static void setDay(int day) {
        kuckuckDateTime = LocalDateTime.of(kuckuckDateTime.getYear(), kuckuckDateTime.getMonthValue(), day, kuckuckDateTime.getHour(), kuckuckDateTime.getMinute(), kuckuckDateTime.getSecond());
    }

    public static void setAlarmHour(int alarm_no, int hour) {
        alarmTime[alarm_no] = LocalTime.of(hour, alarmTime[alarm_no].getMinute());
    }

    public static void setAlarmMinute(int alarm_no, int minute) {
        alarmTime[alarm_no] = LocalTime.of(alarmTime[alarm_no].getHour(), minute);
    }

    public static void setAlarmState(int alarm_no, boolean state) {
        if (alarm_no >=0 && alarm_no < 2) {
            bAlarmState[alarm_no] = state;
        }
    }

    public static void setHourly(boolean state) {
        bHourly = state;
    }

    public static void setNightmode(boolean state) {
        bNightmode = state;
    }

    public static int getHour() {
        return kuckuckDateTime.getHour();
    }

    public static int getMinute() {
        return kuckuckDateTime.getMinute();
    }

    public static int getSecond() {
        return kuckuckDateTime.getSecond();
    }

    public static int getYear() {
        return kuckuckDateTime.getYear();
    }

    public static int getMonth() {
        return kuckuckDateTime.getMonthValue();
    }

    public static int getDay() {
        return kuckuckDateTime.getDayOfMonth();
    }

    public static boolean getAlarmState(int alarm_no) {
        return bAlarmState[alarm_no];
    }

    public static int getAlarmHour(int alarm_no) {
        return alarmTime[alarm_no].getHour();
    }

    public static int getAlarmMinute(int alarm_no) {
        return alarmTime[alarm_no].getMinute();
    }

    public static boolean getHourly() {
        return bHourly;
    }

    public static boolean getNightmode() {
        return bNightmode;
    }

    public static void syncWithRealTime() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        ClockService.setYear(year);
        ClockService.setMonth(month);
        ClockService.setDay(day);
        ClockService.setHour(hour);
        ClockService.setMinute(minute);
        ClockService.setSecond(second);
    }



}
