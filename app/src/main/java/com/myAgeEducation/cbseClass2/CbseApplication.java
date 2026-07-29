package com.myAgeEducation.cbseClass2;

import android.app.Application;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;

public class CbseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Firebase.setAndroidContext(this);
        //FirebaseApp.initializeApp(this);
    }
}
