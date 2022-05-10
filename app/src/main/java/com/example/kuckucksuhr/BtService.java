package com.example.kuckucksuhr;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import android.os.CountDownTimer;

public class BtService extends Service {

    private static final Handler handler = new Handler();

    private static final String KUCKUCK_MAC_ADRESS = "98:D3:41:F5:E3:08";
    private static final int MILLIS_SYNC_DELAY = 100;
    private static final int MAX_FAIL_COUNT = 100;
    private static final char START_BYTE = 'X';
    private static final char END_BYTE = 'Z';

    private static String lastCmd = "";
    private static boolean bConnectionBlocked = false;
    private static boolean bCmdAcknowledged = false;
    private static int syncFailCount = 0;
    private static boolean bDateSyncOnly = false;

    private static final BluetoothManager bluetoothManager = BluetoothManager.getInstance();
    private static SimpleBluetoothDeviceInterface deviceInterface;

    private static MainActivity mainActivity;
    private static DateTimeActivity dateTimeActivity;
    private static SettingsActivity settingsActivity;

    private static Context _context;

    private static final CountDownTimer timerLastCmdSent = new CountDownTimer(10000, 3000) {
        @Override
        public void onTick(long millisUntilFinished) {
            deviceInterface.sendMessage(START_BYTE + lastCmd + END_BYTE);
        }

        @Override
        public void onFinish() {
            Toast.makeText(_context, "Kuckuck antwortet nicht", Toast.LENGTH_LONG).show();
            setConnection(true);
        }
    };

    public BtService() {
    }

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onDestroy() {
        onError(new Throwable());
    }

    public static boolean begin(Context context) {
        if (bluetoothManager == null) {
            return false;
        }
        _context = context;
        return true;
    }


    @SuppressLint("CheckResult")
    public static void connect() {
        bluetoothManager.openSerialDevice(KUCKUCK_MAC_ADRESS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(BtService::onConnected, BtService::onError);
    }

    public static void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public static void setDateTimeActivity(DateTimeActivity activity) {
        dateTimeActivity = activity;
    }

    public static void setSettingsActivity(SettingsActivity activity) {
       settingsActivity = activity;
    }

    public static void sendNightmode(boolean state) {
        if (state) {
            sendCmd("N1");
        } else {
            sendCmd("N0");
        }
    }

    public static void sendHourly(boolean state) {
        if (state) {
            sendCmd("H1");
        } else {
            sendCmd("H0");
        }
    }

    public static void sendAlarmState(int alarm_no, boolean state) {
        if (state) {
            sendCmd("AS" + alarm_no + "1");
        } else {
            sendCmd("AS" + alarm_no + "0");
        }
    }

    public static void sendAlarmHour(int alarm_no, int hour) {
        if (hour < 10) {
            sendCmd("AH" + alarm_no + "0" + hour);
        } else {
            sendCmd("AH" + alarm_no + "" + hour);
        }
    }

    public static void sendAlarmMinute(int alarm_no, int minute) {
        if (minute < 10) {
            sendCmd("AM" + alarm_no + "0" + minute);
        } else {
            sendCmd("AM" + alarm_no + "" + minute);
        }
    }

    public static void sendTimeHour(int hour) {
        if (hour < 10) {
            sendCmd("TH0" + hour);
        } else {
            sendCmd("TH" + hour);
        }
    }

    public static void sendTimeMinute(int minute) {
        if (minute < 10) {
            sendCmd("Tm0" + minute);
        } else {
            sendCmd("Tm" + minute);
        }
    }

    public static void sendTimeSecond(int second) {
        if (second < 10) {
            sendCmd("TS0" + second);
        } else {
            sendCmd("TS" + second);
        }
    }

    public static void sendTimeYear(int year) {
        sendCmd("TY" + year);
    }

    public static void sendTimeMonth(int month) {
        if (month < 10) {
            sendCmd("TM0" + month);
        } else {
            sendCmd("TM" + month);
        }
    }

    public static void sendTimeDay(int day) {
        if (day < 10) {
            sendCmd("TD0" + day);
        } else {
            sendCmd("TD" + day);
        }
    }

    public static void sendUpdateRequest() {
        sendCmd("U");
    }

    public static void syncDateTime() {
        bConnectionBlocked = true;
        bCmdAcknowledged = true;
        syncFailCount = 0;
        bDateSyncOnly = false;
        queueYearSync();
    }

    public static void syncTime() {
        bConnectionBlocked = true;
        bCmdAcknowledged = true;
        syncFailCount = 0;
        queueHourSync();
    }

    public static void syncDate() {
        bConnectionBlocked = true;
        bCmdAcknowledged = true;
        syncFailCount = 0;
        bDateSyncOnly = true;
        queueYearSync();
    }

    public static void syncAlarmTime(int alarm_no) {
        bConnectionBlocked = true;
        bCmdAcknowledged = true;
        syncFailCount = 0;
        queueAlarmHourSync(alarm_no);
    }

    private static void queueYearSync() {
         sendTimeYear(ClockService.getYear());
         handler.postDelayed(BtService::queueMonthSync, MILLIS_SYNC_DELAY);
    }

    private static void queueMonthSync() {
        if (bCmdAcknowledged) {
            sendTimeMonth(ClockService.getMonth());
            handler.postDelayed(BtService::queueDaySync, MILLIS_SYNC_DELAY);
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(BtService::queueMonthSync, MILLIS_SYNC_DELAY);
        }
    }

    private static void queueDaySync() {
        if (bCmdAcknowledged) {
            sendTimeDay(ClockService.getDay());
            if (bDateSyncOnly) {
                handler.postDelayed(BtService::queueSyncCheck, MILLIS_SYNC_DELAY);
            } else {
                handler.postDelayed(BtService::queueHourSync, MILLIS_SYNC_DELAY);
            }
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(BtService::queueDaySync, MILLIS_SYNC_DELAY);
        }
    }

    private static void queueHourSync() {
        if (bCmdAcknowledged) {
            sendTimeHour(ClockService.getHour());
            handler.postDelayed(BtService::queueMinuteSync, MILLIS_SYNC_DELAY);
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(BtService::queueHourSync, MILLIS_SYNC_DELAY);
        }
    }

    private static void queueMinuteSync() {
        if (bCmdAcknowledged) {
            sendTimeMinute(ClockService.getMinute());
            handler.postDelayed(BtService::queueSecondSync, MILLIS_SYNC_DELAY);
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(BtService::queueMinuteSync, MILLIS_SYNC_DELAY);
        }
    }

    private static void queueSecondSync() {
        if (bCmdAcknowledged) {
            sendTimeSecond(ClockService.getSecond());
            handler.postDelayed(BtService::queueSyncCheck, MILLIS_SYNC_DELAY);
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(BtService::queueSecondSync, MILLIS_SYNC_DELAY);
        }
    }

    private static void queueAlarmHourSync(int alarm_no) {
        if (bCmdAcknowledged) {
            sendAlarmHour(alarm_no, ClockService.getAlarmHour(alarm_no));
            handler.postDelayed(() -> {
                queueAlarmMinuteSync(alarm_no);
            }, MILLIS_SYNC_DELAY);
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(() -> {
                queueAlarmHourSync(alarm_no);
            }, MILLIS_SYNC_DELAY);
        }
    }

    private static void queueAlarmMinuteSync(int alarm_no) {
        if (bCmdAcknowledged) {
            sendAlarmMinute(alarm_no, ClockService.getAlarmMinute(alarm_no));
            handler.postDelayed(BtService::queueSyncCheck, MILLIS_SYNC_DELAY);
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(() -> {
                queueAlarmMinuteSync(alarm_no);
            }, MILLIS_SYNC_DELAY);
        }
    }

    private static void queueSyncCheck() {
        if (bCmdAcknowledged) {
            bConnectionBlocked = false;
            setConnection(true);
        } else {
            if (++syncFailCount > MAX_FAIL_COUNT) {
                onError(new Throwable());
                return;
            }
            handler.postDelayed(BtService::queueSyncCheck, MILLIS_SYNC_DELAY);
        }
    }



    private static void connectKuckuck() {
        deviceInterface.sendMessage(START_BYTE + "C" + END_BYTE);
    }

    private static void acknowledgeConnection() {
        sendUpdateRequest();
        Toast.makeText(_context, "Kuckuck connected - syncing data..", Toast.LENGTH_LONG).show();
    }

    private static void sendCmd(String cmd) {
        if (deviceInterface == null) return;
        setConnection(false);
        lastCmd = cmd;
        bCmdAcknowledged = false;
        timerLastCmdSent.start();
    }

    private static void onMessageSent(String message) {
        // We sent a message! Handle it here.
        Log.d("Bluetooth", "Sent message: " + message);
    }

    private static void onMessageReceived(String message) {
        // We received a message! Handle it here.
        setConnection(false);                               //Schaltflächen werden deaktiviert, bis eingehender Command "XQZ" oder "XUZ"
        char[] msg = message.toCharArray();

        if (msg[0] == START_BYTE && msg[msg.length - 1] == END_BYTE) {
            handleReceived(msg);
        }
        Log.d("Bluetooth", "Received message: " + message);
    }

    private static void handleReceived(final char[] msg) {

        switch (msg[1]) {
            case 'C':
                connectKuckuck();
                break;

            case 'K':
                acknowledgeConnection();
                break;

            case 'N':
                nightmodeInput(msg);
                break;

            case 'H':
                hourlyInput(msg);
                break;
            case 'A':
                alarmInput(msg);
                break;
            case 'T':
                timeInput(msg);
                break;

            case 'U':
                updateActivities();
                bCmdAcknowledged = true;
                setConnection(true);
                Toast.makeText(_context, "Update finished!", Toast.LENGTH_LONG).show();
                break;

            case 'Q':
                timerLastCmdSent.cancel();
                bCmdAcknowledged = true;
                setConnection(true);
                break;
        }
    }

    private static void nightmodeInput(char[] msg) {

        switch (msg[2]) {
            case '0':
                ClockService.setNightmode(false);
                break;

            case '1':
                ClockService.setNightmode(true);
                break;
        }
    }

    private static void hourlyInput(char[] msg) {

        switch (msg[2]) {
            case '0':
                ClockService.setHourly(false);
                break;

            case '1':
                ClockService.setHourly(true);
                break;
        }
    }

    private static void alarmInput(char[] msg) {

        switch (msg[2]) {
            case 'S':
                alarmStateInput(msg);
                break;

            case 'H':
                alarmHourInput(msg);
                break;

            case 'M':
                alarmMinuteInput(msg);
                break;
        }
    }

    private static void timeInput(char[] msg) {
        switch (msg[2]) {
            case 'H':
                timeHourInput(msg);
                break;

            case 'm':
                timeMinuteInput(msg);
                break;

            case 'S':
                timeSecondInput(msg);
                break;

            case 'Y':
                timeYearInput(msg);
                break;

            case 'M':
                timeMonthInput(msg);
                break;

            case 'D':
                timeDayInput(msg);
                break;
        }
    }

    private static void updateActivities() {
        if (mainActivity != null) {
            mainActivity.updateData();
        }
    }

    private static void alarmStateInput(char[] msg) {
        switch (msg[4]) {

            case '0':
                ClockService.setAlarmState(Character.getNumericValue(msg[3]), false);
                break;

            case '1':
                ClockService.setAlarmState(Character.getNumericValue(msg[3]), true);
                break;
        }
    }

    private static void alarmHourInput(char[] msg) {
        int hour = Character.getNumericValue(msg[4]) * 10 + Character.getNumericValue(msg[5]);
        if (hour < 0 || hour > 23) {
            return;
        }
        ClockService.setAlarmHour(Character.getNumericValue(msg[3]), hour);
    }

    private static void alarmMinuteInput(char[] msg) {
        int minute = Character.getNumericValue(msg[4]) * 10 + Character.getNumericValue(msg[5]);
        if (minute < 0 || minute > 59) {
            return;
        }
        ClockService.setAlarmMinute(Character.getNumericValue(msg[3]), minute);
    }

    private static void timeHourInput(char[] msg) {
        int hour = Character.getNumericValue(msg[3]) * 10 + Character.getNumericValue(msg[4]);
        if (hour < 0 || hour > 23) {
            return;
        }
        ClockService.setHour(hour);
    }

    private static void timeMinuteInput(char[] msg) {
        int minute = Character.getNumericValue(msg[3]) * 10 + Character.getNumericValue(msg[4]);
        if (minute < 0 || minute > 59) {
            return;
        }
        ClockService.setMinute(minute);
    }

    private static void timeSecondInput(char[] msg) {
        int second = Character.getNumericValue(msg[3]) * 10 + Character.getNumericValue(msg[4]);
        if (second < 0 || second > 59) {
            return;
        }
        ClockService.setSecond(second);
    }

    private static void timeYearInput(char[] msg) {
        int year = Character.getNumericValue(msg[3]) * 1000 + Character.getNumericValue(msg[4]) * 100 + Character.getNumericValue(msg[5]) * 10 + Character.getNumericValue(msg[6]);
        if (year < 1970 || year > 3000) {
            return;
        }
        ClockService.setYear(year);
    }

    private static void timeMonthInput(char[] msg) {
        int month = Character.getNumericValue(msg[3]) * 10 + Character.getNumericValue(msg[4]);
        if (month < 1 || month > 12) {
            return;
        }
        ClockService.setMonth(month);
    }

    private static void timeDayInput(char[] msg) {
        int day = Character.getNumericValue(msg[3]) * 10 + Character.getNumericValue(msg[4]);
        if (day < 1 || day > 31) {
            return;
        }
        ClockService.setDay(day);
    }

    private static void onError(Throwable error) {
        // Handle the error
        setConnection(false);
        Log.d("Error", error.toString());
        Toast.makeText(_context, "Verbindungsfehler, App neustarten", Toast.LENGTH_LONG).show();
    }

    private static void onConnected(BluetoothSerialDevice connectedDevice) {

        //Toast.makeText(_context, "BT connected", Toast.LENGTH_LONG).show();

        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface();

        // Listen to bluetooth events
        deviceInterface.setListeners(BtService::onMessageReceived, BtService::onMessageSent, BtService::onError);

        setConnection(true);
    }

    public static void setConnection(boolean state) {
        if (bConnectionBlocked) {
            state = false;
        }
        if (mainActivity != null) {
            mainActivity.setConnection(state);
        }
        if (dateTimeActivity != null) {
            dateTimeActivity.setConnection(state);
        }
        if (settingsActivity != null) {
            settingsActivity.setConnection(state);
        }
    }
}
