package com.example.playem.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playem.ControllerEmulatorSurfaceView.ControllerEmulator;

public class EmulatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //setContentView(R.layout.activity_main);
        setContentView(new ControllerEmulator(this));
//        setContentView(R.layout.activity_thumbstick);

    }
}
