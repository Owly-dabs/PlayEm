package com.example.playem.ControllerEmulatorSurfaceView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.playem.InteractableElements.*;
import com.example.playem.R;


// ControllerEmulator manages all objects inside of the Controller Emulator state
// Responsible for updating states and rendering all objects on screen
public class ControllerEmulator extends SurfaceView implements SurfaceHolder.Callback {
    private final ControllerEmulatorLoop CELoop;
    private final ElementHandler elementHandler;


    public ControllerEmulator(Context context) {
        super(context);

        //get Surface Holder and callback
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        //instantiate ControllerEmulatorLoop
        CELoop = new ControllerEmulatorLoop(this, surfaceHolder);

        // declare Window dimensions


        // Instantiate Joysticks and buttons
        ElementLoader elementLoader = new ElementLoader();

        /// TESTING ///
        ThumbStick thumbStick = new ThumbStick(1,275, 350, 50, 70);
        ThumbStick thumbStick2 = new ThumbStick(2,275, 900, 60, 100);
        ControllerButton button = new ControllerButton(3,275, 500, 50);
        ControllerButton button2 = new ControllerButton(4,400, 500, 100);
        ControllerButton button3 = new ControllerButton(5,600, 500, 60);
        ControllerElement[] ELEMENTS = {thumbStick, thumbStick2, button, button2, button3};
        elementLoader.loadELEMENTS(ELEMENTS);
        /// TESTING ///


        elementHandler = new ElementHandler(elementLoader.GetPositionHashMap(), ELEMENTS);

        //set focus to true
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                elementHandler.handleTouchStart(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                elementHandler.handleTouchMove(event);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                elementHandler.handleTouchEnd(event);
                return true;

        }
        // pass elementHandler.getOutputs() to bluetooth stuff
        return super.onTouchEvent(event);
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

        //Draw Buttons, Joysticks and ThumbSticks
        // call ElementHandler.draw()
        elementHandler.draw(canvas);

        /// TESTING ///
        //thumbStick.draw(canvas); // for testing
        //button.draw(canvas); // for testing
        /// TESTING ///

        // Draw update text on screen
        drawUPS(canvas);

        Paint paint = new Paint();
        int colour = ContextCompat.getColor(this.getContext(), R.color.white);
        paint.setColor(colour);
        paint.setTextSize(50);
        canvas.drawText("Dimensions: " + Resources.getSystem().getDisplayMetrics().widthPixels +
                        "x" + Resources.getSystem().getDisplayMetrics().heightPixels,
                100, 100, paint);
        /// TESTING ///

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
        // call ElementHandler.update()
        elementHandler.update();
    }
}
