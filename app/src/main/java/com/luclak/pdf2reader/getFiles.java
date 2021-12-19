package com.luclak.pdf2reader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class getFiles {

    private static List<File> fileList = new ArrayList<File>();

    private static void getFilesList(File[] directoryListing) {
        if (directoryListing != null) {
            for (File f : directoryListing) {
//                Log.d("File ", f.getAbsolutePath());
                if (f.isFile()) {
//                    Log.d("File ", "is file");
                    fileList.add(f);
                } else {
//                    Log.d("File ", "is folder");
                    getFilesList(f.listFiles());
                }
            }
        } else {
            Log.d("Folder ", "not found");
        }
    }
    public static void getFiles(Activity activity) {
        String[] requiredPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
        ActivityCompat.requestPermissions(activity, requiredPermissions, 0);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] directoryListing = downloadFolder.listFiles();
            Log.d("Folder ", downloadFolder.getPath());
            getFilesList(directoryListing);
            for (File f : fileList) {
                Log.d("File ", f.getAbsolutePath());
            }
//            folder = downloadFolder.getPath(); // /storage/emulated/0/Download
        }
    }
}