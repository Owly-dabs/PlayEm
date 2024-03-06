package com.example.playem.InteractableElements;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.fragment.app.Fragment;

public class ThumbStick2 extends View {

    private static final int OUTER_CIRCLE_COLOR = Color.GRAY;
    private static final int INNER_CIRCLE_COLOR = Color.LTGRAY;

    private static final float OUTER_CIRCLE_RADIUS = 100f;
    private static final float INNER_CIRCLE_RADIUS = 40f;

    private float CentreX;
    private float CentreY;

    private float innerCircleX;
    private float innerCircleY;

    private Paint outerCirclePaint;
    private Paint innerCirclePaint;

    private boolean isPressed = false;

    public ThumbStick2(Context context) {
        super(context);
        init();
    }

    public ThumbStick2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThumbStick2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        outerCirclePaint = new Paint();
        outerCirclePaint.setColor(OUTER_CIRCLE_COLOR);
        outerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(INNER_CIRCLE_COLOR);
        innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        CentreX = w / 2f;
        CentreY = h / 2f;
        innerCircleX = CentreX;
        innerCircleY = CentreY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(CentreX, CentreY, OUTER_CIRCLE_RADIUS, outerCirclePaint);
        canvas.drawCircle(innerCircleX, innerCircleY, INNER_CIRCLE_RADIUS, innerCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInsideInnerCircle(event.getX(), event.getY())) {
                    isPressed = true;
                    updateInnerCirclePosition(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPressed) {
                    updateInnerCirclePosition(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                isPressed = false;
                updateInnerCirclePosition(CentreX, CentreY);
                break;
        }
        return true;
    }

    private boolean isInsideInnerCircle(float x, float y) {
        float distance = (x - CentreX) * (x - CentreX) + (y - CentreY) * (y - CentreY);
        return distance <= INNER_CIRCLE_RADIUS * INNER_CIRCLE_RADIUS;
    }

    private void updateInnerCirclePosition(float x, float y) {
        // Ensure the inner circle stays inside the outer circle
        float deltaX = x - CentreX;
        float deltaY = y - CentreY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance <= OUTER_CIRCLE_RADIUS - INNER_CIRCLE_RADIUS) {
            innerCircleX = x;
            innerCircleY = y;
        } else {
            float ratio = (OUTER_CIRCLE_RADIUS - INNER_CIRCLE_RADIUS) / distance;
            innerCircleX = CentreX + deltaX * ratio;
            innerCircleY = CentreY + deltaY * ratio;
        }
        invalidate(); // Redraw the view
    }
}

