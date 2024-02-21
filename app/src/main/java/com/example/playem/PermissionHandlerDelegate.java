package com.example.playem;
import java.util.HashMap;

public interface PermissionHandlerDelegate {
    void RegisterRequests(PermissionsHandle resolution);
//Interface is is only responsible for permission status resolution. API calls does not initiate a permissions request.
//Apps should initiate their permission request through ContextCompact
    HashMap<String,PermissionsHandle> pending = new HashMap<>();
    void DeregisterRequests(String permissions);
}

