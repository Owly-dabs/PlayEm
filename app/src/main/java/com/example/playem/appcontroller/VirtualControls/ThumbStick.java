package com.example.playem.appcontroller.VirtualControls;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.example.playem.appcontroller.ControlComponent;
import com.example.playem.appcontroller.ControlHandler;
import com.example.playem.appcontroller.interfaces.Buildable;
import com.example.playem.hid.interfaces.ChunkType;
import com.example.playem.pipes.HidBleDataPipe;

public class ThumbStick extends ControlComponent implements ControlHandler , Buildable {
    public ThumbStick(){
        super();
    }
    public ThumbStick(int idxX, int idxY, int width,int height,int pixelsPerStep, int pipeid) {
        super(idxX, idxY,width<1?5:width,height<1?5:height,pixelsPerStep,pipeid);
        this.basePobj = new Paint();
        this.pointyPobj = new Paint();
        basePobj.setColor(Color.WHITE);
        pointyPobj.setColor(Color.RED);
        basePobj.setAlpha(122);
        lastValuePack = new ValuePack[1];
        lastValuePack[0] = new ValuePack();
        lastValuePack[0].type = ChunkType.AXES2;
        lastValuePack[0].x = idxX;
        lastValuePack[0].y = idxY;
        lastValuePack[0].id = pipeid;
        this.handler = this;
        if(idxX>-1&&idxY>-1) //TODO This is a bug fix for serialization
            this.MoveAndUpdateDrawSpace(idxX,idxY);
    }

    private final int minstep = 2;
    private float pointyRatio = 0.5f;
    private float baseRatio = 0.7f;
    private float baseband = 0.2f;
    private boolean building = false;
    private float pointyX,pointyY=-1f;
    private Paint basePobj;
    private Paint pointyPobj;
    @SuppressLint("DefaultLocale")
    @Override
    public synchronized void Draw(Canvas canvas, Paint bgPaintObject) {
        float drawLength = this.widthSteps*this.pixelsPerStep;
        if(pointyX <0 || pointyY <0){
            pointyX =  this.screenCentrePosX;
            pointyY = this.screenCentrePosY;
        }
        //Log.i("THUMB",String.format("%f %f %f c Pixel point",pointyX,pointyY,drawLength));
        canvas.drawRect(this.drawSpace,bgPaintObject);
        canvas.drawCircle(this.screenCentrePosX,this.screenCentrePosY,drawLength*baseRatio/2,basePobj);
        canvas.drawCircle(this.screenCentrePosX,this.screenCentrePosY,drawLength*(baseRatio-baseband)/2,bgPaintObject);
        float radius =drawLength*pointyRatio/2;
        float underdrawX = Math.min(Math.max(pointyX,drawSpace.left+radius),drawSpace.right-radius);
        float underdrawY = Math.min(Math.max(pointyY,drawSpace.top+radius),drawSpace.bottom-radius);
        canvas.drawCircle(underdrawX,underdrawY,radius,pointyPobj);
        if(building) {
            Paint p = new Paint();
            p.setTextSize(30);
            p.setColor(Color.WHITE);
            canvas.drawRect(drawSpace.left, drawSpace.top, drawSpace.right, drawSpace.top + 50, bgPaintObject);
            String details = String.format("%d %d", lastValuePack[0].x, lastValuePack[0].y);
            canvas.drawText(details, drawSpace.left, drawSpace.top + 50, p);
        }
    }

    private ValuePack[] lastValuePack;
    @Override
    public ValuePack[] onEnter(float x, float y, int pointerId) {
        if(lastValuePack==null)
            lastValuePack = new ValuePack[1];
        return onMove(x,y,pointerId);
    }

    @Override
    public ValuePack[] onExit(float x, float y, int pointerId) {
        return onMove(screenCentrePosX,screenCentrePosY,pointerId);
    }

    @Override
    public ValuePack[] onMove(float x, float y, int pointerId) {
        //Log.i("THUMB",String.format("onMove Draw %f %f",x,y));
        lastValuePack[0].type = ChunkType.AXES2;
        float maxlen = widthSteps*pixelsPerStep;
        lastValuePack[0].relPixelX = (int)x;
        lastValuePack[0].relPixelY = (int)y;

        float relX = Math.min(Math.max(x,drawSpace.left),drawSpace.right);
        float relY = Math.min(Math.max(y,drawSpace.top),drawSpace.bottom);

        pointyY = relY;
        pointyX = relX;
        //Log.i("THUMB",String.format("onMove Draw Point %f %f",pointyX,pointyY));
        lastValuePack[0].x = Math.min((int)(((relX-drawSpace.left)/maxlen)*65535),65535);
        lastValuePack[0].y = Math.min((int)(((relY-drawSpace.top)/maxlen)*65535),65535);
        if(dataPipe!=null){
            dataPipe.UpdateAxis(reportID, lastValuePack[0].x);
            dataPipe.UpdateAxis(reportID+1, lastValuePack[0].y);
        }
        return lastValuePack;
    }
    @Override //TODO remove and make as default function (optional override)
    public ValuePack[] onMotionEvent(MotionEvent motionEvent) {
        return new ValuePack[0];
    }

    @Override
    public void Resize(int step) {
        int squarestep = Math.max(this.widthSteps+step,minstep);
        this.widthSteps= squarestep;
        this.heightSteps = squarestep;
    }

    @Override
    public void MoveAndUpdateDrawSpace(int datumX, int datumY) {
        SetDatums(datumX,datumY);
        onMove(screenCentrePosX,screenCentrePosY,0);
        pointyX = screenCentrePosX;
        pointyY = screenCentrePosY;
    }

    @Override
    public void NewBuildState(boolean newState) {
        this.building = newState;
        String state = newState?"True":"False";
        Log.w("BUILDMODE",String.format("Entered Build Mode %s",state));
    }

    @Override
    public void DrawColliderBox(Canvas screen, Paint colliderColor,int stroke_width) {
        stroke_width*=7;
        Rect r = new Rect(drawSpace.left+stroke_width,drawSpace.top+stroke_width,drawSpace.right-stroke_width,drawSpace.bottom-stroke_width);
        screen.drawRect(r,colliderColor);
    }

    @Override
    public ControlComponent GetComponent() {
        return this;
    }

    @Override
    public ChunkType GetChunkType() {
        return lastValuePack[0].type;
    }

    @Override
    public VirtualControlTemplates GetVirtualControlType() {
        return VirtualControlTemplates.THUMSTICK;
    }

    @Override
    public void onInputReportPipeID(int reportID) {
        this.reportID = reportID;
    }
    private int reportID=-1;
    @Override
    public void onSetDataPipe(HidBleDataPipe dataPipe) {
        this.dataPipe = dataPipe;
    }
    private HidBleDataPipe dataPipe;
}
