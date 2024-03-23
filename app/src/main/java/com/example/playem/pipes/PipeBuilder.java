package com.example.playem.pipes;

import android.util.Log;

import com.example.playem.appcontroller.interfaces.Buildable;
import com.example.playem.AppGattService;
import com.example.playem.hid.HIDProfileBuilder;

import java.util.List;

public class PipeBuilder {
    public PlayEmDataPipe BuildPipe(List<Buildable> componentList, AppGattService currentServiceInstance){
        HIDProfileBuilder builderObject = new HIDProfileBuilder();
        if(componentList!=null && currentServiceInstance!=null){
            int axesC = 0;
            int butC = 0;
            for(Buildable bb : componentList){
                switch(bb.GetChunkType()) {
                    case AXES2:
                        bb.onInputReportPipeID(axesC);
                        axesC+=2;
                        break;
                    case AXES:
                        bb.onInputReportPipeID(axesC);
                        axesC++;
                        break;
                    case BUTTONS:
                        bb.onInputReportPipeID(butC);
                        butC++;
                        break;
                }
            }
            builderObject.StartJoyStick();
            try{
                builderObject.AddButtons(butC,(byte)0x02);
                builderObject.AddAxes(axesC,(byte)0x02);
            }catch(Exception e){
                Log.e("PIPE",String.format("%d items",componentList.size()));
            }
            builderObject.EndJoyStick();
            builderObject.Build();
            PlayEmDataPipe pipe = new PlayEmDataPipe(builderObject.GetChunks());
            currentServiceInstance.BuildPipe(builderObject,pipe);
            for(Buildable bb:componentList){
                bb.onSetDataPipe(pipe);
            }
            return pipe;
            //currentServiceInstance.StartAdvertisement();
        }
        return null;
    }
}
