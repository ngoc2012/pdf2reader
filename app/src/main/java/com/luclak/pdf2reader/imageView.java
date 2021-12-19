package com.luclak.pdf2reader;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class imageView {
    private float x1, y1, x2, y2;
    public float dx=0.0f, dy=0.0f;

    public void resetDxDy() {dx=0.0f; dy=0.0f;}

    public void getMotion(MotionEvent event) {
        switch(event.getAction()) {
            case(MotionEvent.ACTION_DOWN):
                x1 = event.getX();
                y1 = event.getY();
                break;

            case(MotionEvent.ACTION_UP):
                x2 = event.getX();
                y2 = event.getY();
                float dx0 = x2-x1;
                float dy0 = y2-y1;

                // Use dx and dy to determine the direction of the move
                if((Math.abs(dx0) > Math.abs(dy0)) & (Math.abs(dx0) > 10)) {
                    dx = dx0;
                    dy = 0.0f;
                } else {
                    dx = 0.0f;
                    dy = dy0;
                }

            default:
        }
    }
}
