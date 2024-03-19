package com.example.playem.InteractableElements;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


// Not used will be removed later
public class ThumbStick extends ControllerElement{

    private final Paint InnerCirclePaint;
    private final Paint OuterCirclePaint;
    private double InnerCirclePosX;
    private double InnerCirclePosY;
    private final double OuterCircleRadius;
    private final double InnerCircleRadius;
    private boolean IsPressed = false;
    private double actuatorX;
    private double actuatorY;
    private int pointerID = -1;

    // Constructor
    public ThumbStick(int elementID, double CentreX, double CentreY,
                      double InnerCircleRadius, double OuterCircleRadius) {
        super(elementID, CentreX, CentreY, true);

        // Defining Inner and Outer circle radii and positions
        this.InnerCirclePosX = CentreX;
        this.InnerCirclePosY = CentreY;
        super.radius = this.OuterCircleRadius = OuterCircleRadius;

        this.InnerCircleRadius = InnerCircleRadius;

        // Colour of ThumbSticks (temporary solution for now)
        this.OuterCirclePaint = new Paint();
        this.OuterCirclePaint.setColor(Color.GRAY);
        this.OuterCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        this.InnerCirclePaint = new Paint();
        this.InnerCirclePaint.setColor(Color.LTGRAY);
        this.InnerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    @Override
    public boolean isPressed(double touchX, double touchY) {
        return distanceToCentreSquared(touchX, touchY) < Math.pow(OuterCircleRadius, 2);
    }

    public void setActuator(double touchX, double touchY) {
        double dX = touchX - CentreX;
        double dY = touchY - CentreY;
        double dDistance = Math.sqrt(this.distanceToCentreSquared(touchX, touchY));
        // normalise vectors
        if (dDistance < OuterCircleRadius) {
            actuatorX = dX/OuterCircleRadius;
            actuatorY = dY/OuterCircleRadius;
        } else {
            actuatorX = dX/(dDistance);
            actuatorY = dY/(dDistance);
        }
    }
    public void resetActuator() {
        actuatorY = actuatorX = 0;
    }
    @Override
    public void update() {
        updateInnerCirclePos();
    }
    public void updateInnerCirclePos() {
        InnerCirclePosX = (int) CentreX + actuatorX*OuterCircleRadius*0.6;
        InnerCirclePosY = (int) CentreY + actuatorY*OuterCircleRadius*0.6;
    }
    @Override
    public int[] handleActionDown(double touchX, double touchY, int pointerID) {
        if (isPressed(touchX, touchY)) {
            this.IsPressed = true;
            this.pointerID = pointerID;
            return new int[]{1};
            // add in or return any needed information from thumbStick press
            // IsPressed is the state of the button
        } return null;
    }
    @Override
    public int[] handleActionMove(double touchX, double touchY, int pointerID) {
        if (IsPressed && (this.pointerID == pointerID)) {
            setActuator(touchX, touchY);
            return new int[]{(int) (actuatorX*OuterCircleRadius), (int) (actuatorY *OuterCircleRadius)};
            // add in or return any needed information from thumbStick move here
            // actuatorX and actuatorY are normalised values from within the thumbStick
        } return null;
    }
    @Override
    public int[] handleActionUp(int pointerID) {
        this.pointerID = -1;
        IsPressed = false;
        resetActuator();
        return null;
    }
    @Override
    public void draw(Canvas canvas){
        canvas.drawCircle((float) CentreX,
                          (float) CentreY,
                          (float) OuterCircleRadius,
                          OuterCirclePaint);

        canvas.drawCircle((float) InnerCirclePosX,
                          (float) InnerCirclePosY,
                          (float) InnerCircleRadius,
                          InnerCirclePaint);
    }
}
