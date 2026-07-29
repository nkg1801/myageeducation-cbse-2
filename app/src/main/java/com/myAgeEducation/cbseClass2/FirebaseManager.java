package com.myAgeEducation.cbseClass2;

import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class FirebaseManager {
    public static void updateTestReport(String testReportRoot)
    {
        Firebase ref = new Firebase(testReportRoot);
        String uuid = UUID.randomUUID().toString();
        final Firebase childRef = ref.child(Util.getCurrentDate()).child(uuid);
        childRef.setValue(Util.getCurrentTime() + "/" + Util.Subject + "/"+ Util.UserUid);
    }

    static void readAds(final FirebaseCallback firebaseCallback)
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("ads");
        databaseReference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                try{
                    Util.AdDetail = snapshot.getValue(Ads.class);
                    firebaseCallback.onCallback("1");
                }
                catch(Exception e)
                {
                    firebaseCallback.onCallback("0");
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d("Exception: ", firebaseError.getMessage());
                firebaseCallback.onCallback("0");
            }
        });
    }
}
