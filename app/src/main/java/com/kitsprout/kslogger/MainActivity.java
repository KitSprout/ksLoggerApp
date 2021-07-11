package com.kitsprout.kslogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import com.kitsprout.system.ksDevice;
import com.kitsprout.system.ksFile;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "KS-LOG";
    private View mainLayout;
    private TextView informationText;
    private Button logTriggerButton;

    //
    private String phoneInfo;
    private ksFile logFile = new ksFile("log");
    private String logString = "";
    private boolean logEnable = false;

    // sensor
    private String sensorInfo;
    private SensorManager sensorManager;
    private int sensorMode = SensorManager.SENSOR_DELAY_GAME;
    private float[] gyr  = new float[6];
    private float[] acc  = new float[6];
    private float[] mag  = new float[6];
//    private float[] magU = new float[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // layout
        mainLayout = findViewById(R.id.mainLayout);
        informationText = findViewById(R.id.InformationTextView);
        logTriggerButton = findViewById(R.id.LogTriggerButton);

        // sensor startup
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        setSensorUpdateMode(sensorList, sensorMode);

        // get model, cpu, android version
        ksDevice.showSystemParameter();
        phoneInfo = ksDevice.getSystemInformationString();
        informationText.setText(String.format("%s\nSENSOR: %s", phoneInfo, sensorInfo));

        // request permission
        requestFileSystemPermission();

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void requestFileSystemPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mainLayout, "已取得檔案儲存權限", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(mainLayout, "需要取得檔案儲存權限授權來紀錄 LOG", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        }
                    }).show();
        } else {
            Snackbar.make(mainLayout, "檔案讀寫權限尚未取得授權", Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private final int[] sensorList = {
//            Sensor.TYPE_GYROSCOPE,                      // x[0], y[1], z[2] (rad/s)
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED,         // x[0], y[1], z[2] (rad/s), bias x[3], y[4], z[5] (rad/s)
//            Sensor.TYPE_ACCELEROMETER,                  // x[0], y[1], z[2] (m/s^2)
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,     // x[0], y[1], z[2] (m/s^2), bias x[3], y[4], z[5] (m/s^2)
//            Sensor.TYPE_MAGNETIC_FIELD,                 // x[0], y[1], z[2] (uT)
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,    // x[0], y[1], z[2] (uT),    bias x[3], y[4], z[5] (uT)
//            Sensor.TYPE_PRESSURE,                       // p[0] (hPa or millibar)
//            Sensor.TYPE_AMBIENT_TEMPERATURE,            // t[0] (degC)
//            Sensor.TYPE_LINEAR_ACCELERATION,            // x[0], y[1], z[2] (m/s^2)
//            Sensor.TYPE_GRAVITY,                        // x[0], y[1], z[2] (m/s^2)
//            Sensor.TYPE_ROTATION_VECTOR,                // v[0], v[1], v[2], v[3],   heading Accuracy (in radians) (-1 if unavailable)
//            Sensor.TYPE_GAME_ROTATION_VECTOR,           // acc + gyr, without magnetometer
//            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,    // acc + mag, without gyroscope
    };
    private final String[][] sensorListString = {
//            {"GYROSCOPE                  ", "GC"},  // gyr calibrated
            {"GYROSCOPE_UNCALIBRATED     ", "GU"},  // gyr uncalibrated
//            {"ACCELEROMETER              ", "AC"},  // acc calibrated
            {"ACCELEROMETER_UNCALIBRATED ", "AU"},  // acc uncalibrated
//            {"MAGNETIC_FIELD             ", "MC"},  // mag calibrated
            {"MAGNETIC_FIELD_UNCALIBRATED", "MU"},  // mag uncalibrated
//            {"PRESSURE                   ", "PC"},  // pressure
//            {"AMBIENT_TEMPERATURE        ", "TC"},  // temperature
//            {"LINEAR_ACCELERATION        ", "AL"},  // linear acc
//            {"GRAVITY                    ", "AG"},  // gravity
//            {"ROTATION_VECTOR            ", "RC"},  // rotation
//            {"GAME_ROTATION_VECTOR       ", "RG"},  // rotation, gyr + acc
//            {"GEOMAGNETIC_ROTATION_VECTOR", "RM"},  // rotation, acc + mag

    };

    private boolean[] sensorEnable = new boolean[sensorList.length];
    private long[][] sensorTimestamp = new long[sensorList.length][2];  // dt, timestamp

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        float dt = getSensorSamplingTime(event);
        switch (event.sensor.getType()) {
//            case Sensor.TYPE_GYROSCOPE:
//                System.arraycopy(values, 0, gyr, 0, 3);
//                Log.d(TAG, String.format("[GC][%.4f] %12.6f %12.6f %12.6f", dt, gyr[0], gyr[1], gyr[2]));
//                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                System.arraycopy(values, 0, gyr, 0, 6);
//                Log.d(TAG, String.format("[GU][%.4f] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f ... %s", dt, gyr[0], gyr[1], gyr[2], gyr[3], gyr[4], gyr[5], event.sensor.getVendor()));
//                Log.d(TAG, String.format("[GU][%.4f] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f", dt, gyr[0]-gyr[3], gyr[1]-gyr[4], gyr[2]-gyr[5], gyr[3], gyr[4], gyr[5]));
//                Log.d(TAG, String.format("[IMU][G] %12.6f %12.6f %12.6f [A] %12.6f %12.6f %12.6f [M] %12.6f %12.6f %12.6f [T] %.6f",
//                        gyr[0]-gyr[3], gyr[1]-gyr[4], gyr[2]-gyr[5],
//                        acc[0]-acc[3], acc[1]-acc[4], acc[2]-acc[5],
//                        mag[0]-mag[3], mag[1]-mag[4], mag[2]-mag[5], dt));
                float[] yg = new float[6];
                float[] ya = new float[6];
                float[] ym = new float[6];
                for (int i = 0; i < 6; i++)
                {
                    yg[i] = gyr[i];
                    ya[i] = acc[i];
                    ym[i] = mag[i];
                }
                for (int i = 0; i < 3; i++)
                {
                    yg[i] -= yg[i+3];
                    ya[i] -= ya[i+3];
                    ym[i] -= ym[i+3];
                }
                final String rawString = String.format("[G] %12.6f %12.6f %12.6f\n[A] %12.6f %12.6f %12.6f\n[M] %12.6f %12.6f %12.6f\n[B] %12.6f %12.6f %12.6f\n[T] %.6f",
                        yg[0], yg[1], yg[2],
                        ya[0], ya[1], ya[2],
                        ym[0], ym[1], ym[2],
                        ym[3], ym[4], ym[5], dt);
                informationText.setText(String.format("%s", rawString));
                updateLog(yg, ya, ym, dt, true);
                break;
//            case Sensor.TYPE_ACCELEROMETER:
//                System.arraycopy(values, 0, acc, 0, 3);
//                Log.d(TAG, String.format("[AC][%.4f] %12.6f %12.6f %12.6f", dt, acc[0], acc[1], acc[2]));
//                break;
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                System.arraycopy(values, 0, acc, 0, 6);
//                Log.d(TAG, String.format("[AU][%.4f] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f", dt, acc[0], acc[1], acc[2], acc[3], acc[4], acc[5]));
//                Log.d(TAG, String.format("[AU][%.4f] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f", dt, acc[0]-acc[3], acc[1]-acc[4], acc[2]-acc[5], acc[3], acc[4], acc[5]));
                break;
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                System.arraycopy(values, 0, mag, 0, 3); // mag = values;
//                // check mag uncalibrated data is enable
//                if (!sensorEnable[getSensorListIndex(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)]) {
//                    System.arraycopy(values, 0, magU, 0, 3); // magU = mag;
//                }
//                Log.d(TAG, String.format("[MC][%.4f] %12.6f %12.6f %12.6f", dt, mag[0], mag[1], mag[2]));
//                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                System.arraycopy(values, 0, mag, 0, 6);
//                Log.d(TAG, String.format("[MU][%.4f] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f", dt, magU[0], magU[1], magU[2], magU[3], magU[4], magU[5]));
//                Log.d(TAG, String.format("[MU][%.4f] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f", dt, mag[0]-mag[3], mag[1]-mag[4], mag[2]-mag[5], mag[3], mag[4], mag[5]));
                break;
//            case Sensor.TYPE_PRESSURE:
//                Log.d(TAG, String.format("[PC][%.4f] %12.6f", dt, values[0]));
//                break;
//            case Sensor.TYPE_AMBIENT_TEMPERATURE:
//                Log.d(TAG, String.format("[TC][%.4f] %12.6f", dt, values[0]));
//                break;
//            case Sensor.TYPE_LINEAR_ACCELERATION:
//                Log.d(TAG, String.format("[AL][%.4f] %12.6f %12.6f %12.6f", dt, values[0], values[1], values[2]));
//                break;
//            case Sensor.TYPE_GRAVITY:
//                Log.d(TAG, String.format("[AG][%.4f] %12.6f %12.6f %12.6f", dt, values[0], values[1], values[2]));
//                break;
//            case Sensor.TYPE_ROTATION_VECTOR:
//                Log.d(TAG, String.format("[RC][%.4f] %12.6f %12.6f %12.6f %12.6f", dt, values[0], values[1], values[2], values[3]));
//                break;
//            case Sensor.TYPE_GAME_ROTATION_VECTOR:
//                Log.d(TAG, String.format("[RG][%.4f] %12.6f %12.6f %12.6f %12.6f", dt, values[0], values[1], values[2], values[3]));
//                break;
//            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
//                Log.d(TAG, String.format("[RM][%.4f] %12.6f %12.6f %12.6f %12.6f", dt, values[0], values[1], values[2], values[3]));
//                break;
            default:
                Log.w(TAG, "Unexpected sensor type: " + event.sensor.getType());
        }
    }

    private int getSensorListIndex(int type) {
        for (int i = 0; i < sensorList.length; i++) {
            if (sensorList[i] == type) {
                return i;
            }
        }
        return -1;
    }

    private float getSensorSamplingTime(SensorEvent event) {
        int idx = getSensorListIndex(event.sensor.getType());
        if (idx == -1) {
            return 0;
        }
        if (sensorTimestamp[idx][1] == 0) {
            sensorTimestamp[idx][1] = event.timestamp;
            return 0;
        }
        sensorTimestamp[idx][0] = event.timestamp - sensorTimestamp[idx][1];
        sensorTimestamp[idx][1] = event.timestamp;
        return sensorTimestamp[idx][0] / 1000000000.0f;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setSensorUpdateMode(int[] list, int mode) {
        StringBuilder info = new StringBuilder();
        sensorManager.unregisterListener(this);
        for (int i = 0; i < list.length; i++) {
            sensorEnable[i] = sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(list[i]), mode);
            if (sensorEnable[i]) {
                if (info.length() != 0) {
                    info.append(", ");
                }
                info.append(sensorListString[i][1]);
            }
            sensorInfo = info.toString();
        }
        // print information
        for (int i = 0; i < list.length; i++) {
            if (sensorEnable[i]) {
                Log.d(TAG, String.format("%s -> ENABLE", sensorListString[i][0]));
            } else {
                Log.w(TAG, String.format("%s -> DISABLE", sensorListString[i][0]));
            }
        }
    }

    public void onButtonLogClickEvent(View view) {
        if (logEnable) {
            stopLogger();
            logTriggerButton.setText("START");
            logTriggerButton.setTextColor(Color.GRAY);
            Log.d(TAG, "onButtonLogClickEvent Off");
        } else {
            startLogger();
            logTriggerButton.setText("STOP");
            logTriggerButton.setTextColor(Color.RED);
            Log.d(TAG, "onButtonLogClickEvent On");
        }
        vibrateDelay(200);
    }
    private void startLogger() {
        String fileName = "LOG" + "_APP_" + getSystemTimeString(getSystemTime(), "yyyyMMdd_HHmmss") + ".csv";
        boolean status = logFile.createFile(fileName);
        if (status) {
            // csv format: gyr(3), acc(3), mag(3), dt(1), bias(3)
            String rawString = phoneInfo + "\n\n";
            rawString += "GYR.X,GYR.Y,GYR.Z,ACC.X,ACC.Y,ACC.Z,MAG.X,MAG.Y,MAG.Z";
            rawString += ",DT";
            rawString += ",MAG.BIAS.X,MAG.BIAS.Y,MAG.BIAS.Z";
            rawString += "\n";
            logFile.writeFile(rawString);
            logEnable = true;
            logString = "";
        } else {
            Log.d(TAG, "No file read/write permission");
        }
    }

    private void stopLogger() {
        logEnable = false;
        logString = "";
    }

    private void updateLog(float[] yg, float[] ya, float[] ym, float dt, boolean write2csv) {
        if (logEnable) {
            logString += String.format("%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f", yg[0], yg[1], yg[2], ya[0], ya[1], ya[2], ym[0], ym[1], ym[2]);
            logString += String.format(",%.10f", dt);
            logString += String.format(",%.10f,%.10f,%.10f", ym[3], ym[4], ym[5]);
            logString += String.format("\n");
            if (write2csv) {
                logFile.writeFile(logString);
                logString = "";
            }
        }
    }

    private Date getSystemTime() {
        return new Date(System.currentTimeMillis());
    }

    private String getSystemTimeString(final Date date, final String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    private void vibrateDelay(long milliseconds) {
        Vibrator vibrator = (Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(milliseconds);
    }

}