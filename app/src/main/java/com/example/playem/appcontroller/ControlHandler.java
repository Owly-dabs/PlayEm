package com.example.playem.appcontroller;

import android.view.MotionEvent;

import com.example.playem.hid.interfaces.ChunkType;

public interface ControlHandler {
    class ValuePack{
        public ChunkType type;
        public int x=0;
        public int y=0;
        public float relPixelX =0;
        public float relPixelY =0;
        public int id=0;
    }
    ValuePack[] onEnter(float x, float y, int pointerId);
    ValuePack[] onExit(float x, float y, int pointerId);
    ValuePack[] onMove(float x, float y, int pointerId);
    ValuePack[] onMotionEvent(MotionEvent motionEvent);
}
