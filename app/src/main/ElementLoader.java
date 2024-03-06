package com.example.playem;

import com.example.playem.InteractableElements.*;
//import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


// takes in an an array of interactable element object and loads in it into a hashmap
public class ElementLoader {
    private SpriteBaseClass[] ELEMENTS;
    private SpriteBaseClass[] PositionHashMap;
    private final DisplayMetrics displayMetrics = new DisplayMetrics();
    private int lengthX = displayMetrics.widthPixels;
    private int lengthY = displayMetrics.heightPixels;



    private int[] HashElement(SpriteBaseClass element) {
        return new int[]{};
    }
    public void loadELEMENTS(SpriteBaseClass[] ELEMENTS) {
        this.ELEMENTS = ELEMENTS;
        for (SpriteBaseClass element: this.ELEMENTS) {
            for (int i: HashElement(element)) {
                PositionHashMap[i] = element;
            }
        }
    }
    public SpriteBaseClass[] GetPositionHashMap() {
        return PositionHashMap;
    }
}
