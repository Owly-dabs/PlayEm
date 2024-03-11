package com.example.playem.ControllerEmulatorSurfaceView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowMetrics;

import androidx.constraintlayout.widget.ConstraintSet;

import com.example.playem.PlayEmGATTService;
import com.example.playem.generics.ConcurrentListBackHashMap;
import com.example.playem.pipes.PlayEmDataPipe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import kotlin.jvm.functions.Function2;

public class ControlGrid {
    public static final int ORIENTATION_POTRAIT=0;
    public static final int ORIENTATION_LANDSCAPE=1;
    private float gridHeight,gridWidth;
    private final float screenActualWidth,screenActualHeight, density;
    private final int gridPixelLength,gridPixelWidth;
    private int arrX,arrY;
    private final ControlComponent[][] grid;
    public int GetGridWidth(){
        return arrX;
    }
    public int GetGridHeight(){
            return arrY;
    }
    public ControlGrid(Activity activity, float min_physical_unit_len_inches,int orientation){ //
        //WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
        WindowMetrics maxWindowMetrics = activity.getWindowManager().getMaximumWindowMetrics();
        DisplayCutout dc = maxWindowMetrics.getWindowInsets().getDisplayCutout();
        float cutoutBottom = 0f;
        float cutoutRight = 0f;
        float cutoutLeft = 0f;
        float cutoutTop = 0f;
        if(dc!=null){
            cutoutTop = dc.getSafeInsetTop();
            cutoutRight = dc.getSafeInsetRight();
            cutoutLeft = dc.getSafeInsetLeft();
            cutoutBottom = dc.getSafeInsetTop();
        }
        density =  activity.getResources().getDisplayMetrics().density*DisplayMetrics.DENSITY_DEFAULT; //approxiamtely screendpi
        gridPixelLength =  maxWindowMetrics.getBounds().height();
        gridPixelWidth = maxWindowMetrics.getBounds().width();

        screenActualWidth =  (gridPixelWidth-cutoutRight-cutoutLeft) / density;
        screenActualHeight = (gridPixelLength-cutoutTop-cutoutBottom) / density;

        gridWidth=orientation==ORIENTATION_POTRAIT? screenActualWidth:screenActualHeight;
        gridHeight=orientation==ORIENTATION_POTRAIT? screenActualHeight:screenActualWidth;

        gridWidth= gridWidth/min_physical_unit_len_inches;
        gridHeight= gridHeight/min_physical_unit_len_inches;
        Log.e("GRID",String.format("%f %f %f %f",cutoutTop,cutoutRight,cutoutBottom,cutoutLeft));
        Log.e("GRID",String.format("%f %f",screenActualWidth,screenActualHeight));
        Log.e("GRID",String.format("%f %f",gridWidth,gridHeight));
        drawCalls = new ConcurrentLinkedQueue<>();
        grid = new ControlComponent[(int)gridWidth][(int)gridHeight];
        //gridHeight = scr
    }
    private final ConcurrentListBackHashMap<Integer,ControlComponent> ActivePointers = new ConcurrentListBackHashMap<>();
    private final ConcurrentLinkedQueue<ControlComponent> drawCalls;
    private PlayEmDataPipe dataPipe;
    private ControlComponent getControlHandler(float x, float y){
        int arrXidx = (int)(x/gridPixelWidth*gridWidth)-1;
        int arrYidx = (int)(y/gridPixelLength*gridHeight)-1;
        //Log.w("CTLGRID",String.format("%d x %d y",arrXidx,arrYidx));
        return grid[arrXidx][arrYidx];
    }
    public Queue<ControlComponent> GetDrawCalls(){
        return drawCalls;
    }
    public void SetPipe(PlayEmDataPipe dataPipe){
        dataPipe = dataPipe;
    }

    private boolean pipeFramePush = false;

    private void handlePack(ControlHandler.ValuePack[] pack){
        pipeFramePush = true;
        switch(pack[0].type){
            case AXES:
                dataPipe.UpdateAxis(pack[0].id,pack[0].x);
                break;
            case AXES2:
                dataPipe.UpdateAxis(pack[0].id,pack[0].x);
                dataPipe.UpdateAxis(pack[0].id+1,pack[0].y);
                break;
            case BUTTONS:
                dataPipe.UpdateButtonNumber(pack[0].id,pack[0].x>0);
                break;
            default:
                pipeFramePush = false||pipeFramePush;
                break;
        }
    }

    public void newPointer(MotionEvent event,int id,int idx){
        if(ActivePointers.ContainsKey(id)){
            Log.e("CONTROL","UP or CANCEL event not registered, DOWN received with active pointer in cache");
        }
        float x = event.getX(idx);
        float y = event.getY(idx);
        ControlComponent ch = getControlHandler(x,y);
        if(ch!=null) {
            ActivePointers.AddorUpdate(id, ch);
            handlePack(ch.handler.onEnter(x,y,id));
            drawCalls.add(ch);
        }
    }

    private void movePointer(MotionEvent event,int id ,int idx){
        float x = event.getX(idx);
        float y = event.getY(idx);
        handlePack(Objects.requireNonNull(ActivePointers.Get(id)).handler.onMove(x,y,id));
    }

    private void dropPointer(MotionEvent event,int id, int idx){
        float x = event.getX(idx);
        float y = event.getY(idx);
        if(ActivePointers.ContainsKey(id)){
         handlePack(Objects.requireNonNull(ActivePointers.Get(id)).handler.onExit(x,y,id));
        }
    }
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                newPointer(event,0,0);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                int idx = event.getActionIndex();
                int id = event.getPointerId(idx);
                newPointer(event,id,idx);
                break;
            case MotionEvent.ACTION_MOVE:
                for(int i :ActivePointers.GetKeys()) {
                    int idx1 = event.findPointerIndex(i);
                    if(idx1>-1)
                        movePointer(event,i,idx1);
                    else
                        Log.w("CONTROL", "MOVE received but Active pointer not registered");
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int idx2 = event.getActionIndex();
                int id2 = event.getPointerId(idx2);
                dropPointer(event,id2,idx2);
                break;
            case MotionEvent.ACTION_UP:
                int id3 = event.getPointerId(0);
                dropPointer(event,id3,0);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.w("CONTROL","CANCEL received, Unsupported input handling");
        }
        if(pipeFramePush){
            dataPipe.PushFrame();
            dataPipe.NotifyDataReady();
        }
        pipeFramePush = false;
        return true;
    }

    public boolean AddControlComponent(ControlComponent component){
        if(component==null)
            return false;

        if(!CheckOverlap(component))
            return false;

        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;i++){
                grid[i][j] = component;
            }
        }
        return true;
    }

    public boolean RemoveComponent(ControlComponent component){
        if(CheckOverlap(component)){
            for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
                for(int j = component.positionY;j<component.positionY+component.heightSteps;i++){
                    grid[i][j] = null;
                }
            }
            return true;
        }
        return false;
    }

    public boolean CheckOverlap(ControlComponent component){
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;i++){
                if(grid[i][j]!=null)
                    return false;
            }
        }
        return true;
    }
}
