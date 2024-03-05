package com.example.playem.generics;

import android.content.Context;

public interface UserErrorNotification {
    public String ToastMessage(Context activity, Exception e, String Message);

}
