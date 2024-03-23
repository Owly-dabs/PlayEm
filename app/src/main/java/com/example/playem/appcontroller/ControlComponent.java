package com.example.playem.appcontroller;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public abstract class ControlComponent {
    public ControlComponent(){}
    public ControlComponent(int idxX, int idxY,int pixelsPerStep,int pipeId){
        this.pixelsPerStep = pixelsPerStep;
        pipeID = pipeId;
        SetDatums(idxX,idxY);
    }
    protected void SetDatums(int idxX, int idxY){
        positionX = idxX;
        positionY = idxY;
        Log.i("DATUM",String.format("%d %d",positionX,positionY));
        screenCentrePosY = (float)(idxY*pixelsPerStep+(heightSteps*pixelsPerStep/2));
        screenCentrePosX = (float)(idxX*pixelsPerStep+(widthSteps*pixelsPerStep/2));
        updateDrawSpace();
    }
    public int pipeID;
    protected ControlHandler handler;
    protected Rect drawSpace;
    public int positionX,positionY;
    protected float screenCentrePosX;
    protected float screenCentrePosY;
    public int widthSteps,heightSteps;
    public int pixelsPerStep;
    public int clearCount=0;
    public boolean colliding = false;
    public boolean dragging,locked;
    public abstract void Draw(Canvas canvas, Paint bgPaintObject);
    public void MoveCentre(int gridW, int gridH){
        positionX = (gridW-widthSteps)/2;
        positionY = (gridH-heightSteps)/2;
        screenCentrePosX = (float)((positionX*pixelsPerStep)+(float)(widthSteps*pixelsPerStep)/2.0f);
        screenCentrePosY = (float)((positionY*pixelsPerStep)+(float)(heightSteps*pixelsPerStep)/2.0f);
        updateDrawSpace();
        Log.i("THUMB",String.format("%d %d %f %f c Pixel point",positionX,positionY,screenCentrePosX,screenCentrePosY));
    }

    private void updateDrawSpace(){
        if(drawSpace==null)
            drawSpace = new Rect();
        drawSpace.left = positionX*pixelsPerStep;
        drawSpace.right = drawSpace.left + widthSteps*pixelsPerStep;
        drawSpace.top = positionY*pixelsPerStep;
        drawSpace.bottom = drawSpace.top + heightSteps*pixelsPerStep;
    }

}
