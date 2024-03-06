package com.example.playem.generics;
import android.app.Activity;

import java.util.HashMap;

public interface PermissionHandlerDelegate {
    void RegisterRequests(PermissionsHandle resolution);
//Interface is is only responsible for permission status resolution. API calls does not initiate a permissions request.
//Apps should initiate their permission request through ContextCompact
    HashMap<String,PermissionsHandle> pending = new HashMap<>();
    void DeregisterRequests(String permissions);
    void SetFocusedActivity(Activity activity);
}

