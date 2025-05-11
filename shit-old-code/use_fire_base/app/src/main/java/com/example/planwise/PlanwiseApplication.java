// PlanwiseApplication.java
package com.example.planwise;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class PlanwiseApplication extends Application {
    private static final String TAG = "PlanwiseApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化Firebase
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase初始化完成");
    }
}