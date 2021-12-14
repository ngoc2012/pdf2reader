package com.luclak.pdf2reader;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Point;
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
//import android.content.res.AssetManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    private TextView debugText;
    private ImageView[] mImageViews;
    private ParcelFileDescriptor[] mFileDescriptors;
    private PdfRenderer[] mPdfRenderers;
    private PdfRenderer.Page[] mCurrentPages;
    private Bitmap[] bitmaps;
    private int currentDocument;

    //    Alt + Shift + Insert
    private class document {
        public int currentPage=0;
        public int numberPage=0;
        public int zoom=100;
        public int positionPage=0;
        public float speed=1.0f;
        document(int currentPage, int numberPage, int zoom, int positionPage, float speed) {
            this.currentPage=currentPage;
            this.numberPage=numberPage;
            this.zoom=zoom;
            this.positionPage=positionPage;
            this.speed=speed;
        }
    }

    private document[] documents;

    /**
     * @param item  the view that received the drag event
     * @param event the event from {@link android.view.View.OnDragListener#onDrag(View, DragEvent)}
     * @return the coordinates of the touch on x and y axis relative to the screen
     */
    public static Point getTouchPositionFromDragEvent(View item, DragEvent event) {
        Rect rItem = new Rect();
        item.getGlobalVisibleRect(rItem);
        return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
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

        // Pdf imageView
        mImageViews = new ImageView[2];
        mImageViews[0] = (ImageView) findViewById(R.id.imageView);
        mImageViews[1] = (ImageView) findViewById(R.id.imageView2);

        mImageViews[0].setOnTouchListener(new View.OnTouchListener() {
            float x1, x2, y1, y2, dx, dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                TextView debugText = findViewById(R.id.textView2);
//                debugText.setText("onTouch");
		        mImageViews[0].setBackgroundColor(Color.parseColor("#52D017"));
                mImageViews[1].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 0;
                String direction;
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
                                direction = "right";
                            else
                                direction = "left";
                        } else {
                            if(dy>0)
                                direction = "down";
                            else
                                direction = "up";
                        }

//                        debugText.setText("ACTION_UP: direction " + direction + ";" + String.valueOf(dx) + ";" + String.valueOf(dy));

                        movePage (0, dy);
                        movePage (1, dy);
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
                mImageViews[1].setBackgroundColor(Color.parseColor("#52D017"));
                mImageViews[0].setBackgroundColor(Color.parseColor("#ffffff"));
                currentDocument = 1;
                String direction;
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
                                direction = "right";
                            else
                                direction = "left";
                        } else {
                            if(dy>0)
                                direction = "down";
                            else
                                direction = "up";
                        }

//                        debugText.setText("ACTION_UP: direction " + direction + ";" + String.valueOf(dx) + ";" + String.valueOf(dy));

                        movePage (1, dy);
                        break;

                    default:
                }
                // The view needs to return true on first ACTION_DOWN event, only then it will receive successive touch events.
                return true;
            }
        });

        Spinner dropdown = findViewById(R.id.spinnerDocument);
        String[] items = new String[]{"1", "2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        dropdown.setAdapter(adapter);
        currentDocument = Integer.parseInt(dropdown.getSelectedItem().toString()) - 1;
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		        currentDocument = position;
            }  
 	        @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
 		        if (mCurrentPages[currentDocument] != null) {
                    movePage (currentDocument, documents[currentDocument].positionPage);
                }

            }  
 	        @Override
            public void onNothingSelected(AdapterView<?> parent) {}
       });


        Spinner dropdownPage = findViewById(R.id.spinnerPage);
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i=0;i<100;i++) {
            arrayList.add(String.valueOf(i));
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownPage.setAdapter(arrayAdapter);

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
        documents[0] = new document(8, 0,100, 0,1.0f);
        documents[1] = new document(3, 0,100, 0,1.0f);

    }

    private void movePage (int iDoc, float dy) {

        float bitWidth = bitmaps[iDoc].getWidth()*100.0f/documents[iDoc].zoom;
        int startX = (int) (bitmaps[iDoc].getWidth()*0.5 - (float) bitWidth*0.5);
        float documentHeight = (float) mImageViews[iDoc].getHeight() / (float) mImageViews[iDoc].getWidth() * bitWidth;
        float documentImageHeight = bitmaps[iDoc].getHeight() * (float) mImageViews[iDoc].getWidth() / bitWidth;
//        debugText.setText(String.valueOf(mImageView.getHeight()) + " " + String.valueOf(bitmap.get(document).getHeight()) );

//        debugText.setText(String.valueOf(mImageView.getHeight()) + " " + String.valueOf(bitmap.get(document).getHeight()) + " " + String.valueOf((int) documentHeight));
//        debugText.setText(String.valueOf(bitWidth) + " " + String.valueOf(startX) + " " + String.valueOf(bitmap.get(document).getWidth()) + " " + String.valueOf(zoom.get(document)));

        documents[iDoc].positionPage = Math.min(Math.max(documents[iDoc].positionPage - (int) dy, 0), (int) ( documentImageHeight - mImageViews[iDoc].getHeight())* (int) bitWidth/mImageViews[iDoc].getWidth());

        Bitmap bitmap1 = Bitmap.createBitmap(bitmaps[iDoc], startX, documents[iDoc].positionPage, (int) bitWidth, (int) documentHeight);

        TextView debugText = findViewById(R.id.textView);
//        debugText.setText("positionPage: direction " + positionPage.get(document) + ";" + String.valueOf(dy) + ";" + String.valueOf(mImageView.getHeight()) + ";" + String.valueOf(documentImageHeight) + ";" + String.valueOf(bitmap.get(document).getHeight()));
        mImageViews[iDoc].setImageBitmap(bitmap1);
    }

    private void renderPage (int iDoc) {
        TextView debugText = findViewById(R.id.textView);

        mCurrentPages[iDoc] = mPdfRenderers[iDoc].openPage(documents[iDoc].currentPage);
//        debugText.setText("mCurrentPage.getWidth() " + String.valueOf(mCurrentPage.getWidth()) + "mCurrentPage.getHeight() " + String.valueOf(mCurrentPage.getHeight()));

        bitmaps[iDoc] =  Bitmap.createBitmap(mCurrentPages[iDoc].getWidth(), mCurrentPages[iDoc].getHeight(), Bitmap.Config.ARGB_8888);
        // The rectangle is represented by the coordinates of its 4 edges (left, top, right bottom)
        mCurrentPages[iDoc].render(bitmaps[iDoc], null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // close the page
        mCurrentPages[iDoc].close();

//
//        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.get(document), startX, 0, (int) bitWidth, (int) documentHeight);
//
//        mImageView.setImageBitmap(bitmap1);

        movePage (iDoc, 0.0f);
    }

    private void OpenFile (Context context, int iDoc, String FILENAME) throws IOException {

        TextView debugText = findViewById(R.id.textView);
        debugText.setText(context.getCacheDir().toString());

        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            debugText.setText("file" + FILENAME + " does not exists");

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
            debugText.setText("file" + FILENAME + " exists");
        }

        mFileDescriptors[iDoc] = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptors[iDoc] != null) {
            mPdfRenderers[iDoc] = new PdfRenderer(mFileDescriptors[iDoc]);
            documents[iDoc].numberPage = mPdfRenderers[iDoc].getPageCount();
            renderPage(iDoc);
            //mPdfRenderers[iDoc].close();
        }
        //mFileDescriptors[iDoc].close();
    }

    public void onBtnOpen (View view) throws IOException {
        OpenFile(this, 0, "HuckFinn.pdf");
        OpenFile(this, 1, "HuckFinn_vn.pdf");
    }

    public void onBtnPrevClick (View view) {
	documents[currentDocument].currentPage = Math.max(documents[currentDocument].currentPage-1,0);
	renderPage(currentDocument);
        TextView debugText = findViewById(R.id.textView);
        debugText.setText("Prev"+ String.valueOf(documents[currentDocument].currentPage));
    }

    public void onBtnNextClick (View view) {
	documents[currentDocument].currentPage = Math.min(documents[currentDocument].currentPage+1,documents[currentDocument].numberPage-1);
	renderPage(currentDocument);
        TextView debugText = findViewById(R.id.textView);
        debugText.setText("Next" + " " + String.valueOf(currentDocument)  + " " + String.valueOf(documents[currentDocument].currentPage));
    }
}
