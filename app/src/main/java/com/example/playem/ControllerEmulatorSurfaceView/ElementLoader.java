package com.example.playem.ControllerEmulatorSurfaceView;

import com.example.playem.InteractableElements.*;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// takes in an an array of interactable element object and loads in it into a hashmap
public class ElementLoader {
    private ControllerElement[] ELEMENTS;
    private Map<Integer, List<ControllerElement>> PositionHashMap = new HashMap<>();
//    private static final DisplayMetrics displayMetrics = new DisplayMetrics();
//    private int lengthX = displayMetrics.widthPixels;
//    private int lengthY = displayMetrics.heightPixels;

    public static final int NUM_GRIDS = 576; // chosen from average screen size 16:9 -> 32:18 (32*18)
    public static double gridSize = (Resources.getSystem().getDisplayMetrics().widthPixels *
                            Resources.getSystem().getDisplayMetrics().heightPixels)/ (double) NUM_GRIDS;

//    public ElementLoader(){
//        int PlaneSize = lengthX * lengthY;
//        gridSize = PlaneSize/ NUM_GRIDS;
//    }

    // Hashing may be expensive but only has to be done once when loading chosen elements
    // As we may have to check every grid (NUM_GRIDS) to see if the element falls in it
    // Depending on the element radius size
    private void HashElement(ControllerElement element) {
        // Calculate min and max X of Grid
        int minGridX = Math.max(0, (int) ((element.CentreX - element.radius)/gridSize));
        int maxGridX = Math.max(NUM_GRIDS, (int) ((element.CentreX + element.radius)/gridSize));

        // Calculate min and max Y of Grid
        int minGridY = Math.max(0, (int) ((element.CentreY - element.radius)/gridSize));
        int maxGridY = Math.max(NUM_GRIDS, (int) ((element.CentreY + element.radius)/gridSize));

        for (int x=minGridX; x<=maxGridX; x++) {
            for (int y=minGridY; y<=maxGridY; y++) {
                int key = x * NUM_GRIDS + y;
                if (!PositionHashMap.containsKey(key)) {
                    PositionHashMap.put(key, new ArrayList());
                } PositionHashMap.get(key).add(element);
            }
        }
    }

    // this only needs to be done once before entering the controller emulator.
    // thus we can spend extra time to "hash" all the elements before loading them in
    public void loadELEMENTS(ControllerElement[] ELEMENTS) {
        this.ELEMENTS = ELEMENTS;
        // Hash every element
        for (ControllerElement element: this.ELEMENTS) {
                HashElement(element);
            }
    }
    public Map<Integer, List<ControllerElement>> GetPositionHashMap() {
        return PositionHashMap;
    }
}
