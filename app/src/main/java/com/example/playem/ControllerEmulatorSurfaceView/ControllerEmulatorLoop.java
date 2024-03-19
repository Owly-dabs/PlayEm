package com.example.playem.ControllerEmulatorSurfaceView;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

class ControllerEmulatorLoop extends Thread{
    private static final double MaxUps = 100;
    private static final double TargetUpsPeriod = 1E+3/MaxUps;
    private boolean isRunning = false;
    private final SurfaceHolder surfaceHolder;
    private final ControllerEmulator controllerEmulator;
    private double averageUPS;

    protected void Stop(){
        synchronized ((Object) isRunning){
            isRunning = false;
        }
    }
    public ControllerEmulatorLoop(ControllerEmulator controllerEmulator, SurfaceHolder surfaceHolder) {
        this.controllerEmulator = controllerEmulator;
        this.surfaceHolder = surfaceHolder;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public void startloop() {
        isRunning = true;
        start();
    }

    @Override
    public void run() {
        super.run();
        long loopretry=0;

        // Declare time and cycle counts
        int UpdateCount = 0;

        long startTime;
        long elapsedTime;
        long sleepTime;

        // Emulator loop
        Canvas canvas;
        startTime = System.currentTimeMillis();
        while (isRunning) {
            //Try to update and render controller Emulator
            try {
                canvas = surfaceHolder.lockCanvas();//
                if(canvas==null){
                    loopretry++;
                    Log.i("LOOP",String.format("Emulator Loop failed to lock canvas %d",loopretry));
                    continue;
                }
                // Stop threads from updating emulator multiple times
                synchronized (surfaceHolder) {
                    controllerEmulator.update();
                    //increment update count
                    UpdateCount++;
                    controllerEmulator.draw(canvas);
                }
                surfaceHolder.unlockCanvasAndPost(canvas);
            } catch (Exception e){
                Log.e("LOOP",e.toString());

            }


            //pause  Controller Emulator loop to not exceed target UPS
            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long) (UpdateCount*TargetUpsPeriod - elapsedTime);
            if (sleepTime > 0) {
                try {
                    sleep(sleepTime);
                } catch (InterruptedException error) {
                    error.printStackTrace();
                }
            }

            //skip frames to keep with target UPS
            while (sleepTime <0) {
                controllerEmulator.update();
                UpdateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long) (UpdateCount*TargetUpsPeriod - elapsedTime);
            }

            //calculate average UPS
            elapsedTime = System.currentTimeMillis() - startTime;

            // calculate number of updates after 1 second has passed
            if (elapsedTime >= 1000) {
                averageUPS = UpdateCount/(1E-3 * elapsedTime);
                UpdateCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
        synchronized (this){
            this.notifyAll();
        }
    }
}
