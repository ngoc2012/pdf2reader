package com.luclak.pdf2reader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ImageView;

import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView[] mImageViews;
    private int currentDocument;
    private int numberPage;

    //  Vertical selection:  Alt + Shift + Insert
    private document[] documents;

    private void previousPage () {
        documents[currentDocument].currentPage = Math.max(documents[currentDocument].currentPage - numberPage,0);
        documents[currentDocument].positionPage = 0;
        documents[currentDocument].renderPage();
        TextView textPage = findViewById(R.id.textViewPage);
        textPage.setText(String.valueOf(documents[0].currentPage+1)+"/"+String.valueOf(documents[1].currentPage+1));

        fileIO.writeToFile(this, documents[currentDocument].getString(),documents[currentDocument].fileName.substring(0, documents[currentDocument].fileName.length()-3) + "txt");
    }

    private void nextPage () {
        documents[currentDocument].currentPage = Math.min(documents[currentDocument].currentPage + numberPage,documents[currentDocument].numberPage-1);
        documents[currentDocument].positionPage = 0;
        documents[currentDocument].renderPage();
        TextView textPage = findViewById(R.id.textViewPage);
        textPage.setText(String.valueOf(documents[0].currentPage+1)+"/"+String.valueOf(documents[1].currentPage+1));

        fileIO.writeToFile(this, documents[currentDocument].getString(),documents[currentDocument].fileName.substring(0, documents[currentDocument].fileName.length()-3) + "txt");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Hide app name bar default
        getSupportActionBar().hide();

        // Initiate
        numberPage = 1;

        // Pdf imageView
        mImageViews = new ImageView[2];
        mImageViews[0] = (ImageView) findViewById(R.id.imageView);
        mImageViews[1] = (ImageView) findViewById(R.id.imageView2);
        imageView mImageViews0 = new imageView();
        imageView mImageViews1 = new imageView();
        String imageBgColor = "#E1E1E1";
        mImageViews[0].setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
		        mImageViews[0].setBackgroundColor(Color.parseColor(imageBgColor));
                mImageViews[1].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 0;

                // No document selected
                if (documents[currentDocument].mCurrentPages == null) {return true;}

                mImageViews0.getMotion(event);
                if (mImageViews0.dx > 0)
                    previousPage();
                if (mImageViews0.dx < 0)
                    nextPage();
                if (Math.abs(mImageViews0.dy) > 0) {
                    documents[0].movePage (mImageViews0.dy);
                    if (documents[1].mCurrentPages == null)
                        documents[1].movePage (mImageViews0.dy);
                }

                mImageViews0.resetDxDy();

                return true;
            }
        });

        mImageViews[1].setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mImageViews[1].setBackgroundColor(Color.parseColor(imageBgColor));
                mImageViews[0].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 1;

                // No document selected
                if (documents[currentDocument].mCurrentPages == null) {return true;}

 		mImageViews0.getMotion(event);
                if (mImageViews0.dx > 0)
                    previousPage();
                if (mImageViews0.dx < 0)
                    nextPage();
                if (Math.abs(mImageViews0.dy) > 0) 
                    documents[1].movePage (mImageViews0.dy);
                

                mImageViews0.resetDxDy();
                return true;
            }
        });

        // Spinner - Zoom
        Spinner dropdownZoom = findViewById(R.id.spinnerZoom);
        String[] itemsZoom = new String[]{"100", "110", "125", "150", "200", "125"};
        ArrayAdapter<String> adapterZoom = new ArrayAdapter<>(this, R.layout.spinner_item, itemsZoom);
        dropdownZoom.setAdapter(adapterZoom);
	    dropdownZoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		        documents[currentDocument].zoom = Integer.parseInt(itemsZoom[position]);
 		        if (documents[currentDocument].mCurrentPages != null) {
                    documents[currentDocument].movePage (documents[currentDocument].positionPage);
                }

            }  
 	        @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner - Page
        Spinner dropdownPage = findViewById(R.id.spinnerPage);
        String[] itemsPage = new String[]{"1", "5", "10", "20", "50"};
        ArrayAdapter<String> adapterPage = new ArrayAdapter<>(this, R.layout.spinner_item, itemsPage);
        dropdownPage.setAdapter(adapterPage);
        dropdownPage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                numberPage = Integer.parseInt(itemsPage[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner dropdownSpeed = findViewById(R.id.spinnerSpeed);
        String[] itemsSpeed = new String[]{"1.0", "0.9", "0.8", "0.7", "0.6", "0.5", "0.4"};
        ArrayAdapter<String> adapterSpeed = new ArrayAdapter<>(this, R.layout.spinner_item, itemsSpeed);
        dropdownSpeed.setAdapter(adapterSpeed);
	    dropdownSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		        documents[currentDocument].speed = Float.parseFloat(itemsSpeed[position]);
            } 
	        @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        documents = new document[2];
        documents[0] = new document();
        documents[1] = new document();
        documents[0].context = this;
        documents[1].context = this;
        documents[0].mImageViews = (ImageView) findViewById(R.id.imageView);
        documents[1].mImageViews = (ImageView) findViewById(R.id.imageView2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentDocument = extras.getInt("currentDocument");

            ViewTreeObserver vto = documents[0].mImageViews.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
//                    Log.i("pdf2reader main ", String.valueOf(documents[0].mImageViews.getWidth()) + " ");
                    try {
                        documents[currentDocument].OpenFile(extras.getString("folder"), extras.getString("fileName"));
//                        documents[1].OpenFile(extras.getString("folder"), extras.getString("fileName"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
            });


        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//        }
        if (documents[0].mCurrentPages != null) {
            documents[0].renderPage();
//            movePage (0, documents[0].positionPage);
        }
        if (documents[1].mCurrentPages != null) {
            documents[1].renderPage();
        }
    }

    public void onBtnOpen (View view) throws IOException {

        Intent intentMain = new Intent(MainActivity.this ,
                BrowserActivity.class);
        intentMain.putExtra("currentDocument",currentDocument);
        MainActivity.this.startActivity(intentMain);
        Log.i("pdf2reader "," Main layout ");
//        documents[0].OpenFile("HuckFinn.pdf");
//        documents[1].OpenFile("HuckFinn_vn.pdf");
    }

    public void onBtnPrevClick (View view) {
        previousPage();
    }

    public void onBtnNextClick (View view) {
        nextPage();
    }

}
