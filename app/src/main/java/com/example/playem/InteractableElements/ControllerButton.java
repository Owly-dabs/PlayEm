package com.example.playem.InteractableElements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ControllerButton extends ControllerElement {

    private boolean IsPressed = false;
    private final double ButtonRadius;
    private final Paint ButtonPaint;
    private final Paint ButtonPaintPressed;
    private int pointerID =-1;

    public ControllerButton(int elementID, double CentreX, double CentreY, double ButtonRadius) {
        super(elementID, CentreX, CentreY, false);
        super.radius = this.ButtonRadius = ButtonRadius;

        // Colour of button (temporary solution for now)
        ButtonPaint = new Paint();
        ButtonPaint.setColor(Color.GRAY);
        ButtonPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        ButtonPaintPressed = new Paint();
        ButtonPaintPressed.setColor(Color.DKGRAY);
        ButtonPaintPressed.setStyle(Paint.Style.FILL_AND_STROKE);

    }
    @Override
    public boolean isPressed(double touchX, double touchY) {
        return distanceToCentreSquared(touchX, touchY) <= Math.pow(ButtonRadius, 2);
    }
    @Override
    public int[] handleActionDown(double touchX, double touchY, int pointerID) {
        if (isPressed(touchX, touchY)) {
            IsPressed = true;
            this.pointerID = pointerID;
        }
        // add in information to be returned
        return IsPressed? new int[]{1}:null;
    }
    @Override
    public int[] handleActionMove(double touchX, double touchY, int pointerID) {
        if (isPressed(touchX, touchY) && (this.pointerID == pointerID)) {
            return new int[]{1};
        }
        IsPressed = false;
        this.pointerID = -1;
        return null;
    }
    @Override
    public int[] handleActionUp(int pointerID) {
        IsPressed = false;
        this.pointerID = -1;
        return null;
    }
    @Override
    public void update() {
        // No update needed
    }
    @Override
    public void draw(Canvas canvas) {
        Paint paint = IsPressed? ButtonPaintPressed:ButtonPaint;
        canvas.drawCircle((float) CentreX, (float) CentreY, (float) ButtonRadius, paint);

    }
}
