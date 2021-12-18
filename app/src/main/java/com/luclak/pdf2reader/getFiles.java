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

public class getFiles {
    public static void getFiles(Activity activity) {
        String[] requiredPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
        ActivityCompat.requestPermissions(activity, requiredPermissions, 0);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] directoryListing = downloadFolder.listFiles();
            Log.d("Folder ", downloadFolder.getPath());
            if (directoryListing != null) {
                for (File f : directoryListing) {
                    Log.d("File ", f.getName());
//                    f.get
                }
            } else {
                Log.d("Folder ", "not found");
            }
//            folder = downloadFolder.getPath(); // /storage/emulated/0/Download
        }
    }
}
