package com.luclak.pdf2reader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class BrowserActivity extends AppCompatActivity {
    private int currentDocument;

    public void goBack(File f) {
        String param = fileIO.readFromFile(getApplication().getApplicationContext(), getApplicationInfo().dataDir + "/files/param.txt");
        String[] params = param.split(",");
        String txt = "0," + f.getAbsolutePath() + "," + params[2];
        if (params[0].equalsIgnoreCase("1"))
            txt = "1," + params[1] + "," + f.getAbsolutePath();
        Log.i("pdf2reader BrowserActivity goBack", txt);
        fileIO.writeToFile(getApplication().getApplicationContext(), txt, "param.txt");
        Intent intentApp = new Intent(BrowserActivity.this, MainActivity.class);
        BrowserActivity.this.startActivity(intentApp);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        // Hide app name bar default
        getSupportActionBar().hide();

        getFiles.getFiles(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        String[] textArray = {"One", "Two", "Three", "Four"};
        setContentView(linearLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        String textBgColor = "#E1E1E1";
        for (File f : getFiles.fileList) {
//            Log.d("pdf2reader ", f.getAbsolutePath());

            TextView textView = new TextView(this);
            textView.setText(f.getName());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textView.setBackgroundColor(Color.parseColor(textBgColor));
                    goBack(f);
                }
            });
            linearLayout.addView(textView);
        }
    }


}
