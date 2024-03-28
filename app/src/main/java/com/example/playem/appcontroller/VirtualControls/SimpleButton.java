package com.example.playem.appcontroller.VirtualControls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.example.playem.appcontroller.ControlComponent;
import com.example.playem.appcontroller.ControlHandler;
import com.example.playem.appcontroller.interfaces.Buildable;
import com.example.playem.hid.interfaces.ChunkType;
import com.example.playem.pipes.HidBleDataPipe;

public class SimpleButton extends ControlComponent implements ControlHandler, Buildable {
    public SimpleButton(){
        super();
    }
    public SimpleButton(int idxX, int idxY, int width,int height,int pixelsPerStep, int pipeid) {
        super(idxX, idxY,width<1?2:width,height<1?2:height,pixelsPerStep,pipeid);
        outerRingPobj.setColor(Color.BLUE);
        innerPobj.setColor(Color.RED);
        innerPressedPobj.setColor(Color.RED);
        innerPressedPobj.setAlpha(70);
        lastValuePack[0] = new ValuePack();
        lastValuePack[0].type=ChunkType.BUTTONS;
        lastValuePack[0].relPixelX=0;
        this.handler = this;
    }
    protected final ValuePack[] lastValuePack= new ValuePack[1];
    private boolean pressed = false;
    private float baseRadius = 1.0f;
    private float innerCircle = 0.5f;
    private float bandRatio = 0.3f;
    protected Paint outerRingPobj = new Paint();
    protected Paint innerPobj = new Paint();
    protected Paint innerPressedPobj = new Paint();
    private final int minStep = 1;
    private int reportID=-1;
    @Override
    public void Draw(Canvas canvas, Paint bgPaintObject) {
        float drawLength = this.widthSteps*this.pixelsPerStep;
        canvas.drawRect(drawSpace,bgPaintObject);
        canvas.drawCircle(screenCentrePosX,screenCentrePosY,drawLength*baseRadius/2,outerRingPobj);
        canvas.drawCircle(screenCentrePosX,screenCentrePosY,drawLength*(baseRadius-bandRatio)/2,bgPaintObject);

        if(pressed){
            canvas.drawCircle(screenCentrePosX,screenCentrePosY,drawLength*innerCircle/2,innerPressedPobj);
        }else{
            canvas.drawCircle(screenCentrePosX,screenCentrePosY,drawLength*innerCircle/2,innerPobj);
        }
    }

    @Override
    public ValuePack[] onEnter(float x, float y, int pointerId) {
        lastValuePack[0].x = 1;
        dataPipe.UpdateButtonNumber(reportID,true);
        return lastValuePack;
    }

    @Override
    public ValuePack[] onExit(float x, float y, int pointerId) {
        lastValuePack[0].x=0;
        dataPipe.UpdateButtonNumber(reportID,false);
        return lastValuePack;
    }

    @Override
    public ValuePack[] onMove(float x, float y, int pointerId) {
        lastValuePack[0].x = 1;
        dataPipe.UpdateButtonNumber(reportID,true);
        return lastValuePack;
    }

    @Override
    public ValuePack[] onMotionEvent(MotionEvent motionEvent) {
        return lastValuePack;
    }

    @Override
    public void Resize(int step) {
        int squarestep = Math.max(this.widthSteps+step,minStep);
        this.widthSteps= squarestep;
        this.heightSteps = squarestep;
    }

    @Override
    public void MoveAndUpdateDrawSpace(int datumX, int datumY) {
        SetDatums(datumX,datumY);
        screenCentrePosX = (float)((positionX*pixelsPerStep)+(float)(widthSteps*pixelsPerStep)/2.0f);
        screenCentrePosY = (float)((positionY*pixelsPerStep)+(float)(heightSteps*pixelsPerStep)/2.0f);
    }

    @Override
    public void NewBuildState(boolean newState) {
    }

    @Override
    public void DrawColliderBox(Canvas screen, Paint colliderColor, int stroke_width) {
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
        return VirtualControlTemplates.SBUTTON;
    }

    @Override
    public void onInputReportPipeID(int reportID) {
        this.reportID = reportID;
    }

    @Override
    public void onSetDataPipe(HidBleDataPipe dataPipe) {
        this.dataPipe = dataPipe;
    }
    private HidBleDataPipe dataPipe;
}
