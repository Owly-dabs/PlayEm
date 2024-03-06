package com.example.playem.InteractableElements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ControllerButton extends SpriteBaseClass implements GestureElement {
    private boolean IsPressed;
    private final double ButtonRadius;
    private final Paint ButtonPaint;
    private final Paint ButtonPaintPressed;

    public ControllerButton(double CentreX, double CentreY, double ButtonRadius) {
        super(CentreX, CentreY, false);
        this.ButtonRadius = ButtonRadius;

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
    public void handleActionDown(double touchX, double touchY) {
        IsPressed = isPressed(touchX, touchY);
        // add in information to be returned
        // IsPressed
    }
    @Override
    public void handleActionMove(double touchX, double touchY) {
        // add in information to be returned
        // IsPressed
    }
    @Override
    public void handleActionUp() {
        IsPressed = false;
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