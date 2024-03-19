package com.example.playem.pipes;

import com.example.playem.ControllerEmulatorSurfaceView.interfaces.Buildable;
import com.example.playem.PlayEmGATTService;
import com.example.playem.hid.HIDProfileBuilder;

import java.util.List;

public class PipeBuilder {
    public void BuildPipe(List<Buildable> componentList, PlayEmGATTService currentServiceInstance){
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
                        bb.onInputReportPipeID(axesC);
                        butC++;
                        break;
                }
            }
            builderObject.StartJoyStick();
            builderObject.AddButtons(butC,(byte)0x02);
            builderObject.AddAxes(axesC,(byte)0x02);
            builderObject.EndJoyStick();
            builderObject.Build();
            PlayEmDataPipe pipe = new PlayEmDataPipe(builderObject.GetChunks());
            currentServiceInstance.BuildPipe(builderObject,pipe);
            for(Buildable bb:componentList){
                bb.onSetDataPipe(pipe);
            }
            //currentServiceInstance.StartAdvertisement();
        }
    }
}
