package com.kitsprout.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class CsvFile {

    private static final String TAG = "KS-FILE";

    private final String defaultFolder = "ksLogger";

    private File file;
    private String fileName = "";
    private String defaultFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/' + defaultFolder;
    private String subFolderPath = "";

    public CsvFile() {

    }

    public CsvFile(String subFolder) {
        this.subFolderPath = defaultFolderPath + '/' + subFolder;
        createFolder(this.subFolderPath);
    }

    public CsvFile(String subFolder, String fileName) {
        this.fileName = fileName;
        this.subFolderPath = defaultFolderPath + '/' + subFolder;
        createFolder(this.subFolderPath);
        createFile(this.subFolderPath, fileName);
    }

    public CsvFile(String defaultFolderPath, String subFolder, String fileName) {
        this.fileName = fileName;
        this.defaultFolderPath = defaultFolderPath;
        this.subFolderPath = defaultFolderPath + '/' + subFolder;
        createFolder(this.subFolderPath);
        createFile(this.subFolderPath, fileName);
    }

    private void createFolder(String folder) {
        File wallpaperDirectory = new File(folder);
        wallpaperDirectory.mkdirs();
    }

    public boolean createFile(String fileName) {
        Log.d(TAG, "subFolderPath: " + this.subFolderPath + ", fileName: " + fileName);
        boolean status = false;
        try {
            createFolder(this.subFolderPath);
            this.fileName = fileName;
            this.file = new File(this.subFolderPath, this.fileName);
            if (!this.file.exists()) {
                this.file.createNewFile();
            }
            status = true;
            Log.d(TAG, "Create file: " + this.subFolderPath + "/" + this.fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed in createFile(): " + e.toString());
        }

        return status;
    }

    private boolean createFile(String folder, String fileName) {
        boolean status = false;
        try {
            this.file = new File(folder, fileName);
            if (!this.file.exists()) {
                this.file.createNewFile();
            }
            status = true;
            Log.d(TAG, "Create file: " + folder + "/" + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed in createFile(): " + e.toString());
        }

        return status;
    }

    public void writeFile(String writeString) {
        try {
            FileWriter fw = new FileWriter(this.file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(writeString);
            bw.close();
            Log.d(TAG,"Writing to log, size: " + getFileSizeString());
        } catch (IOException e) {
            Log.e(TAG, "Failed in writeFile(): " + e.toString());
        }
    }

    public String getFileName() {
        return this.fileName;
    }

    public long getFileSize() {
        return file.length();
    }

    public String getFileSizeString() {
        float lens = getFileSize();
        String sizeString;
        if (lens > 1024 * 1024)
        {
            lens /= 1024 * 1024;
            sizeString = String.format(Locale.US,"%.3f MB", lens);
        }
        else if (lens > 1024)
        {
            lens /= 1024;
            sizeString = String.format(Locale.US,"%.3f KB", lens);
        }
        else
        {
            sizeString = String.format(Locale.US,"%.0f B", lens);
        }
        return sizeString;
    }
}
