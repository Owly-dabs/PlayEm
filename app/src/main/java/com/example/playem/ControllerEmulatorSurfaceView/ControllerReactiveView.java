package com.example.playem.ControllerEmulatorSurfaceView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.playem.ControllerEmulatorSurfaceView.VirtualControls.ThumbStick;
import com.example.playem.ControllerEmulatorSurfaceView.VirtualControls.VirtualControlTemplates;
import com.example.playem.ControllerEmulatorSurfaceView.interfaces.Buildable;
import com.example.playem.ControllerReactiveActivity;
import com.example.playem.PlayEmGATTService;
import com.example.playem.pipes.PipeBuilder;
import com.example.playem.pipes.PlayEmDataPipe;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerReactiveView extends SurfaceView {

    private ControlGrid controlGrid;
    private final ExecutorService inputThread;
    private final ExecutorService drawThread;
    private Rect screenBounds;
    public ControllerReactiveView(Context context, ControlGrid controlGrid,Rect screenBounds) {
        super(context);
        this.getHolder().addCallback(this.surfaceViewCallback);
        this.controlGrid = controlGrid;
        inputThread = Executors.newSingleThreadExecutor();
        drawThread = Executors.newSingleThreadExecutor();
        defPaint.setColor(Color.BLACK);
        this.screenBounds = screenBounds;

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
        //Log.i("SURFV","onTouch");
        //touchCount++;
        //inputThread.execute(()->controlGrid.onTouchEvent(event));
        controlGrid.onTouchEvent(event);
        if(controlGrid.building){
            drawThread.execute(this::drawForBuild);
        }else{
            drawThread.execute(this::drawFromComponent);
        }
        return true;
    }
    private void drawFromComponent(){
        long time = System.currentTimeMillis();
        Canvas screenC = surfaceHolder.lockHardwareCanvas();
        int maxdraw = 50;//TODO Use this as metrics for draw thread
        Queue<ControlComponent> toDraw = controlGrid.GetDrawCalls();
        //Log.i("DRAW","Draw Requested");
        while(maxdraw>=0){
            maxdraw--;
            ControlComponent ch = toDraw.poll();
            if(ch!=null)
                ch.Draw(screenC,defPaint);
            else
                break;
        }
        //screenC.drawText(String.format("%d ms",time-lasttime),150,150,defPaint);
        lasttime = time;
        surfaceHolder.unlockCanvasAndPost(screenC);
    }
    private void drawForBuild(){

        long time = System.currentTimeMillis();
        Queue<Rect> toClear = controlGrid.GetClearCalls();
        Queue<ControlComponent> toDraw = controlGrid.GetDrawCalls();
        if(toClear.size()>0||toDraw.size()>0) {
            Canvas screenC = surfaceHolder.lockHardwareCanvas();
            /*int maxclear = 50;//TODO Use this as metrics for draw thread
            while (maxclear >= 0) {
                maxclear--;
                Rect ch = toClear.poll();
                if (ch != null)
                    screenC.drawRect(ch, defPaint);
                else
                    break;
            }
            int maxdraw = 10;
            //Log.i("DRAW","Draw Requested");
            while (maxdraw >= 0) {
                maxdraw--;
                ControlComponent ch = toDraw.poll();
                if (ch != null)
                    ch.Draw(screenC, defPaint);
                else
                    break;

            }*/
            screenC.drawRect(screenBounds,defPaint);
            for(Buildable bb: controlGrid.GetComponentList()){
                bb.GetComponent().Draw(screenC,defPaint);
            }
            //screenC.drawText(String.format("%d ms",time-lasttime),150,150,defPaint);
            lasttime = time;
            surfaceHolder.unlockCanvasAndPost(screenC);
        }
    }

    public void AddComponent(VirtualControlTemplates type){
        int[] screenInfo = controlGrid.GetScreenInfo();
        ThumbStick ts = new ThumbStick(0,0,screenInfo[2],0);
        controlGrid.AddBuildableComponenet(ts,ts);
        drawThread.execute(this::drawFromComponent);
    }
    public void RemoveComponent(){
        controlGrid.RemoveComponentInFocus();
        drawThread.execute(this::drawForBuild);
    }

    public void FinishEdits(){
        controlGrid.AcceptEdits();
    }
    public void FinalizeAndBuildAll(PlayEmGATTService service){
        new PipeBuilder().BuildPipe(controlGrid.GetComponentList(),service);
    }

    public void SwitchToPlay(){
        controlGrid.building = false;
    }

    private final SurfaceHolder.Callback surfaceViewCallback = new SurfaceHolder.Callback() {
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
