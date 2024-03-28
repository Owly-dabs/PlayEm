package com.example.playem.appsettings;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class GridData {
    public GridData(float screenWidth,float screenHeight,float min_unit_len){
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.min_unit_len = min_unit_len;
    }
    public float screenWidth;
    public float screenHeight;
    public float min_unit_len;

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString(){
        return String.format("width: %f\nheight: %f\nmin unit: %f",screenWidth,screenHeight,min_unit_len);
    }
}
