package com.kitsprout.system;

import android.util.Log;

public class ksDevice {

    private static final String TAG = "KS-DEVICE";

    private static String getManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    private static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    private static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    private static String getSystemBootloader() {
        return android.os.Build.BOOTLOADER;
    }

    private static String getSystemCPU() {
        return android.os.Build.HARDWARE;
    }

    private static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static void showSystemParameter() {
        Log.d(TAG, ">> Phone Information");
        Log.d(TAG, "MANUFACTURER: " + getManufacturer());
        Log.d(TAG, "BRAND       : " + getDeviceBrand());
        Log.d(TAG, "MODEL       : " + getSystemModel());
//        Log.d(TAG, "BOOTLOADER  : " + getSystemBootloader());
        Log.d(TAG, "CPU         : " + getSystemCPU());
        Log.d(TAG, "VERSION     : " + getSystemVersion());
    }

    public static String getSystemInformationString() {
        String info = "";
        info += getDeviceBrand() + ", " + getSystemModel() + ", " + getSystemCPU() + ", Android " + getSystemVersion();
//        Log.d(TAG, "INFO: " + info);
        return info;
    }
}
