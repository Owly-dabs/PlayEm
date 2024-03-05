package com.example.playem.ControllerEmulatorSurfaceView;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

class ControllerEmulatorLoop extends Thread{
    private static final double MaxUps = 60;
    private static final double TargetUpsPeriod = 1E+3/MaxUps;
    private boolean isRunning = false;
    private SurfaceHolder surfaceHolder;
    private ControllerEmulator controllerEmulator;
    private double averageUPS;


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
                canvas = surfaceHolder.lockCanvas();
                // Stop threads from updating emulator multiple times
                synchronized (surfaceHolder) {
                    controllerEmulator.update();
                    //increment update count
                    UpdateCount++;
                    controllerEmulator.draw(canvas);
                }
                surfaceHolder.unlockCanvasAndPost(canvas);
            } catch (IllegalArgumentException error){
                error.getStackTrace();
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
    }
}
