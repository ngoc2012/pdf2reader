package com.luclak.pdf2reader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.pdf.PdfRenderer;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.graphics.Rect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView[] mImageViews;
//    private ParcelFileDescriptor[] mFileDescriptors;
//    private PdfRenderer[] mPdfRenderers;
//    private PdfRenderer.Page[] mCurrentPages;
//    private Bitmap[] bitmaps;
    private int currentDocument;
    private int numberPage;

    //  Vertical selection:  Alt + Shift + Insert
    private document[] documents;

    public static Point getTouchPositionFromDragEvent(View item, DragEvent event) {
        Rect rItem = new Rect();
        item.getGlobalVisibleRect(rItem);
        return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
    }

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
//        mFileDescriptors = new ParcelFileDescriptor[2];
//        mPdfRenderers = new PdfRenderer[2];
//        mCurrentPages = new PdfRenderer.Page[2];
//        bitmaps = new Bitmap[2];
        numberPage = 1;

        // Pdf imageView
        mImageViews = new ImageView[2];
        mImageViews[0] = (ImageView) findViewById(R.id.imageView);
        mImageViews[1] = (ImageView) findViewById(R.id.imageView2);
        String imageBgColor = "#E1E1E1";
        mImageViews[0].setOnTouchListener(new View.OnTouchListener() {
            float x1, x2, y1, y2, dx, dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
		        mImageViews[0].setBackgroundColor(Color.parseColor(imageBgColor));
                mImageViews[1].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 0;

                // No document selected
                if (documents[currentDocument].mCurrentPages == null) {return true;}

                switch(event.getAction()) {
                    case(MotionEvent.ACTION_DOWN):
                        x1 = event.getX();
                        y1 = event.getY();
                        break;

                    case(MotionEvent.ACTION_UP):
                        x2 = event.getX();
                        y2 = event.getY();
                        dx = x2-x1;
                        dy = y2-y1;

                        // Use dx and dy to determine the direction of the move
                        if(Math.abs(dx) > Math.abs(dy)) {
                            if(dx>10)
                                previousPage();
                            else if (dx < -10)
                                nextPage();

                        } else {
                            documents[0].movePage (dy);
                            documents[1].movePage (dy);
                        }

                        break;

                    default:
                }
                // The view needs to return true on first ACTION_DOWN event, only then it will receive successive touch events.
                return true;
            }
        });

        mImageViews[1].setOnTouchListener(new View.OnTouchListener() {
            float x1, x2, y1, y2, dx, dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mImageViews[1].setBackgroundColor(Color.parseColor(imageBgColor));
                mImageViews[0].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 1;

                // No document selected
                if (documents[currentDocument].mCurrentPages == null) {return true;}

                switch(event.getAction()) {
                    case(MotionEvent.ACTION_DOWN):
                        x1 = event.getX();
                        y1 = event.getY();
                        break;

                    case(MotionEvent.ACTION_UP):
                        x2 = event.getX();
                        y2 = event.getY();
                        dx = x2-x1;
                        dy = y2-y1;

                        // Use dx and dy to determine the direction of the move
                        if(Math.abs(dx) > Math.abs(dy)) {
                            if(dx > 10)
                                previousPage();
                            else if (dx < -10)
                                nextPage();

                        } else {
                            documents[1].movePage (dy);
                        }

                        break;

                    default:
                }
                // The view needs to return true on first ACTION_DOWN event, only then it will receive successive touch events.
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
        getFiles.getFiles(this);
        for (File f : getFiles.fileList)
            Log.d("File ", f.getAbsolutePath());
        documents[0].OpenFile("HuckFinn.pdf");
        documents[1].OpenFile("HuckFinn_vn.pdf");
    }

    public void onBtnPrevClick (View view) {
        previousPage();
    }

    public void onBtnNextClick (View view) {
        nextPage();
    }
}
