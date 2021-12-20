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
        Intent intentApp = new Intent(BrowserActivity.this,
                MainActivity.class);
        intentApp.putExtra("fileName", f.getName());
        intentApp.putExtra("folder", f.getParent());
        intentApp.putExtra("currentDocument", currentDocument);
        BrowserActivity.this.startActivity(intentApp);
        Log.i("pdf2reader ",f.getAbsolutePath());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        // Hide app name bar default
//        getSupportActionBar().hide();

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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentDocument = extras.getInt("currentDocument");
            //The key argument here must match that used in the other activity
        }
    }


}