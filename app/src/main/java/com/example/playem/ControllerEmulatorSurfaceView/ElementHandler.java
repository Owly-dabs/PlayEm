package com.example.playem.ControllerEmulatorSurfaceView;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.Output;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.playem.InteractableElements.ControllerElement;
import com.example.playem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ElementHandler{
    private Map<Integer, List<ControllerElement>> PositionHashMap;
    private  Map<Integer, int[]> OutputsMap;
    private ControllerElement[] ELEMENTS;
    public ElementHandler(Map<Integer, List<ControllerElement>> PositionHashMap, ControllerElement[] ELEMENTS) {
        this.PositionHashMap = PositionHashMap;
        this.ELEMENTS = ELEMENTS;

        // Create OutputsMap for every element in ELEMENTS
        this.OutputsMap = new HashMap<>();
        for (ControllerElement element: ELEMENTS) {
            OutputsMap.put(element.elementID, null);
        }
    }

    public int getKey(double touchX, double touchY) {
        int gridX = Math.min(Math.max((int) (touchX / ElementLoader.gridSize), 0), ElementLoader.NUM_GRIDS - 1);
        int gridY = Math.min(Math.max((int) (touchY / ElementLoader.gridSize), 0), ElementLoader.NUM_GRIDS - 1);
        return gridX * ElementLoader.NUM_GRIDS + gridY;
    }
    public void handleTouchStart(MotionEvent event) {
        for (int i=0; i < event.getPointerCount(); i++){
            int pointerID = event.getPointerId(i);

            double touchX = event.getX(i);
            double touchY = event.getY(i);

            // calculate key
            int key = getKey(touchX, touchY);

            List<ControllerElement> ElementsInGrid = this.PositionHashMap.get(key);
            assert ElementsInGrid != null;
            for (ControllerElement element: ElementsInGrid) {
                int[] output = element.handleActionDown(touchX, touchY, pointerID);
                // set outputs
                OutputsMap.put(element.elementID, output);
            }
        }
    }
    public void handleTouchMove(MotionEvent event) {
        for (int i=0; i < event.getPointerCount(); i++){
            int pointerID = event.getPointerId(i);
            double touchX = event.getX(i);
            double touchY = event.getY(i);

            int key = getKey(touchX, touchY);

            List<ControllerElement> ElementsInGrid = this.PositionHashMap.get(key);
            assert ElementsInGrid != null;
            for (ControllerElement element: ElementsInGrid) {
                int[] output = element.handleActionMove(touchX, touchY, pointerID);
                // set outputs
                OutputsMap.put(element.elementID, output);
            }
        }
    }
    public void handleTouchEnd(MotionEvent event) {
        for (int i=0; i < event.getPointerCount(); i++){
            int pointerID = event.getPointerId(i);
            double touchX = event.getX(i);
            double touchY = event.getY(i);
            int key = getKey(touchX, touchY);

            List<ControllerElement> ElementsInGrid = this.PositionHashMap.get(key);
            assert ElementsInGrid != null;
            for (ControllerElement element: ElementsInGrid) {
                int[] output = element.handleActionUp(pointerID);
                //return output
                OutputsMap.put(element.elementID, output);
            }
        }
    }
    public Map<Integer, int[]> getOutputs(){
        return OutputsMap;

    }

    public void update() {
        for (ControllerElement element: ELEMENTS) {
            element.update();
        }
    }
    public void draw(Canvas canvas){
        for (ControllerElement element: ELEMENTS) {
            element.draw(canvas);
        }

        /// TESTING ///
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        String out = "";
        for (int elementid: getOutputs().keySet()) {
            int[] values = OutputsMap.get(elementid);
            out += elementid + ": " + (values != null? values[0]:null) + ", ";
        }
        //canvas.drawText("all outputs: " + getOutputs(), 50, 2000, paint);
        canvas.drawText("outputs: " + out, 50, 2000, paint);
        /// TESTING ///
    }

}
