package com.example.playem.testutils;

import android.util.Log;

import com.example.playem.pipes.PlayEmDataPipe;

import java.util.TimerTask;

public class saturationTest {
    public saturationTest(){}

    double angle = 0.0;
    double radiusJoy = 0.5;
    int nButton, idxbutton =0;
    PlayEmDataPipe dataPipe;
    double t = 0;
        public TimerTask runTest(PlayEmDataPipe _dataPipe, double radps, double radiusRatio, int updateMillis, int millisPerbutton){
            radiusJoy  = radiusRatio>1?1:radiusRatio;
            this.dataPipe = _dataPipe;
            nButton = 8;
            if(this.dataPipe!=null)
                Log.i("TEST","DataPipe Valid");
            return new TimerTask() {
                @Override
                public void run() {

                    final int maxValue = (int)(0.5*0xFFFF);
                    final double fullA =2*Math.PI;
                    final double radpc = radps/1000*updateMillis;

                    t = t>millisPerbutton?0:t+updateMillis;

                    angle = (angle > fullA) ? 0 : (angle + radpc);
                    int x =  (int)((Math.sin(angle))*maxValue)+0xFFFF;
                    int y =  (int)((Math.cos(angle))*maxValue)+0xFFFF;
                    dataPipe.UpdateAxis(1, y);
                    dataPipe.UpdateAxis(0, x);
                    dataPipe.UpdateAxis(2, -y);
                    dataPipe.UpdateAxis(3, x);
                    dataPipe.UpdateAxis(4, -x);
                    if(t>millisPerbutton){
                        idxbutton = (idxbutton+1)%nButton;
                        Log.i("TEST",String.format("Running on %s",Thread.currentThread().getName()));
                    }
                    for(int i = 0;i<nButton;i++){ //Updates all buttons but this is so since it is a saturation test
                        dataPipe.UpdateButtonNumber(i,false);
                    }
                    dataPipe.UpdateButtonNumber(idxbutton,true);
                }
            };
        }
    }
