package com.luclak.pdf2reader;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

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
}
