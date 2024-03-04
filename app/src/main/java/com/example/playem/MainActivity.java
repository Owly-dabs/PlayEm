package com.example.playem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.generics.PermissionHandlerDelegate;
import com.example.playem.generics.PermissionsHandle;
import com.example.playem.generics.PermissionsHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends AppCompatActivity implements PermissionHandlerDelegate {
    /*private ExecutorService BLEManagerExecutorPool;
    PlayEmBTManager mBTManager;
    private double a = 0;
    private boolean aswing = true;
    private int b = 0;
    private boolean testB = true;
    public PlayEmDataPipe dataPipe;
    private Timer genericTimer = new Timer("Report Test");
    private boolean gTimerActive = false;*/
    PlayEmGATTService gattService;
    boolean isBound = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: Move this to after surface view is started
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        permissionHandler = new BTPermissionHandler("PlayEm BT Manager", btPermissionsHandles);
        if(!permissionHandler.CheckAllPermissions(this)) {
            permissionHandler.RequestMissingPermissions(this);
        }
        Button bleAdvertButton = findViewById(R.id.bAdvertise);

        bleAdvertButton.setOnClickListener(v -> {

            //bleAdvertButton.setEnabled(false);
            //mBTManager.AdvertiseHID();
        });
       /* Button testButton = findViewById(R.id.bTest);
        testButton.setOnClickListener(v -> {
            if(!gTimerActive){
                Executor uiExecutor = this.getMainExecutor();
                genericTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        uiExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                dataPipe.UpdateButtonNumber(b/1000,testB);
                                b+=1000/10; //one second per actuation
                                if(b==(1000*8)) {
                                    b = 0;
                                    testB = !testB;
                                }
                                if(Math.abs(a)>1)
                                    aswing=!aswing;
                                a = aswing?a-0.01:a+0.01;
                                double aa = a;
                                for(int i =0;i<5;i++){
                                    dataPipe.UpdateAxis(i,(int)(Math.sin(aa)*0xFFFF),aa<0?true:false);
                                    aa = -aa;
                                }
                            }
                        });
                    }
                },1000,10);
            }else{
                genericTimer.purge();
            }
            gTimerActive = !gTimerActive;
        });
            gattService.BuildAndPipeAll();
        }

*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayEmGATTService.class);
        startService(intent);
        if(!bindService(intent, gattServiceConnection,BIND_AUTO_CREATE)) {
            Log.e("BINDER","Error in binding");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i("APP","onResume entered");
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(isBound){
            unbindService(gattServiceConnection);
        }
    }

    private final ServiceConnection gattServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayEmGATTService.mBinder binderLink = (PlayEmGATTService.mBinder) service;
            gattService = binderLink.getService();
            isBound = true;
            gattService.DeferredConstructor(MainActivity.this, new GattServiceCallbacks() {
                @SuppressLint("MissingPermission")
                @Override
                public Runnable onBondedDevicesChange(ConcurrentLinkedQueue<BluetoothDevice> bondedDevices) {
                    return () -> {
                        BluetoothDevice[] bondedList = (BluetoothDevice[]) bondedDevices.toArray();
                        TextView tv = findViewById(R.id.tBondedList);
                        String s ="";
                        for(BluetoothDevice d : bondedList){
                            s+=String.format("%s %s\n",d.getName(),d.getAddress());
                        }
                        tv.setText(s);
                    };
                }
                @Override
                public Runnable onConnectionStateChanged(String d_address, String d_name, PlayEmGATTService.SERVICE_STATES state) {
                    return ()->{
                        TextView t = findViewById(R.id.tHostName);
                        if (Objects.requireNonNull(state) == PlayEmGATTService.SERVICE_STATES.BLUETOOTH_DEVICE_DISCONNECT) {
                            t.setText("DISCONNECTED");
                        } else {
                            t.setText(String.format("%s %s", d_name, d_address));
                        }
                    };
                }
                @Override
                public Runnable onAdvertisementStateChanged(PlayEmGATTService.SERVICE_STATES state) {
                    return ()->{
                        Button b = findViewById(R.id.bAdvertise);
                        b.setEnabled(state != PlayEmGATTService.SERVICE_STATES.ADVERT_ENABLED);
                    };
                }
                @Override
                public Runnable onNotifierChanged(PlayEmGATTService.SERVICE_STATES state) {
                    return ()->{
                        Button b = findViewById(R.id.bTest);
                        if(state== PlayEmGATTService.SERVICE_STATES.NOTIFY_DETACHED){
                            b.setEnabled(false);
                        }
                    };
                }

                @Override
                public Runnable onServicesAddComplete(PlayEmGATTService.SERVICE_STATES state) {
                    return ()-> {
                        Button b = findViewById(R.id.bTest);
                        b.setEnabled(true);
                    };
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            gattService= null;
        }
    };
/*
    public Runnable UpdateBondedLists(){ return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                TextView tbond = findViewById(R.id.tBondedList);
                String bondedList="";
                while(!mBTManager.bondedDevices.isEmpty()){
                    BluetoothDevice d =Objects.requireNonNull(mBTManager.bondedDevices.poll());
                    bondedList += d.getName();
                    bondedList += String.format(":  %s\n",d.getAddress());
                }
                tbond.setText(bondedList);
            }
        };
    }
    public Runnable UpdateAvailDevicesList = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            TextView tDevices = findViewById(R.id.tDevices);
            String s_devices="";
            while(!mBTManager.availDevices.isEmpty()){
                BluetoothDevice d =Objects.requireNonNull(mBTManager.availDevices.poll());
                s_devices += d.getName();
                s_devices += String.format(":  %s\n",d.getAddress());
            }
            tDevices.setText(s_devices);
        }
    };
    public Runnable DeviceConnected = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            TextView tHost = findViewById(R.id.tHostName);
            String s_devices="";
            BluetoothDevice d =  mBTManager.connectedDevice.poll();
            while(!mBTManager.connectedDevice.isEmpty()){
                d =Objects.requireNonNull(mBTManager.connectedDevice.poll());
            }
            s_devices += d.getName();
            s_devices += String.format(":  %s\n",d.getAddress());
            tHost.setText(s_devices);
        }
    };

*/


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i =0;i<permissions.length;i++){
            PermissionsHandle ph =this.pending.get(permissions[i]);
            if(ph==null){
                Log.e("PERM","Empty PermissionsHandle Object");
            }
            assert ph != null;
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
        if(!Objects.equals(resolution.getPermission(), "PlayEm")) {
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
    private final PermissionsHandle[] btPermissionsHandles = new PermissionsHandle[]{
            new BTPermissionHandle(android.Manifest.permission.BLUETOOTH_CONNECT),
            new BTPermissionHandle(android.Manifest.permission.BLUETOOTH_SCAN),
            new BTPermissionHandle(android.Manifest.permission.BLUETOOTH_ADVERTISE),
            new BTPermissionHandle(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            new BTPermissionHandle(Manifest.permission.ACCESS_FINE_LOCATION),
    };

    private BTPermissionHandler permissionHandler;
    protected  class BTPermissionHandler extends PermissionsHandler {
        public BTPermissionHandler(String GroupName, PermissionsHandle[] Permissions_to_track) {
            super(GroupName, Permissions_to_track);
        }

        @Override
        protected void RequestMissingPermissions(AppCompatActivity context) {
            ArrayList<String> requires = new ArrayList<>();
            for(PermissionsHandle s: btPermissionsHandles)
            {
                if(MainActivity.this.checkSelfPermission(s.getPermission())!= PackageManager.PERMISSION_GRANTED){
                    requires.add(s.getPermission());
                    Log.w("PERM","Requiring:"+s);
                    ((PermissionHandlerDelegate)context).RegisterRequests(s);
                }
            }
            if(!requires.isEmpty()){
                String[] requiredPerm = new String[requires.size()];
                requiredPerm= requires.toArray(requiredPerm);
                context.requestPermissions(requiredPerm,2); //Could use request code for a hashed event system from activity
                //this.requestPermissions(requiredPerm,11);
            }
        }
    }
        protected static class BTPermissionHandle implements PermissionsHandle{
            protected BTPermissionHandle(String permission){
                Permission = permission;
            }
            protected String Permission;
            @Override
            public void NotGranted() {
                Log.e("PERM",Permission +" Not Granted!");
            }
            @Override
            public void Granted() {

            }
            @Override
            public String Rationale() {
                return "null";
            }
            @NonNull
            @Override
            public String toString(){
                return new String(Arrays.copyOf(Permission.toCharArray(),Permission.length()));
            }
            public String getPermission(){
                return this.toString();
            }
        }
}

