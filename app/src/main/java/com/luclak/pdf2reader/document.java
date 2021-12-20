package com.luclak.pdf2reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class document {
    public String fileName="";
    public int currentPage=0;
    public int numberPage=0;
    public int zoom=100;
    public int positionPage=0;
    public float speed=1.0f;

    public ImageView mImageViews;
    public ParcelFileDescriptor mFileDescriptors;
    public PdfRenderer mPdfRenderers;
    public PdfRenderer.Page mCurrentPages;
    public Bitmap bitmaps;
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
    public void getConfig(String config) {
        String[] configs = config.split(",");
        this.currentPage = Integer.parseInt(configs[0]);
        this.numberPage = Integer.parseInt(configs[1]);
        this.zoom = Integer.parseInt(configs[2]);
        this.positionPage = Integer.parseInt(configs[3]);
        this.speed = Float.parseFloat(configs[4]);
    }

    public void movePage (float dy) {
        float bitWidth = this.bitmaps.getWidth()*100.0f/this.zoom;
        int startX = (int) (this.bitmaps.getWidth()*0.5 - (float) bitWidth*0.5);
        float documentHeight = (float) this.mImageViews.getHeight() / (float) this.mImageViews.getWidth() * bitWidth;
        float documentImageHeight = this.bitmaps.getHeight() * (float) this.mImageViews.getWidth() / bitWidth;
        Log.i("pdf2reader movePage ", String.valueOf(this.mImageViews.getWidth()) + " ");
        this.positionPage = Math.min(Math.max(this.positionPage - (int) dy, 0), (int) ( documentImageHeight - this.mImageViews.getHeight())* (int) bitWidth/this.mImageViews.getWidth());

        Bitmap bitmap1 = Bitmap.createBitmap(this.bitmaps, startX, this.positionPage, (int) bitWidth, (int) documentHeight);
        this.mImageViews.setImageBitmap(bitmap1);

        fileIO.writeToFile(context, this.getString(),this.fileName.substring(0, this.fileName.length()-3) + "txt");
        
    }

    public void renderPage () {

        this.mCurrentPages = this.mPdfRenderers.openPage(this.currentPage);
//       ("mCurrentPage.getWidth() " + String.valueOf(mCurrentPage.getWidth()) + "mCurrentPage.getHeight() " + String.valueOf(mCurrentPage.getHeight()));

        int factor = 4;
        this.bitmaps =  Bitmap.createBitmap(this.mCurrentPages.getWidth()*factor, this.mCurrentPages.getHeight()*factor, Bitmap.Config.ARGB_8888);

        // The rectangle is represented by the coordinates of its 4 edges (left, top, right bottom)
        this.mCurrentPages.render(this.bitmaps, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // close the page
        this.mCurrentPages.close();

        this.movePage(0.0f);
    }

    public void OpenFile (String folder, String FILENAME) throws IOException {
//        Log.d("pdf2reader OpenFile ", folder + FILENAME);
        File file = new File(folder, FILENAME);
        if (!file.exists()) {

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

        }

        this.mFileDescriptors = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (this.mFileDescriptors != null) {
            this.mPdfRenderers = new PdfRenderer(this.mFileDescriptors);
            this.numberPage = this.mPdfRenderers.getPageCount();
            //this.mPdfRenderers.close();
            this.fileName = FILENAME;

            Log.d("Opened ", FILENAME);

//            // Check for configuration file
//            String config = fileIO.readFromFile(context, FILENAME.substring(0, FILENAME.length()-3) + "txt");
//            if (config != "")
//                this.getConfig(config);
//            this.currentPage = Math.min(this.numberPage-1, this.currentPage);

            renderPage();
        }
        //this.mFileDescriptors.close();
    }
}