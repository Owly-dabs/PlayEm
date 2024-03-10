package com.example.playem;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playem.InteractableElements.ThumbStick2_archived;
import com.example.playem.R;

public class MainActivity_test_archived extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_thumbstick);

        // Find the ThumbStick2 view in the layout
        //ThumbStick2_archived thumbStick1 = findViewById(R.id.thumbStick2);

    }
}
