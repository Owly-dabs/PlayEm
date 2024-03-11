package com.example.playem.ControllerEmulatorSurfaceView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.playem.PlayEmGATTService;
import com.example.playem.pipes.PlayEmDataPipe;
import com.example.playem.viewmodels.GattServiceState;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerReactiveView extends SurfaceView {

    private ControlGrid controlGrid;
    private ExecutorService inputThread;
    private ExecutorService drawThread;
    public ControllerReactiveView(Context context, ControlGrid controlGrid) {
        super(context);
        this.getHolder().addCallback(this.surfaceViewCallback);
        this.controlGrid = controlGrid;
        inputThread = Executors.newSingleThreadExecutor();
        drawThread = Executors.newSingleThreadExecutor();
    }

    public void SetPipe(@NonNull PlayEmDataPipe pipe){
        if(controlGrid!=null)
            controlGrid.SetPipe(pipe);
        else
            Log.e("CTRLVIEW","Tried to set pipe when control grid is null");
    }

    private Paint defPaint = new Paint();
    private SurfaceHolder surfaceHolder;
    private int touchCount = 0;
    private long lasttime=0;
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchCount++;
        inputThread.execute(()->controlGrid.onTouchEvent(event));
        drawThread.execute(this::drawFromComponent);
        return true;
    }
    private void drawFromComponent(){
        long time = System.currentTimeMillis();
        Canvas screenC = surfaceHolder.lockHardwareCanvas();
        int maxdraw = 10;
        Queue<ControlComponent> toDraw = controlGrid.GetDrawCalls();
        while(maxdraw>=0){
            maxdraw--;
            ControlComponent ch = toDraw.poll();
            if(ch!=null)
                ch.Draw(screenC,defPaint);
            else
                break;
        }
        //screenC.drawText(String.format("%d ms",time-lasttime),50,50,defPaint);
        lasttime = time;
        surfaceHolder.unlockCanvasAndPost(screenC);
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
