package com.example.playem.appsettings;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.List;

public class ProfileData {
    public ProfileData(String name,List<ControlsData> controlsData,GridData gridData){
        this.name = name;
        this.controlsList = controlsData;
        this.gridData = gridData;
    }
    public String name;
    public List<ControlsData> controlsList;
    public GridData gridData;

    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(ControlsData cd : controlsList){
            sb.append("        ");
            sb.append(cd.toString());
            sb.append("\n");
        }
        return String.format("Title: %s\n    tControls: %d\n%sGrid:\n%s",name,controlsList.size(), sb,gridData.toString());
    }
}
