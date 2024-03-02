package com.example.playem;

import android.content.Context;

public interface UserErrorNotification {
    public String ToastMessage(Context activity, Exception e, String Message);

}
