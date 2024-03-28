package com.example.playem.appcontroller;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.WindowMetrics;
import android.widget.Toast;

import com.example.playem.appcontroller.interfaces.Buildable;
import com.example.playem.ControllerActivity;
import com.example.playem.generics.ConcurrentListBackHashMap;
import com.example.playem.pipes.HidBleDataPipe;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ControlGrid {
    public static final int ORIENTATION_POTRAIT=0;
    public static final int ORIENTATION_LANDSCAPE=1;
    private float gridHeight,gridWidth;
    private final int gridPixelLength,gridPixelWidth;
    private final float screenActualHeight,screenActualWidth,density;
    private ControlComponent[][] grid;
    private final ControllerActivity parentActivity;
    private float min_unit_len;
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
        parentActivity = (ControllerActivity) activity; //Disgusting downcast TODO
        density = activity.getResources().getDisplayMetrics().density * DisplayMetrics.DENSITY_DEFAULT; //approxiamtely screendpi
        gridPixelLength =  maxWindowMetrics.getBounds().height();
        gridPixelWidth = maxWindowMetrics.getBounds().width();

        screenActualWidth = (gridPixelWidth - cutoutRight - cutoutLeft) / density;
        screenActualHeight = (gridPixelLength - cutoutTop - cutoutBottom) / density;

        //gridWidth=orientation==ORIENTATION_POTRAIT? screenActualWidth : screenActualHeight;
        //gridHeight=orientation==ORIENTATION_POTRAIT? screenActualHeight : screenActualWidth;
        min_unit_len = min_physical_unit_len_inches;
        gridWidth= screenActualWidth/min_physical_unit_len_inches;
        gridHeight= screenActualHeight/min_physical_unit_len_inches;

        drawCalls = new ConcurrentLinkedQueue<>();
        clearCalls = new ConcurrentLinkedQueue<>();
        grid = new ControlComponent[(int)gridWidth][(int)gridHeight];
        dataPipe = null;
        //gridHeight = scr
    }
    public int[] GetGridInfo(){
        return new int[]{(int)gridWidth,(int)gridHeight,(int)(gridPixelWidth/gridWidth)};
    }

    private final ConcurrentListBackHashMap<Integer,ControlComponent> ActivePointers = new ConcurrentListBackHashMap<>();
    private final ConcurrentListBackHashMap<Integer,Buildable> MasterComponentList = new ConcurrentListBackHashMap<>();
    private final ConcurrentLinkedQueue<ControlComponent> drawCalls;
    private final ConcurrentLinkedQueue<Rect> clearCalls;
    private HidBleDataPipe dataPipe;
    private ControlComponent getControlHandler(float x, float y){

        int arrXidx = (int)Math.min((x/gridPixelWidth*gridWidth),gridWidth-1);
        int arrYidx = (int)Math.min((y/gridPixelLength*gridHeight),gridHeight-1);
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
    public void SetPipe(HidBleDataPipe dataPipe){
        this.dataPipe = dataPipe;
    }
    private boolean pipeFramePush = false;
    private void handlePack(ControlHandler.ValuePack[] pack){
        //Not Used
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
            ch.clearCount = 0;
            clearFromBuffer.Remove(ch.pipeID);
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
    public ConcurrentListBackHashMap<Integer,ControlComponent> clearFromBuffer = new ConcurrentListBackHashMap<>();
    private void dropPointer(MotionEvent event,int id, int idx){
        float x = event.getX(idx);
        float y = event.getY(idx);
        if(ActivePointers.ContainsKey(id)){
            ControlComponent cc = ActivePointers.Get(id);
            if(cc!=null){
                handlePack(Objects.requireNonNull(cc.handler.onExit(x,y,id)));
                drawCalls.add(cc);
                ActivePointers.Remove(id);
                cc.clearCount = 5;
                clearFromBuffer.AddorUpdate(cc.pipeID,cc);
            }
        }
    }
    public void PadExtraOnExit(ControlComponent component){
        handlePack(component.handler.onExit(component.screenCentrePosX,component.screenCentrePosY,-1));
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
                        CheckOverlap(focusedComponent);
                    }
                }else{
                    Log.w("BUILDMODE","Focused object not valid!");
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                    Log.i("BUILDMODE","Multi Touch not consumed for Build");
                break;
            case MotionEvent.ACTION_UP:
                if(focusedBuilding!=null){
                    if(focusedComponent.colliding){
                        parentActivity.ShowToastError("Overlap!",Toast.LENGTH_SHORT);
                    }else{
                        parentActivity.ShowToastError("Component Placed!",Toast.LENGTH_SHORT);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.w("BUILDMODE","CANCEL received, Unsupported input handling");
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
    public void AddBuildableComponent(Buildable buildable, ControlComponent component,boolean firstBuild){
        this.building = true;
        buildable.NewBuildState(true);
        focusedBuilding = buildable;
        AddControlComponent(component,firstBuild);
        MasterComponentList.AddorUpdate(component.pipeID,buildable);
        uniqueComponent++;
        focusedComponent = component;
        parentActivity.HideMenuOptions();
        parentActivity.ShowControlOptions();

        //return
    }
    public void AcceptEdits(){
        if(focusedBuilding!=null ){
            if(CheckOverlap(focusedComponent)){
                BurnIntoGrid(focusedComponent);
                focusedBuilding = null;
                focusedComponent = null;
                return;
            }else{
                parentActivity.ShowToastError("Cannot Overlap!", Toast.LENGTH_SHORT);
            }
        }
        building = true;
    }
    public void AddControlComponent(ControlComponent component, boolean firstBuild){
        if(component==null)
            return;
        if(firstBuild)
            component.MoveCentre((int)gridWidth,(int)gridHeight);
        component.pipeID = uniqueComponent;
        if(!CheckOverlap(component))
            return;
        //drawCalls.add(component);
        Log.i("GRID",String.format("Added Component %d %d",component.positionX,component.positionY));
    }

    private void BurnIntoGrid(ControlComponent component){
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;j++){
                grid[i][j] = component;
                //Log.i("GRID",String.format("%d %d",i,j));
            }
        }
    }
    private void ClearGrid(){
        Log.w("GRIDCALC",String.format("%f %f",gridWidth,gridHeight));
        for(int i = 0;i<(int)gridWidth;i++){
            for(int j = 0;j<(int)gridHeight;j++){
                grid[i][j] = null;
                //Log.w("GRID","Grid commanded to clear");
            }
        }
    }
    private void ClearBuffers(){
        MasterComponentList.Clear();
        ActivePointers.Clear();
        drawCalls.clear();
        clearCalls.clear();
    }
    public void FlushGridAndBuffers(){
        ClearBuffers();
        ClearGrid();
        uniqueComponent=0;
        focusedBuilding = null;
        focusedComponent = null;
        dataPipe = null;
    }
    public void ResetControlGrid(float min_unit_len){
        Log.w("GRIDCALC",String.format("%f %f %f",gridWidth,gridHeight,min_unit_len));
        FlushGridAndBuffers();
        gridWidth = (screenActualWidth/min_unit_len);
        gridHeight = screenActualHeight/min_unit_len;
        this.min_unit_len = min_unit_len;
        grid = new ControlComponent[(int)gridWidth][(int)gridHeight];
    }
    private int uniqueComponent=0;
    public void RemoveComponentInFocus(){
        if(focusedBuilding!=null){
            Log.i("REMOVE","Removing");
            clearCalls.add(new Rect(focusedComponent.drawSpace));
            RemoveComponent(focusedComponent);
            MasterComponentList.Remove(focusedComponent.pipeID);
            focusedComponent = null;
            focusedBuilding =null;
            //drawCalls.addAll(MasterComponentList.GetValues())
        }
    }
    public void ResizeComponent(int step){
        if(focusedBuilding!=null){
            Log.i("RESIZE","Resizing");
            Buildable b = focusedBuilding;
            ControlComponent c = focusedComponent;
            if(c.positionX+c.widthSteps+step<=gridWidth && c.positionY+c.heightSteps+step<gridHeight) {
                clearCalls.add(new Rect(c.drawSpace));
                RemoveComponentInFocus();
                b.Resize(step);
                b.MoveAndUpdateDrawSpace(c.positionX, c.positionY);
                AddBuildableComponent(b,c,false);
            }
            //MasterComponentList.Remove(focusedComponent.pipeID);
            //focusedComponent = null;
            //focusedBuilding =null;
            //drawCalls.addAll(MasterComponentList.GetValues())
        }
    }
    public void RemoveComponent(ControlComponent component){
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;j++){
                if(grid[i][j]==component)
                    grid[i][j] = null;
            }
        }
    }

    public boolean CheckOverlap(ControlComponent component){
        for(int i = component.positionX;i<component.positionX+component.widthSteps;i++){
            for(int j = component.positionY;j<component.positionY+component.heightSteps;j++){
                if(grid[i][j]!=null && grid[i][j]!=component){
                    component.colliding = true;
                    return false;
                }
            }
        }
        component.colliding = false;
        return true;
    }
    public List<Buildable> GetComponentList(){
        return this.MasterComponentList.GetValues();
    }

    public float[] GetScreenInfo() {
        return new float[]{screenActualWidth,screenActualHeight,min_unit_len};
    }
}
