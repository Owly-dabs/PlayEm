package com.example.playem.appcontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.playem.AppGattService;
import com.example.playem.appcontroller.VirtualControls.MandatoryButton;
import com.example.playem.appcontroller.VirtualControls.SimpleButton;
import com.example.playem.appcontroller.VirtualControls.ThumbStick;
import com.example.playem.appcontroller.VirtualControls.VirtualControlTemplates;
import com.example.playem.appcontroller.interfaces.Buildable;
import com.example.playem.appcontroller.interfaces.BuildableViewCallbacks;
import com.example.playem.appsettings.ControlsData;
import com.example.playem.appsettings.GridData;
import com.example.playem.appsettings.ProfileData;
import com.example.playem.appsettings.ProfileSerializable;
import com.example.playem.pipes.PipeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("ViewConstructor")
public class ControllerReactiveView extends SurfaceView {
    private final ControlGrid controlGrid;
    private final ExecutorService drawThread;
    private final Rect screenBounds;
    private final BuildableViewCallbacks buildableViewCallbacks;
    public ControllerReactiveView(Context context, ControlGrid controlGrid, Rect screenBounds, BuildableViewCallbacks buildableViewCallbacks) {
        super(context);
        this.buildableViewCallbacks = buildableViewCallbacks;
        getHolder().addCallback(ControllerReactiveView.this.surfaceViewCallback);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setZOrderOnTop(false);
        this.controlGrid = controlGrid;
        drawThread = Executors.newSingleThreadExecutor();
        defPaint.setColor(Color.BLACK);
        defPaint.setAlpha(255);
        colliderStroke.setColor(Color.CYAN);
        colliderStroke.setAlpha(70);
        colliderStroke.setStrokeWidth(5f);
        colliderStroke.setStyle(Paint.Style.STROKE);
        collisionBg.setColor(Color.RED);
        this.screenBounds = screenBounds;
    }

    private final Paint defPaint = new Paint();
    private final Paint colliderStroke = new Paint();
    private final Paint collisionBg = new Paint();
    private SurfaceHolder surfaceHolder;
    //private long lasttime=0;
    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controlGrid.onTouchEvent(event);
        if(controlGrid.building){
            drawThread.execute(this::drawForBuild);
        }else{
            drawThread.execute(this::drawFromComponent);
        }
        return true;
    }
    private void drawFromComponent(){
        //long time = System.currentTimeMillis();
        Canvas screenC = surfaceHolder.lockHardwareCanvas();
        int maxdraw = 50;//TODO Use this as metrics for draw thread
        Queue<ControlComponent> toDraw = controlGrid.GetDrawCalls();
        List<ControlComponent> drawReset = controlGrid.clearFromBuffer.GetValues();
        for(ControlComponent reqClear : drawReset){
            reqClear.clearCount--;
            reqClear.Draw(screenC,defPaint);
            controlGrid.PadExtraOnExit(reqClear);
            if(reqClear.clearCount<=0)
                controlGrid.clearFromBuffer.Remove(reqClear.pipeID);
        }

        while(maxdraw>=0){
            maxdraw--;
            ControlComponent ch = toDraw.poll();
            if(ch!=null)
                ch.Draw(screenC,defPaint);
            else
                break;
        }
        surfaceHolder.unlockCanvasAndPost(screenC);
    }
    private void drawForBuild(){
        //long time = System.currentTimeMillis();
        Queue<Rect> toClear = controlGrid.GetClearCalls();
        List<Buildable> builables = controlGrid.GetComponentList();
        if(!toClear.isEmpty()||!builables.isEmpty()) {
            Canvas screenC = surfaceHolder.lockHardwareCanvas();
            screenC.drawRect(screenBounds,defPaint);
            for(Buildable bb: builables){
                bb.GetComponent().Draw(screenC,defPaint);
                if(!bb.GetComponent().colliding){
                    bb.GetComponent().Draw(screenC,defPaint);
                }else{
                    //Log.i("COLL","Colliding");
                    bb.GetComponent().Draw(screenC,collisionBg);
                }
                //Log.e("COLLIDER","ColliderStroke");
                bb.DrawColliderBox(screenC,colliderStroke,2);
            }
            //screenC.drawText(String.format("%d ms",time-lasttime),150,150,defPaint);
            //lasttime = time;
            surfaceHolder.unlockCanvasAndPost(screenC);
        }else{
            Log.e("DRAW","Buildables are empty");
        }
    }
    private void drawCleared(){
        Canvas screenC = surfaceHolder.lockHardwareCanvas();
        screenC.drawRect(screenBounds,defPaint);
        surfaceHolder.unlockCanvasAndPost(screenC);
    }
    public void AddComponent(VirtualControlTemplates type){
        int[] screenInfo = controlGrid.GetGridInfo();
        AddComponent(type,0,0,-1,-1,screenInfo[2],0,true);
    }
    public void AddComponent(VirtualControlTemplates type,int idxX,int idxY,int width,int height,int pixelsPerStep,int pipeId,boolean firstBuild){
        switch(type){
            case THUMSTICK:
                ThumbStick ts = new ThumbStick(idxX,idxY,width,height,pixelsPerStep,pipeId);
                controlGrid.AddBuildableComponent(ts,ts,firstBuild);
                break;
            case SBUTTON:
                SimpleButton sb = new SimpleButton(idxX,idxY,width,height,pixelsPerStep,pipeId);
                controlGrid.AddBuildableComponent(sb,sb,firstBuild);
                break;
            case MANDATORY:
                MandatoryButton mb = new MandatoryButton(idxX,idxY,width,height,pixelsPerStep,buildableViewCallbacks);
                controlGrid.AddBuildableComponent(mb,mb,firstBuild);
                break;
        }
        drawThread.execute(this::drawForBuild);
    }
    public void RemoveComponent(){
        controlGrid.RemoveComponentInFocus();
        drawThread.execute(this::drawForBuild);
    }

    public void ResizeComponent(int size){
        controlGrid.ResizeComponent(size);
        drawThread.execute(this::drawForBuild);
    }

    public void FinishEdits(){
        controlGrid.AcceptEdits();
    }
    public void FinalizeAndBuildAll(AppGattService service){
        controlGrid.SetPipe(new PipeBuilder().BuildPipe(controlGrid.GetComponentList(),service));
    }

    public void SwitchToPlay(){
        controlGrid.building = false;
    }

    private final SurfaceHolder.Callback surfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            surfaceHolder = holder;
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    buildableViewCallbacks.HideAllOptions();
                }
            },5000);

            AddComponent(VirtualControlTemplates.MANDATORY);
        }
        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            surfaceHolder = holder;
        }
        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            holder.removeCallback(ControllerReactiveView.this.surfaceViewCallback);
            surfaceHolder=null;
        }
    };

    public void SaveProfile(String name){
        if(controlGrid.building){
            List<ControlsData> cd = new ArrayList<>();
            for(Buildable b :controlGrid.GetComponentList()){
                cd.add(new ControlsData(b));
            }
            float[] si = controlGrid.GetScreenInfo();
            GridData gd = new GridData(si[0],si[1],si[2]);
            if(ProfileSerializable.SaveProfile(this.getContext(),cd,gd,name)){
                for(String fn : ProfileSerializable.GetProfiles(this.getContext())){
                    Log.i("FILEREAD",fn);
                }
            }
        }
    }
    public void LoadProfile(String name){
        List<String> profiles = ProfileSerializable.GetProfiles(this.getContext());
        if(profiles.contains(name)){
            ProfileData pd= (ProfileData)(ProfileSerializable.GetObjectFromFile(this.getContext(), ProfileData.class,name));
            if(pd!=null) {
                Log.w("LOAD", pd.toString());
                if(!AssembleFromProfile(this,pd)) {
                    Log.e("LOAD", "Load profile failed!... Resetting to empty Grid");
                    this.controlGrid.ResetControlGrid(SAFE_MINUNIT);
                }
            }
        }
    }
    static boolean AssembleFromProfile(ControllerReactiveView thisView, ProfileData pd){
        thisView.buildableViewCallbacks.ShowMenuOptions();
        thisView.drawCleared();
        float[] current_screenInfo =thisView.controlGrid.GetScreenInfo();
        float min_unit_len = current_screenInfo[0]/pd.gridData.screenWidth*pd.gridData.min_unit_len;
        thisView.controlGrid.ResetControlGrid(min_unit_len);
        thisView.controlGrid.building = true;
        int[] grid_info = thisView.controlGrid.GetGridInfo();
        for(ControlsData cd: pd.controlsList){
            thisView.AddComponent(VirtualControlTemplates.valueOf(cd.virtualControlType),cd.idxX,cd.idxY,cd.widthSteps, cd.heightSteps, grid_info[2],cd.pipeId,false);
            thisView.FinishEdits();
        }
        thisView.drawForBuild();
        thisView.buildableViewCallbacks.HideAllOptions();
        thisView.buildableViewCallbacks.ShowMenuOptions();
        return true;
    }

    static float SAFE_MINUNIT = 0.25f;
}
