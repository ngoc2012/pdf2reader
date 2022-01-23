package com.luclak.pdf2reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
//import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.Arrays;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class document {
    public String fileName="";
    public String shortFileName="";
    public int currentPage=0;
    public int numberPage=0;
    public int zoom=100;
    public int positionPage=0;
    public float speed=1.0f;

    public ImageView mImageViews;
    public ParcelFileDescriptor mFileDescriptors;
    public PdfRenderer mPdfRenderers;
    public PdfRenderer.Page mCurrentPages;
    public PdfRenderer.Page mNextPages;
    public Bitmap bitmaps;
    public Bitmap nextBitmaps;
    public Context context;

    //        document(int currentPage, int numberPage, int zoom, int positionPage, float speed) {
//        this.currentPage=currentPage;
//        this.numberPage=numberPage;
//        this.zoom=zoom;
//        this.positionPage=positionPage;
//        this.speed=speed;
//    }
    public String getJson() {
        return ("{\n" +
                "currentPage:" + String.valueOf(currentPage) + "\n," +
                "numberPage:" + String.valueOf(numberPage) + "\n," +
                "zoom:" + String.valueOf(zoom) + "\n," +
                "positionPage:" + String.valueOf(positionPage) + "\n," +
                "speed:" + String.valueOf(speed) + "\n," +
                "}");
    }

    public String getString() {
        return (String.valueOf(currentPage) + "," + String.valueOf(numberPage) + "," + String.valueOf(zoom) + "," + String.valueOf(positionPage) + "," + String.valueOf(speed));
    }

    public void getConfig() {
        // Check for configuration file
        String configFile = this.shortFileName.substring(0, this.shortFileName.length()-3) + "txt";
        // /data/user/0/com.luclak.pdf2reader/files/book.txt
        String config = fileIO.readFromFile(context, context.getApplicationInfo().dataDir + "/files/" + configFile);
        // Log.i("pdf2reader param ", "++++" + context.getApplicationInfo().dataDir + "/files/" + configFile + "++++");
        if (config != "") {
            String[] configs = config.split(",");
            this.currentPage = Integer.parseInt(configs[0]);
            this.numberPage = Integer.parseInt(configs[1]);
            this.zoom = Integer.parseInt(configs[2]);
            this.positionPage = Integer.parseInt(configs[3]);
            this.speed = Float.parseFloat(configs[4]);
        } else {
            fileIO.writeToFile(context, "", configFile);
        }
    }

    public void previousPage() {
        currentPage = Math.max(currentPage - 1, 0);
        positionPage = 0;
        renderPage();
        //TextView textPage = context.findViewById(R.id.textViewPage);
        //textPage.setText(String.valueOf(context.documents[0].currentPage+1)+"/"+String.valueOf(context.documents[1].currentPage+1));

        fileIO.writeToFile(context, getString(),fileName.substring(0, fileName.length()-3) + "txt");
    }

    public void nextPage() {
        currentPage = Math.min(currentPage + 1, numberPage-1);
        positionPage = 0;
        renderPage();
        //TextView textPage = context.findViewById(R.id.textViewPage);
        //textPage.setText(String.valueOf(context.documents[0].currentPage+1)+"/"+String.valueOf(context.documents[1].currentPage+1));

        fileIO.writeToFile(context, getString(), fileName.substring(0, fileName.length()-3) + "txt");
    }

    public void movePage (float dy) {
        float bitWidth = this.bitmaps.getWidth()*100.0f/this.zoom;
        int startX = (int) (this.bitmaps.getWidth()*0.5 - (float) bitWidth*0.5);

//        // Height maximal of the document
//        float documentHeight = (float) this.mImageViews.getHeight() / (float) this.mImageViews.getWidth() * bitWidth;
//
//        float documentImageHeight = this.bitmaps.getHeight() * (float) this.mImageViews.getWidth() / bitWidth;
//        //Log.i("pdf2reader document movePage ", String.valueOf(this.mImageViews.getWidth()) + " ");
//
//        Log.i("pdf2reader document movePage ", String.valueOf(speed) + " " +  String.valueOf((int) dy) + " " +  String.valueOf((int) (dy*speed)) + " " + String.valueOf(this.positionPage));
//
//        this.positionPage = Math.min(Math.max(this.positionPage - ((int) (dy*speed)), 0), (int) ( documentImageHeight - this.mImageViews.getHeight())* (int) bitWidth/this.mImageViews.getWidth());
//
//        Bitmap bitmap1 = Bitmap.createBitmap(this.bitmaps, startX, this.positionPage, (int) bitWidth, (int) documentHeight);
        this.positionPage = Math.max(Math.min(this.positionPage - ((int) (dy*speed)), this.bitmaps.getHeight() - 100), 0);
        int hmax = (int) (this.mImageViews.getHeight() * bitWidth / this.mImageViews.getWidth());
//        Log.i("pdf2reader document movePage ", this.fileName + ":" + String.valueOf((int) dy) + " " + String.valueOf(this.positionPage));
//        Log.i("pdf2reader document movePage ", this.shortFileName + ":" +  this.getString());

        if (this.positionPage > (this.bitmaps.getHeight() - this.nextBitmaps.getHeight())) {
            nextPage();
        } else {
            Bitmap bitmap1 = Bitmap.createBitmap(this.bitmaps, startX, this.positionPage, (int) bitWidth, Math.min( this.bitmaps.getHeight() - this.positionPage, hmax));
            this.mImageViews.setImageBitmap(bitmap1);
            fileIO.writeToFile(context, this.getString(),this.fileName.substring(0, this.fileName.length()-3) + "txt");
        }   
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(Math.max(bmp1.getWidth(), bmp2.getWidth()), bmp1.getHeight() + bmp2.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        //canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp1, 0f, 0f, null);
        canvas.drawBitmap(bmp2, 0f, bmp1.getHeight(), null);
        
        Paint paint = new Paint();
        canvas.drawLine(0, bmp1.getHeight(), Math.max(bmp1.getWidth(), bmp2.getWidth()), bmp1.getHeight(), paint);

        bmp1.recycle();
        bmp2.recycle();
        return bmOverlay;
    }

    public void renderPage () {

        this.mCurrentPages = this.mPdfRenderers.openPage(this.currentPage);
//       ("mCurrentPage.getWidth() " + String.valueOf(mCurrentPage.getWidth()) + "mCurrentPage.getHeight() " + String.valueOf(mCurrentPage.getHeight()));
        int factor = 4;
        //Log.i("pdf2reader document renderPage ", String.valueOf(this.mCurrentPages.getWidth()) + " " + String.valueOf(this.mCurrentPages.getWidth()*factor));
        this.bitmaps =  Bitmap.createBitmap(this.mCurrentPages.getWidth()*factor, this.mCurrentPages.getHeight()*factor, Bitmap.Config.ARGB_8888);

        // The rectangle is represented by the coordinates of its 4 edges (left, top, right bottom)
        this.mCurrentPages.render(this.bitmaps, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // close the page
        this.mCurrentPages.close();
        
        if (this.currentPage < (this.numberPage-1)) {
            this.mNextPages = this.mPdfRenderers.openPage(this.currentPage + 1);
            this.nextBitmaps =  Bitmap.createBitmap(this.mNextPages.getWidth()*factor, this.mNextPages.getHeight()*factor, Bitmap.Config.ARGB_8888);
            this.mNextPages.render(this.nextBitmaps, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            this.mNextPages.close();
            this.bitmaps = overlay(this.bitmaps, this.nextBitmaps);
        }

        this.movePage(0.0f);
    }

    public void OpenFile (String FILENAME) throws IOException {
//        Log.d("pdf2reader OpenFile ", folder + FILENAME);
        // /storage/emulated/0/Download/Donquixote/book.pdf
        String[] fileNames = FILENAME.split("/");

        String[] folders = Arrays.copyOfRange(fileNames, 0, fileNames.length-1);
        String folder = String.join("/", folders) + "/";

        this.shortFileName = fileNames[fileNames.length-1];

        File file = new File(folder, this.shortFileName);
        if (!file.exists()) {
            Log.i("pdf2reader document OpenFile ", " File not found");

            // the cache directory.
            InputStream asset = context.openFileInput(this.shortFileName);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();

        }

        this.mFileDescriptors = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (this.mFileDescriptors != null) {
            this.mPdfRenderers = new PdfRenderer(this.mFileDescriptors);
            this.numberPage = this.mPdfRenderers.getPageCount();
            //this.mPdfRenderers.close();
            this.fileName = FILENAME;

            Log.d("pdf2reader document OpenFile Opened ", FILENAME);

            this.getConfig();

            this.currentPage = Math.min(this.numberPage-1, this.currentPage);
            Log.i("pdf2reader document OpenFile ", this.shortFileName + ":" +  this.getString());

            renderPage();
        }
        //this.mFileDescriptors.close();
    }
}
