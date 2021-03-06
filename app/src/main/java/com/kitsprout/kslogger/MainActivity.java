package com.kitsprout.kslogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

import com.kitsprout.util.CsvFile;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "KS-LOG";
    private static final String TAG_SENS = "KS-SENS";
    private View mainLayout;
    private TextView infoText;
    private TextView fileCountText;
    private TextView samplingRateText;
    private TextView calGyrDataText;
    private TextView calAccDataText;
    private TextView calMagDataText;
    private TextView calMagBiasDataText;
    private TextView logInfoText;
    private ToggleButton logToggleButton;

    // file
    private CsvFile logFile = new CsvFile("");
    private boolean logEnable = false;
    private long logCount = 0;

    // sensor
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;
    private float[] gyr = new float[6];
    private float[] acc = new float[6];
    private float[] mag = new float[6];
    private long dtns = 0;
    private long[] ts = new long[2];    // last timestamp, timestamp

    public String getDeviceName() {
        String manufacturer = android.os.Build.MANUFACTURER.toUpperCase();
        String model = android.os.Build.MODEL.replace("-", "")
                .replace("_", "").replace(" ", "");
        return manufacturer + " " + model;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // layout
        mainLayout = findViewById(R.id.mainLayout);
        infoText = findViewById(R.id.InfoText);
        fileCountText = findViewById(R.id.FileCountText);
        samplingRateText = findViewById(R.id.SamplingRateText);
        calGyrDataText = findViewById(R.id.CalGyrDataText);
        calAccDataText = findViewById(R.id.CalAccDataText);
        calMagDataText = findViewById(R.id.CalMagDataText);
        calMagBiasDataText = findViewById(R.id.CalMagBiasDataText);
        logInfoText = findViewById(R.id.LogInfoText);
        logToggleButton = findViewById(R.id.LogToggleButton);

        // device information
        Log.d(TAG, getDeviceName() + " " + android.os.Build.HARDWARE + " Android " + android.os.Build.VERSION.RELEASE);

        // get log file count
        fileCountText.setText(String.valueOf(logFile.getListFilesCount()));

        // request permission
        requestFileSystemPermission();

        // sensor startup
        sensorSetup();
        sensorStart();

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void requestFileSystemPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            Snackbar.make(mainLayout, "???????????????????????????", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(mainLayout, "????????????????????????????????????????????? LOG", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        }
                    }).show();
        } else {
            Snackbar.make(mainLayout, "????????????????????????????????????", Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public void onLogButtonClickEvent(View view) {
        fileCountText.setText(String.valueOf(logFile.getListFilesCount()));
        if (logToggleButton.isChecked()) {
            logStart();
        }
        else {
            logStop();
        }
        vibrateDelay(200);
    }

    private void logStart() {
        String fileName = "LOG_" + getDeviceName().replace(" ", "_") + "_"
                + getSystemTimeString(getSystemTime(), "yyyyMMdd_HHmmss") + ".csv";
        boolean status = logFile.createFile(fileName);
        if (status) {
            // csv format: sn, ts, dt, gyr(3), acc(3), mag(3), bias(3)
            logFile.writeFile("sn,ts,dt,gx,gy,gz,ax,ay,az,mx,my,mz,mbx,mby,mbz");
            logEnable = true;
            logCount = 0;
        } else {
            Log.d(TAG, "No file read/write permission");
        }
    }

    private void logStop() {
        logEnable = false;
    }

    private void write2file(long sn, long ts, long dt, float[] yg, float[] ya, float[] ym) {
        String logString = "";
        logString += String.format(Locale.US, "\n%d,%d,%d",
                sn, ts, dt);
        logString += String.format(Locale.US, ",%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f,%.10f",
                yg[0], yg[1], yg[2], ya[0], ya[1], ya[2], ym[0], ym[1], ym[2]);
        logString += String.format(Locale.US, ",%.10f,%.10f,%.10f",
                ym[3], ym[4], ym[5]);
        logFile.writeFile(logString);
        logInfoText.setText(logFile.getFileSizeString());
    }

    private Date getSystemTime() {
        return new Date(System.currentTimeMillis());
    }

    private String getSystemTimeString(final Date date, final String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    private void sensorSetup() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
        mGyroscope     = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        mMagnetometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        Log.d(TAG, String.format("mAccelerometer.vendor = %s", mAccelerometer.getVendor()));
        Log.d(TAG, String.format("mGyroscope.vendor     = %s", mGyroscope.getVendor()));
        Log.d(TAG, String.format("mMagnetField.vendor   = %s", mMagnetometer.getVendor()));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                dtns = sensorUpdateSamplingTime(event); // second
                float dt = dtns / 1000000000.0f;
                System.arraycopy(values, 0, gyr, 0, 6);
                gyr[0] -= gyr[3];
                gyr[1] -= gyr[4];
                gyr[2] -= gyr[5];
                infoText.setText(getSystemTimeString(getSystemTime(), "HH:mm:ss.SSS"));
                samplingRateText.setText(String.format(Locale.US, "%.2f Hz", 1.0/dt));
                calGyrDataText.setText(String.format(Locale.US, "%8.4f %8.4f %8.4f",
                        gyr[0], gyr[1], gyr[2]));
                Log.d(TAG_SENS, String.format("[G] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f ... %.6f",
                        gyr[0], gyr[1], gyr[2], gyr[3], gyr[4], gyr[5], dt));
                if (logEnable) {
                    write2file(++logCount, ts[1], dtns, gyr, acc, mag);
                }
                break;
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                System.arraycopy(values, 0, acc, 0, 6);
                acc[0] -= acc[3];
                acc[1] -= acc[4];
                acc[2] -= acc[5];
                calAccDataText.setText(String.format(Locale.US, "%8.4f %8.4f %8.4f",
                        acc[0], acc[1], acc[2]));
                Log.d(TAG_SENS, String.format("[A] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f",
                        acc[0], acc[1], acc[2], acc[3], acc[4], acc[5]));
                break;
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                System.arraycopy(values, 0, magc, 0, 3);
//                calMagBiasDataText.setText(String.format(Locale.US, "%8.2f %8.2f %8.2f",
//                        magc[0], magc[1], magc[2]));
//                Log.d(TAG_SENS, String.format("[MC] %12.6f %12.6f %12.6f",
//                        magc[0], magc[1], magc[2]));
//                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                System.arraycopy(values, 0, mag, 0, 6);
                mag[0] -= mag[3];
                mag[1] -= mag[4];
                mag[2] -= mag[5];
                calMagDataText.setText(String.format(Locale.US, "%8.2f %8.2f %8.2f",
                        mag[0], mag[1], mag[2]));
                calMagBiasDataText.setText(String.format(Locale.US, "%8.2f %8.2f %8.2f",
                        mag[3], mag[4], mag[5]));
                Log.d(TAG_SENS, String.format("[M] %12.6f %12.6f %12.6f ... %12.6f %12.6f %12.6f",
                        mag[0], mag[1], mag[2], mag[3], mag[4], mag[5]));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void sensorStart()
    {
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, mMagnetometerCali, SensorManager.SENSOR_DELAY_GAME);
    }

    private void sensorStop()
    {
        mSensorManager.unregisterListener(this);
    }

    private long sensorUpdateSamplingTime(SensorEvent event) {
        ts[0] = ts[1];
        ts[1] = event.timestamp;
        return (ts[1] - ts[0]);
    }

    private void vibrateDelay(long milliseconds) {
        Vibrator vibrator = (Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(milliseconds);
    }
}