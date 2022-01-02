package com.luclak.pdf2reader;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class fileIO {
    public static void writeToFile(Context context, String data,String FILENAME) {

        // Write file to the local folder
        String[] fileNames = FILENAME.split("/");
        String fileName = fileNames[fileNames.length-1];
//        Log.i("pdf2reader fileIO writeToFile fileName:", fileName);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("pdf2reader Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFromFile(Context context, String fileName) {
        //Log.i("pdf2reader fileIO", ": " + fileName);

        String ret = "";
        File file = new File(fileName);
        if(!file.exists())
            return ret;
        
        try {
            // The openFileInput method doesn't accept path separators
//            InputStream inputStream = context.openFileInput(fileName);
            InputStream inputStream = new FileInputStream (new File(fileName));  

            if ( inputStream != null ) {
                // Log.i("pdf2reader fileIO", "inputStream != null");
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("pdf2reader login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("pdf2reader login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
