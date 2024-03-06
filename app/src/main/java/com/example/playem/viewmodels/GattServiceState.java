package com.example.playem.viewmodels;

public class GattServiceState {
    public GattServiceState(String name, String address, String BondState,String bondable, SERVICE_STATUS serviceStatus){
        this.name = name;
        this.address = address;
        bondstate = BondState;
        bondableList = bondable;
        status = serviceStatus;
    }
    public String address,name,bondstate;
    public String bondableList;
    public SERVICE_STATUS status;

    public GattServiceState copy(){
        return new GattServiceState(this.name,this.address,this.bondstate,"Copied Data",this.status);
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
