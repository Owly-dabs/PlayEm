package com.example.playem.viewmodels;

public class GattServiceState {
    public boolean Objectdirty;

    public GattServiceState(String name, String address, String BondState, String bondable, SERVICE_STATUS serviceStatus,boolean objdirty){
        this.name = name;
        this.address = address;
        bondstate = BondState;
        bondableList = bondable;
        status = serviceStatus;
        Objectdirty = objdirty;
    }
    public String address,name,bondstate;
    public String bondableList;
    public SERVICE_STATUS status;

    public GattServiceState copy(boolean objectdirty){
        String a = new String(address);
        String n = "Unknown";
        if(this.name!=null)
            n = new String(name);
        return new GattServiceState(n,a,this.bondstate,"Copied Data",this.status,objectdirty);
    }
    public enum SERVICE_STATUS{
        NOT_INIT,
        NO_FOCUS,
        PERMISSIONS_WAIT,
        IDLE_NODATA,
        BUILT_READY_BROADCAST,
        ADVERT,
        CONNECTING,
        CONNECTED_IDLE,
        NOTIFY,
        DONT_TOUCHME
    }
}
