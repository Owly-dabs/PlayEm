package com.example.playem.ControllerEmulatorSurfaceView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.playem.R;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerReactiveView extends SurfaceView {

    private ControlGrid controlGrid;
    private final Canvas persistantCanvas;
    private ExecutorService executorPool;
    private float x=100,y=100f;
    public ControllerReactiveView(Context context, ControlGrid controlGrid) {
        super(context);
        this.getHolder().addCallback(this.surfaceViewCallback);
        this.controlGrid = controlGrid;
        persistantCanvas = new Canvas();
        oldRect.top = 50;
        oldRect.bottom = 150;
        oldRect.left = 50;
        oldRect.right = 150;
        textRect.top = 0;
        textRect.bottom = 51;
        textRect.left = 0;
        textRect.right = 150;
        executorPool = Executors.newSingleThreadExecutor();
    }
    private Rect oldRect = new Rect();
    private Rect newRect = new Rect();
    private Rect textRect = new Rect();
    private Paint defPaint = new Paint();

    private SurfaceHolder surfaceHolder;
    private int touchCount = 0;
    private long lasttime=0;
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        long time = System.currentTimeMillis();
        executorPool.execute(()->controlGrid.onTouchEvent(event));
            x+=100;
            if(x>1000){
                y+=100;
                x=100;
            }
            if(y>2000)
                y=100;

        //touchCount++;
        newRect = new Rect();
        newRect.bottom = (int) y+50;
        newRect.top = (int) y-50;
        newRect.right = (int) x+50;
        newRect.left = (int) x-50;


        Canvas screenC = surfaceHolder.lockHardwareCanvas();
        if(screenC==null)
            Log.e("CANVAS","Canvas is NULL!");
        defPaint = new Paint();
        defPaint.setColor(Color.WHITE);
        Paint bg = new Paint();
        screenC.drawRect(newRect,bg);
        screenC.drawRect(oldRect,bg);
        screenC.drawCircle(x,y,50,defPaint);
        screenC.drawRect(textRect,bg);
        screenC.drawText(String.format("%d ms",time-lasttime),50,50,defPaint);
        oldRect = newRect;
        lasttime = time;
        surfaceHolder.unlockCanvasAndPost(screenC);

        // pass elementHandler.getOutputs() to bluetooth stuff
        return true;
    }
    private SurfaceHolder.Callback surfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            surfaceHolder = holder;
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            holder.removeCallback(this);
        }
    };
}
