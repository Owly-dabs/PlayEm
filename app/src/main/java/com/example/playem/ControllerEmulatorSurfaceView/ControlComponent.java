package com.example.playem.ControllerEmulatorSurfaceView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;

public abstract class ControlComponent {
    public ControlComponent(int idxX, int idxY,int pixelsPerStep){
        //this.handler = handler;
        positionX = idxX;
        positionY = idxY;
        this.pixelsPerStep = pixelsPerStep;
        updateDrawSpace();
    }
    protected ControlHandler handler;
    protected Rect drawSpace;
    public int positionX,positionY;
    protected float screenCentrePosX;
    protected float screenCentrePosY;
    public int widthSteps,heightSteps;
    public final int pixelsPerStep;
    public boolean dragging,locked;
    public abstract void Draw(Canvas canvas, Paint bgPaintObject);
    public void MoveCentre(){
        screenCentrePosY = (float)(positionX*pixelsPerStep)+(float)(widthSteps*pixelsPerStep)/2.0f;
        screenCentrePosX = (float)(positionX*pixelsPerStep)+(float)(widthSteps*pixelsPerStep)/2.0f;
    }
    private void updateDrawSpace(){
        drawSpace.left = positionX*pixelsPerStep;
        drawSpace.right = drawSpace.left + widthSteps*pixelsPerStep;
        drawSpace.top = positionY*pixelsPerStep;
        drawSpace.bottom = drawSpace.top + widthSteps*pixelsPerStep;
    }

}
