package com.example.playem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.playem.btmanager.PlayEmBTManager;
import com.example.playem.hid.HIDProfileBuilder;
import com.example.playem.pipes.PlayEmDataPipe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements PermissionHandlerDelegate {
    private ExecutorService BLEManagerExecutorPool;
    PlayEmBTManager mBTManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Button bleAdvertButton = findViewById(R.id.bAdvertise);
        bleAdvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleAdvertButton.setEnabled(false);
                mBTManager.AdvertiseHID();
            }
        });
        Button testButton = findViewById(R.id.bTest);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPipe.UpdateButtonNumber(b,testB);
                b+=1;
                if(b==8) {
                    b = 0;
                    testB = testB == 0x01 ? (byte)0x00 : 0x01;
                }
            }
        });
        this.BLEManagerExecutorPool = Executors.newSingleThreadExecutor(); //Executors.newFixedThreadPool(2);
        mBTManager = new PlayEmBTManager(this,this.BLEManagerExecutorPool,this.getMainExecutor());

        BuildAndPipeAll();
    }
    private int b = 0;
    private byte testB = 0x01;
    public PlayEmDataPipe dataPipe;
    public void BuildAndPipeAll(/*TODO Input GUI objects to bind to*/){

        HIDProfileBuilder builder = new HIDProfileBuilder();
        builder.Build();
        //Needs chunking now
        dataPipe = new PlayEmDataPipe(builder.GetChunks());
        mBTManager.GattServerInit(builder.GetReportMap(),new byte[]{},dataPipe);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                              int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i =0;i<permissions.length;i++){
            PermissionsHandle ph =this.pending.get(permissions[i]);
            if(ph==null){
                Log.e("PERM","Empty PermissionsHandle Object");
            }
            if(grantResults[i]== PackageManager.PERMISSION_GRANTED){
                ph.Granted();
            }
            else{
                ph.NotGranted();
            }
            DeregisterRequests(ph.getPermission());
        }
    }
    @Override
    public void RegisterRequests(PermissionsHandle resolution) {
        if(resolution.getPermission()!="PlayEm") {
            this.pending.put(resolution.getPermission(), resolution);
            //Put Stuff here to block UI call if needed
            if (this.shouldShowRequestPermissionRationale(resolution.getPermission())) {
                Log.w("PERM", resolution.Rationale()); //This should be the hook to an activity to show message
            }
        }
    }
    @Override
    public void DeregisterRequests(String permissions) {
        this.pending.remove(permissions);
    }
}

