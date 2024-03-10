package com.example.playem.ControllerEmulatorSurfaceView.VirtualControls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.example.playem.ControllerEmulatorSurfaceView.ControlComponent;
import com.example.playem.ControllerEmulatorSurfaceView.ControlHandler;

public class ThumbStick extends ControlComponent implements ControlHandler {

    public ThumbStick(int idxX, int idxY, int pixelsPerStep) {
        super(idxX, idxY,pixelsPerStep);
        this.widthSteps=5;
        this.heightSteps=5;
        this.basePobj = new Paint();
        this.pointyPobj = new Paint();
        basePobj.setColor(Color.WHITE);
        pointyPobj.setColor(Color.RED);
        this.handler = this;
    }

    private int minstep = 2;
    private float pointyRatio = 0.5f;
    private float baseRatio = 0.7f;
    private float baseband = 0.2f;
    private boolean debugCollision = false;
    private Paint basePobj;
    private Paint pointyPobj;
    @Override
    public void Draw(Canvas canvas, Paint bgPaintObject) {
        float drawLength = this.widthSteps*this.pixelsPerStep;
        float x =  this.lastValuePack[0].relPixelX;
        float y =  this.lastValuePack[0].relPixelY;
        float pointyX =  this.screenCentrePosX+x;
        float pointyY = this.screenCentrePosY+y;
        canvas.drawRect(this.drawSpace,bgPaintObject);
        canvas.drawCircle(this.screenCentrePosX,this.screenCentrePosY,drawLength*baseRatio/2,basePobj);
        canvas.drawCircle(this.screenCentrePosX,this.screenCentrePosY,drawLength*(baseRatio-baseband)/2,bgPaintObject);
        canvas.drawCircle(pointyX,pointyY,drawLength*pointyRatio/2,pointyPobj);
    }

    private ValuePack[] lastValuePack;

    @Override
    public ValuePack[] onEnter(float x, float y, int pointerId) {
        if(lastValuePack==null)
            lastValuePack = new ValuePack[1];
        float maxlen = widthSteps*pixelsPerStep;
        lastValuePack[0].x = (int)x;
        lastValuePack[0].y = (int)y;

        float relX = x<drawSpace.left?0:x;
        relX = x>drawSpace.right?maxlen:x;

        float relY = y<drawSpace.top?0:y;
        relY = y>drawSpace.bottom?maxlen:y;

        lastValuePack[0].relPixelX = (int)((relX/maxlen)*65535);
        lastValuePack[0].relPixelY = (int)((relY/maxlen)*65535);

        return new ValuePack[0];
    }

    @Override
    public ValuePack[] onExit(float x, float y, int pointerId) {
        return new ValuePack[0];
    }

    @Override
    public ValuePack[] onMove(float x, float y, int pointerId) {
        return new ValuePack[0];
    }

    @Override
    public ValuePack[] onMotionEvent(MotionEvent motionEvent) {
        return new ValuePack[0];
    }


}
