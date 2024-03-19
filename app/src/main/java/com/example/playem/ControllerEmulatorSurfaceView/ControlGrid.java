package com.example.playem.ControllerEmulatorSurfaceView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowMetrics;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintSet;

import com.example.playem.ControllerEmulatorSurfaceView.VirtualControls.VirtualControlTemplates;
import com.example.playem.ControllerEmulatorSurfaceView.interfaces.Buildable;
import com.example.playem.ControllerReactiveActivity;
import com.example.playem.PlayEmGATTService;
import com.example.playem.generics.ConcurrentListBackHashMap;
import com.example.playem.hid.HIDProfileBuilder;
import com.example.playem.pipes.PlayEmDataPipe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    private ControllerReactiveActivity parentActivity;
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
        parentActivity = (ControllerReactiveActivity) activity; //Disgusting downcast TODO
        density =  activity.getResources().getDisplayMetrics().density*DisplayMetrics.DENSITY_DEFAULT; //approxiamtely screendpi
        gridPixelLength =  maxWindowMetrics.getBounds().height();
        gridPixelWidth = maxWindowMetrics.getBounds().width();

        screenActualWidth =  (gridPixelWidth-cutoutRight-cutoutLeft) / density;
        screenActualHeight = (gridPixelLength-cutoutTop-cutoutBottom) / density;

        gridWidth=orientation==ORIENTATION_POTRAIT? screenActualWidth:screenActualHeight;
        gridHeight=orientation==ORIENTATION_POTRAIT? screenActualHeight:screenActualWidth;

        gridWidth= gridWidth/min_physical_unit_len_inches;
        gridHeight= gridHeight/min_physical_unit_len_inches;
        /*Log.e("GRID",String.format("%f %f %f %f",cutoutTop,cutoutRight,cutoutBottom,cutoutLeft));
        Log.e("GRID",String.format("%f %f",screenActualWidth,screenActualHeight));
        Log.e("GRID",String.format("%f %f",gridWidth,gridHeight));*/
        drawCalls = new ConcurrentLinkedQueue<>();
        clearCalls = new ConcurrentLinkedQueue<>();
        grid = new ControlComponent[(int)gridWidth][(int)gridHeight];
        //gridHeight = scr
    }
    public int[] GetScreenInfo(){
        return new int[]{(int)gridWidth,(int)gridHeight,(int)(gridPixelWidth/gridWidth)};
    }
    private final ConcurrentListBackHashMap<Integer,ControlComponent> ActivePointers = new ConcurrentListBackHashMap<>();
    private final ConcurrentListBackHashMap<Integer,Buildable> MasterComponentList = new ConcurrentListBackHashMap<>();
    private final ConcurrentLinkedQueue<ControlComponent> drawCalls;
    private final ConcurrentLinkedQueue<Rect> clearCalls;
    private PlayEmDataPipe dataPipe;
    private ControlComponent getControlHandler(float x, float y){

        int arrXidx = (int)(x/gridPixelWidth*gridWidth);
        int arrYidx = (int)(y/gridPixelLength*gridHeight);
        //Log.i("COLL",String.format("%f %f %d %d",x,y,arrXidx,arrYidx));
        //Log.w("CTLGRID",String.format("%d x %d y",arrXidx,arrYidx));
        return grid[arrXidx][arrYidx];
    }
    public Queue<ControlComponent> GetDrawCalls(){
        return drawCalls;
    }
    public Queue<Rect> GetClearCalls(){
        return clearCalls;
    }
    public void SetPipe(PlayEmDataPipe dataPipe){
        dataPipe = dataPipe;
    }

    private boolean pipeFramePush = false;

    private void handlePack(ControlHandler.ValuePack[] pack){
        if(dataPipe!=null){
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
    }

    public void newPointer(MotionEvent event,int id,int idx){
        /*if(ActivePointers.ContainsKey(id)){
            Log.e("CONTROL","UP or CANCEL event not registered, DOWN received with active pointer in cache");
        }*/
        float x = event.getX(idx);
        float y = event.getY(idx);
        //Log.i("NP",String.format("new Pointer %f %f",x,y));
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
        //Log.i("NP",String.format("move Pointer %f %f",x,y));
        ControlComponent cc = ActivePointers.Get(id);
        if(cc!=null){
            handlePack(cc.handler.onMove(x,y,id));
            drawCalls.add(cc);
        }
    }

    private void dropPointer(MotionEvent event,int id, int idx){
        float x = event.getX(idx);
        float y = event.getY(idx);
        if(ActivePointers.ContainsKey(id)){
            ControlComponent cc = ActivePointers.Get(id);
            if(cc!=null){
                handlePack(Objects.requireNonNull(cc.handler.onExit(x,y,id)));
                drawCalls.add(cc);
                ActivePointers.Remove(id);
            }
        }
    }
    public boolean onTouchEvent(MotionEvent event) {
        if(building){
            //Log.e("BUILDMODE","Touch event");
            return onTouchBuildMode(event);
        }
        return onTouchPlayMode(event);
    }
    private float buildDatumX;
    private float buildDatumY;
    private int pushcount=0;
    private boolean onTouchBuildMode(MotionEvent event){
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                buildDatumX = event.getX(0);
                buildDatumY = event.getY(0);
                //Log.e("BUILDMODE","Pointer Down");

                if(focusedBuilding == null) {
                    focusedComponent =  getControlHandler(buildDatumX, buildDatumY);
                    if(focusedComponent!=null){
                        focusedBuilding = MasterComponentList.Get(focusedComponent.pipeID);
                        if(focusedBuilding==null){
                            Log.e("SYS","Not found buildable");
                        }
                        parentActivity.ShowControlOptions();
                    }
                    //Log.e("BUILDMODE","Pointer Down checking null");
                    if(focusedBuilding == null){
                        if(pushcount%2==0){
                            parentActivity.ShowComponentAdd();
                            parentActivity.ShowMenuOptions();}
                        else{
                            parentActivity.HideComponentAdd();
                            parentActivity.HideMenuOptions();}
                        }
                        pushcount++;
                }
                else{
                    parentActivity.ShowControlOptions();
                    parentActivity.HideMenuOptions();
                    parentActivity.HideComponentAdd();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i("BUILDMODE","Multi Touch not used for Build");
                break;
            case MotionEvent.ACTION_MOVE:
                if(focusedBuilding!=null){
                    float currentX = event.getX(0);
                    float currentY = event.getY(0);
                    int moveXStep = (int)((currentX-buildDatumX)/ focusedComponent.pixelsPerStep);
                    int moveYStep = (int)((currentY-buildDatumY)/ focusedComponent.pixelsPerStep);

                    if(Math.abs(moveYStep)>0 || Math.abs(moveXStep)>0){
                        buildDatumY = currentY;
                        buildDatumX = currentX;
                        clearCalls.add(new Rect(focusedComponent.drawSpace));
                        int nDatumX = (int)Math.max(Math.min(focusedComponent.positionX+moveXStep,Math.floor(gridWidth-focusedComponent.widthSteps)),0);
                        int nDatumY = (int)Math.max(Math.min(focusedComponent.positionY+moveYStep,Math.floor(gridHeight-focusedComponent.heightSteps)),0);
                        //Log.i("BUILDMODE",String.format("BuildDatum %d %d %f %f %f %f",nDatumX,nDatumY,gridWidth,Math.floor(gridHeight-1),currentX,currentY));
                        RemoveComponent(focusedComponent);
                        focusedBuilding.MoveAndUpdateDrawSpace(nDatumX,nDatumY);
                        CheckOverlapAndRedraw(focusedComponent);
                        drawCalls.add(focusedComponent);
                    }
                }else{
                    Log.w("BUILDMODE","Focused object not valid!");
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:

                break;
            case MotionEvent.ACTION_UP:
                if(focusedBuilding!=null){
                    Log.i("BUILDMODE","Valid Placement");

                    //AcceptEdits();
                        //BurnIntoGrid(focusedComponent);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.w("CONTROL","CANCEL received, Unsupported input handling");
        }
        return true;
    }
    private boolean onTouchPlayMode(MotionEvent event){
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
                    /*else{
                        //Log.w("CONTROL", "MOVE received but Active pointer not registered");
                    }*/
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
    public boolean building = true;
    private Buildable focusedBuilding;
    private ControlComponent focusedComponent;
    public boolean AddBuildableComponenet(Buildable buildable,ControlComponent component){
        this.building = true;
        buildable.NewBuildState(true);
        focusedBuilding = buildable;
        AddControlComponent(component);
        MasterComponentList.AddorUpdate(component.pipeID,buildable);
        uniqueComponent++;
        focusedComponent = component;
        parentActivity.HideMenuOptions();
        parentActivity.ShowControlOptions();
        return true;
        //return
    }
    public boolean AcceptEdits(){
        if(focusedBuilding!=null ){
            if(CheckOverlap(focusedComponent)){
                BurnIntoGrid(focusedComponent);
                focusedBuilding = null;
                focusedComponent = null;
                return true;
            }else{
                parentActivity.ShowToastError("Cannot Overlap!", Toast.LENGTH_SHORT);
            }
        }
        building = true;
        return false;
    }
    public boolean AddControlComponent(ControlComponent component){
        component.MoveCentre((int)gridWidth,(int)gridHeight);
        if(component==null)
            return false;

        if(!CheckOverlap(component))
            return false;

        component.pipeID = uniqueComponent;

        drawCalls.add(component);
        Log.i("GRID",String.format("Added Component %d %d",component.positionX,component.positionY));
        return true;
    }

    private void BurnIntoGrid(ControlComponent component){
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;j++){
                grid[i][j] = component;
                //Log.i("GRID",String.format("%d %d",i,j));
            }
        }
    }
    private int uniqueComponent=0;
    public boolean RemoveComponentInFocus(){
        if(focusedBuilding!=null){
            Log.i("REMOVE","Removing");
            clearCalls.add(new Rect(focusedComponent.drawSpace));
            RemoveComponent(focusedComponent);
            MasterComponentList.Remove(focusedComponent.pipeID);
            focusedComponent = null;
            focusedBuilding =null;
            //drawCalls.addAll(MasterComponentList.GetValues())
        }
        return true;
    }
    public boolean RemoveComponent(ControlComponent component){
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;j++){
                if(grid[i][j]==component)
                    grid[i][j] = null;
            }
        }
        //MasterComponentList.Remove(component.pipeID);
        return true;
    }

    public boolean CheckOverlap(ControlComponent component){
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;j++){
                if(grid[i][j]!=null && grid[i][j]!=component)
                    return false;
            }
        }
        return true;
    }
    public boolean CheckOverlapAndRedraw(ControlComponent component){
        HashMap<Integer,ControlComponent> overlaps = new HashMap<>();
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;j++){
                if(grid[i][j]!=null)
                    if(component!=grid[i][j])
                        overlaps.put(grid[i][j].pipeID,grid[i][j]);
            }
        }
        drawCalls.addAll(overlaps.values());
        return true;
    }
    public List<Buildable> GetComponentList(){
        return this.MasterComponentList.GetValues();
    }
}
