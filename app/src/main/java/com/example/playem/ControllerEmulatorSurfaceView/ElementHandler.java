package com.example.playem.ControllerEmulatorSurfaceView;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.example.playem.InteractableElements.ControllerElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementHandler{
    private Map<Integer, List<ControllerElement>> PositionHashMap;
    private List<Integer> outputs = new ArrayList<>();
    private ControllerElement[] ELEMENTS;
    
    public ElementHandler(Map<Integer, List<ControllerElement>> PositionHashMap, ControllerElement[] ELEMENTS) {
        this.PositionHashMap = PositionHashMap;
        this.ELEMENTS = ELEMENTS;
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
                element.handleActionDown(touchX, touchY, pointerID);
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
                element.handleActionMove(touchX, touchY, pointerID);
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
                element.handleActionUp(pointerID);
            }
        }
    }
    public List<Integer> getOutputs(){
        return outputs;
    }
    public void ResetOutputs(){
        outputs = new ArrayList<>();
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
    }

}
