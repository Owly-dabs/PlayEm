package com.example.playem.appsettings;

import java.util.List;

public class ProfileData {
    public ProfileData(String name,List<ControlsData> controlsData,GridData gridData){
        this.name = name;
        this.controlsList = controlsData;
        this.gridData = gridData;
    }
    String name;
    List<ControlsData> controlsList;
    GridData gridData;
}
