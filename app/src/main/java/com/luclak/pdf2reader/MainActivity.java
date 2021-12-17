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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private TextView debugText;
    private ImageView[] mImageViews;
    private ParcelFileDescriptor[] mFileDescriptors;
    private PdfRenderer[] mPdfRenderers;
    private PdfRenderer.Page[] mCurrentPages;
    private Bitmap[] bitmaps;
    private int currentDocument;
    private int numberPage;
    private boolean landMode;
    JSONObject documentsJson;
    private String folder;

    //  Vertical selection:  Alt + Shift + Insert
    private document[] documents;

    public static Point getTouchPositionFromDragEvent(View item, DragEvent event) {
        Rect rItem = new Rect();
        item.getGlobalVisibleRect(rItem);
        return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
    }

//    private void writeToFile(String data,String fileName) {
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }
//
//    private String readFromFile(String fileName) {
//
//        String ret = "";
//
//        try {
//            InputStream inputStream = this.openFileInput(fileName);
//
//            if ( inputStream != null ) {
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                String receiveString = "";
//                StringBuilder stringBuilder = new StringBuilder();
//
//                while ( (receiveString = bufferedReader.readLine()) != null ) {
//                    stringBuilder.append(receiveString);
//                }
//
//                inputStream.close();
//                ret = stringBuilder.toString();
//            }
//        }
//        catch (FileNotFoundException e) {
//            Log.e("login activity", "File not found: " + e.toString());
//        } catch (IOException e) {
//            Log.e("login activity", "Can not read file: " + e.toString());
//        }
//
//        return ret;
//    }

    private void previousPage () {
//        Spinner dropdownPage = findViewById(R.id.spinnerPage);
        documents[currentDocument].currentPage = Math.max(documents[currentDocument].currentPage - numberPage,0);
        documents[currentDocument].positionPage = 0;
        renderPage(currentDocument);
        TextView debugText = findViewById(R.id.textView);
        debugText.setText("Page "+ String.valueOf(documents[currentDocument].currentPage));
        TextView textPage = findViewById(R.id.textViewPage);
        textPage.setText(String.valueOf(documents[0].currentPage+1)+"/"+String.valueOf(documents[1].currentPage+1));

        fileIO.writeToFile(this, documents[currentDocument].getString(),documents[currentDocument].fileName.substring(0, documents[currentDocument].fileName.length()-3) + "txt");
    }

    private void nextPage () {
//        Spinner dropdownPage = findViewById(R.id.spinnerPage);
        documents[currentDocument].currentPage = Math.min(documents[currentDocument].currentPage + numberPage,documents[currentDocument].numberPage-1);
        documents[currentDocument].positionPage = 0;
        renderPage(currentDocument);
        TextView debugText = findViewById(R.id.textView);
        debugText.setText("Page " + String.valueOf(documents[currentDocument].currentPage));
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
        mFileDescriptors = new ParcelFileDescriptor[2];
        mPdfRenderers = new PdfRenderer[2];
        mCurrentPages = new PdfRenderer.Page[2];
        bitmaps = new Bitmap[2];
        documentsJson = new JSONObject();
        numberPage = 1;
//        File file = new File("pdf2reader.json");
//        if (file.exists()) {
//            readFromFile()
//        }
        landMode = false;

        // Pdf imageView
        mImageViews = new ImageView[2];
        mImageViews[0] = (ImageView) findViewById(R.id.imageView);
        mImageViews[1] = (ImageView) findViewById(R.id.imageView2);
        String imageBgColor = "#E1E1E1";
        mImageViews[0].setOnTouchListener(new View.OnTouchListener() {
            float x1, x2, y1, y2, dx, dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                TextView debugText = findViewById(R.id.textView2);
//                debugText.setText("onTouch");
		        mImageViews[0].setBackgroundColor(Color.parseColor(imageBgColor));
                mImageViews[1].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 0;

                // No document selected
                if (mCurrentPages[currentDocument] == null) {return true;}

                switch(event.getAction()) {
                    case(MotionEvent.ACTION_DOWN):
                        x1 = event.getX();
                        y1 = event.getY();
//                        debugText.setText("ACTION_DOWN " + String.valueOf(x1) + ";" + String.valueOf(y1));
                        break;

                    case(MotionEvent.ACTION_UP):
                        x2 = event.getX();
                        y2 = event.getY();
                        dx = x2-x1;
                        dy = y2-y1;

                        // Use dx and dy to determine the direction of the move
                        if(Math.abs(dx) > Math.abs(dy)) {
                            if(dx>0)
                                previousPage();
                            else
                                nextPage();

                        } else {
                            movePage (0, dy);
                            movePage (1, dy);
                        }

//                        debugText.setText("ACTION_UP: direction " + direction + ";" + String.valueOf(dx) + ";" + String.valueOf(dy));


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
                TextView debugText = findViewById(R.id.textView);
//                debugText.setText("onTouch");
                mImageViews[1].setBackgroundColor(Color.parseColor(imageBgColor));
                mImageViews[0].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 1;

                // No document selected
                if (mCurrentPages[currentDocument] == null) {return true;}

                switch(event.getAction()) {
                    case(MotionEvent.ACTION_DOWN):
                        x1 = event.getX();
                        y1 = event.getY();
//                        debugText.setText("ACTION_DOWN " + String.valueOf(x1) + ";" + String.valueOf(y1));
                        break;

                    case(MotionEvent.ACTION_UP):
                        x2 = event.getX();
                        y2 = event.getY();
                        dx = x2-x1;
                        dy = y2-y1;

                        // Use dx and dy to determine the direction of the move
                        if(Math.abs(dx) > Math.abs(dy)) {
                            if(dx>0)
                                previousPage();
                            else
                                nextPage();

                        } else {
                            movePage (1, dy);
                        }

//                        debugText.setText("ACTION_UP: direction " + direction + ";" + String.valueOf(dx) + ";" + String.valueOf(dy));


                        break;

                    default:
                }
                // The view needs to return true on first ACTION_DOWN event, only then it will receive successive touch events.
                return true;
            }
        });

//        Spinner dropdown = findViewById(R.id.spinnerDocument);
//        String[] items = new String[]{"1", "2"};
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
//        dropdown.setAdapter(adapter);
//        currentDocument = Integer.parseInt(dropdown.getSelectedItem().toString()) - 1;
//        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
// 		        currentDocument = position;
//            }
// 	        @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });

        // Spinner - Zoom
        Spinner dropdownZoom = findViewById(R.id.spinnerZoom);
        String[] itemsZoom = new String[]{"100", "110", "125", "150", "200", "125"};
        ArrayAdapter<String> adapterZoom = new ArrayAdapter<>(this, R.layout.spinner_item, itemsZoom);
        dropdownZoom.setAdapter(adapterZoom);
	    dropdownZoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		        documents[currentDocument].zoom = Integer.parseInt(itemsZoom[position]);
 		        if (mCurrentPages[currentDocument] != null) {
                    movePage (currentDocument, documents[currentDocument].positionPage);
                }

            }  
 	        @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner - Page
        Spinner dropdownPage = findViewById(R.id.spinnerPage);
//        ArrayList<String> arrayList = new ArrayList<>();
//        for (int i=0;i<100;i++) {
//            arrayList.add(String.valueOf(i));
//        }
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList);
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        dropdownPage.setAdapter(arrayAdapter);
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
//        documents[0] = new document(0, 0,100, 0,1.0f);
//        documents[1] = new document(0, 0,100, 0,1.0f);
        documents[0] = new document();
        documents[1] = new document();
        TextView debugText = findViewById(R.id.textView);
        debugText.setHeight(0);
    }

    private void movePage (int iDoc, float dy) {
//        int orientation = this.getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_PORTRAIT) {}

        float bitWidth = bitmaps[iDoc].getWidth()*100.0f/documents[iDoc].zoom;
        int startX = (int) (bitmaps[iDoc].getWidth()*0.5 - (float) bitWidth*0.5);
        float documentHeight = (float) mImageViews[iDoc].getHeight() / (float) mImageViews[iDoc].getWidth() * bitWidth;
        float documentImageHeight = bitmaps[iDoc].getHeight() * (float) mImageViews[iDoc].getWidth() / bitWidth;

        documents[iDoc].positionPage = Math.min(Math.max(documents[iDoc].positionPage - (int) dy, 0), (int) ( documentImageHeight - mImageViews[iDoc].getHeight())* (int) bitWidth/mImageViews[iDoc].getWidth());
        Bitmap bitmap1 = Bitmap.createBitmap(bitmaps[iDoc], startX, documents[iDoc].positionPage, (int) bitWidth, (int) documentHeight);
        mImageViews[iDoc].setImageBitmap(bitmap1);

        fileIO.writeToFile(this, documents[currentDocument].getString(),documents[currentDocument].fileName.substring(0, documents[currentDocument].fileName.length()-3) + "txt");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//        }

        if (mCurrentPages[0] != null) {
            renderPage(0);
//            movePage (0, documents[0].positionPage);
        }

        if (mCurrentPages[1] != null) {
            renderPage(1);
        }

    }

    private void renderPage (int iDoc) {
        TextView debugText = findViewById(R.id.textView);

        mCurrentPages[iDoc] = mPdfRenderers[iDoc].openPage(documents[iDoc].currentPage);
//        debugText.setText("mCurrentPage.getWidth() " + String.valueOf(mCurrentPage.getWidth()) + "mCurrentPage.getHeight() " + String.valueOf(mCurrentPage.getHeight()));

        int factor = 4;
        bitmaps[iDoc] =  Bitmap.createBitmap(mCurrentPages[iDoc].getWidth()*factor, mCurrentPages[iDoc].getHeight()*factor, Bitmap.Config.ARGB_8888);

        // The rectangle is represented by the coordinates of its 4 edges (left, top, right bottom)
        mCurrentPages[iDoc].render(bitmaps[iDoc], null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // close the page
        mCurrentPages[iDoc].close();

        movePage (iDoc, 0.0f);
    }

    private void OpenFile (Context context, int iDoc, String FILENAME) throws IOException {

        //        readFromFile()

        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
//            debugText.setText("file" + FILENAME + " does not exists");

            // the cache directory.
            InputStream asset = context.getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();

        } else {
//            debugText.setText("file" + FILENAME + " exists");
        }

        mFileDescriptors[iDoc] = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptors[iDoc] != null) {
            mPdfRenderers[iDoc] = new PdfRenderer(mFileDescriptors[iDoc]);
            documents[iDoc].numberPage = mPdfRenderers[iDoc].getPageCount();
            //mPdfRenderers[iDoc].close();
            documents[iDoc].fileName = FILENAME;

            Log.d("Opened ", FILENAME);

            // Check for configuration file
            String config = fileIO.readFromFile(this, FILENAME.substring(0, FILENAME.length()-3) + "txt");
            if (config != "")
                documents[iDoc].getConfig(config);

            renderPage(iDoc);
        }
        //mFileDescriptors[iDoc].close();
    }

    public void onBtnOpen (View view) throws IOException {
        String[] requiredPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
        ActivityCompat.requestPermissions(this, requiredPermissions, 0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] directoryListing = downloadFolder.listFiles();
            Log.d("Folder ", downloadFolder.getPath());
            if (directoryListing != null) {
                for (File f : directoryListing) {
                    Log.d("File ", f.getName());
                }
            } else {
                Log.d("Folder ", "not found");
            }
            folder = downloadFolder.getPath(); // /storage/emulated/0/Download
        }


        OpenFile(this, 0, "HuckFinn.pdf");
        OpenFile(this, 1, "HuckFinn_vn.pdf");
    }

    public void onBtnPrevClick (View view) {
        previousPage();
    }

    public void onBtnNextClick (View view) {
        nextPage();
    }
}
