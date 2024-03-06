package com.example.playem.ControllerEmulatorSurfaceView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.playem.InteractableElements.*;
//import com.example.playem.InteractableElements.ThumbStick;
import com.example.playem.R;

// ControllerEmulator manages all objects inside of the Controller Emulator state
// Responsible for updating states and rendering all objects on screen
public class ControllerEmulator extends SurfaceView implements SurfaceHolder.Callback {
    // Single ThumbStick for testing
    private final ThumbStick thumbStick;
    //Single Button for testing
    private final ControllerButton button;
    private ThumbStick[] ThumbSticks;
    private ControllerButton[] Buttons;
    private ControllerEmulatorLoop CELoop;


    public ControllerEmulator(Context context) {
        super(context);

        //get Surface Holder and callback
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        //instantiate ControllerEmulatorLoop
        CELoop = new ControllerEmulatorLoop(this, surfaceHolder);

        // Instantiate Joysticks and buttons
        thumbStick = new ThumbStick(275, 350, 50, 70); // for testing
        button = new ControllerButton(275, 500, 50); //for testing

        //set focus to true
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerID = event.getPointerId(pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                thumbStick.handleActionDown((double) event.getX(), (double) event.getY()); // for testing
                button.handleActionDown((double) event.getX(), (double) event.getY()); // for testing
                return true;
                // run ElementHandler.handleActionDown(...)
            case MotionEvent.ACTION_MOVE:
                thumbStick.handleActionMove((double) event.getX(), (double) event.getY()); // for testing
                return true;
                // run ElementHandler.handleActionUp(...)
            case MotionEvent.ACTION_UP:
                thumbStick.handleActionUp(); // for testing
                button.handleActionUp(); // for testing
                //run ElementHandler.handleActionDown(...)
                return true;

        } return super.onTouchEvent(event); // return elementHandler.output()
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        CELoop.startloop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        // Draw update text on screen
        drawUPS(canvas);

        //Draw Buttons, Joysticks and ThumbSticks
        thumbStick.draw(canvas); // for testing
        button.draw(canvas); // for testing
    }

    // for testing update speed with multiple elements rendered on screen
    public void drawUPS(Canvas canvas) {
        String averageUPS = Double.toString(CELoop.getAverageUPS());
        Paint paint = new Paint();
        int colour = ContextCompat.getColor(this.getContext(), R.color.white);
        paint.setColor(colour);
        paint.setTextSize(50);
        canvas.drawText("UPS: " + averageUPS, 100, 200, paint);
    }

    public void update() {
        thumbStick.update();
    }
}
