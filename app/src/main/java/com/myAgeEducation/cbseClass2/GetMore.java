package com.myAgeEducation.cbseClass2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GetMore extends Activity {
    private BaseAdapter _listAdapter;
    ListView _listView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getmore);
        _listView = findViewById(android.R.id.list);
        readGetMoreApps();
    }

    void readGetMoreApps()
    {
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("getmoreapps-new/" + Util.SyllabusAndGrade);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                try{
                    ArrayList<GetMoreApps> getMoreAppsList = new ArrayList<>();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        try {
                            GetMoreApps app = postSnapshot.getValue(GetMoreApps.class);
                            getMoreAppsList.add(app);
                        } catch (Exception e) {
                            Log.d("CBSE_ERROR", e.getMessage());
                        }
                    }
                    if(getMoreAppsList.size() > 0) {
                        populateAdapterNew(getMoreAppsList);
                    }
                    else
                    {
                        Util.displayAlert("We could not find handpicked apps", "No apps found", GetMore.this);
                    }
                }
                catch(Exception e)
                {
                    findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                    Util.displayAlert("We could not find handpicked apps", "No apps found", GetMore.this);
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
                Log.d("Exception: ", firebaseError.getMessage());
                Util.displayAlert("We could not find handpicked apps", "No apps found", GetMore.this);
            }
        });
    }

    private void populateAdapterNew(ArrayList<GetMoreApps> list) {

        _listAdapter = new ListViewAdapterForGetMore(GetMore.this, list);//, appTitle, appDescription, appLink);
        _listView.setAdapter(_listAdapter);
    }

    private void populateAdapter() {
        ArrayList<Integer> appImage = new ArrayList<>();
        ArrayList<String> appTitle = new ArrayList<>();
        ArrayList<String> appDescription = new ArrayList<>();
        ArrayList<String> appLink = new ArrayList<>();

        /*appImage.add(R.drawable.g2048);
        appTitle.add("my 2048");
        appDescription.add("my 2048 is a numerical puzzle. It helps in developing analytical skills and quick thinking. The game is played by swiping left/right/top/bottom. The similar adjacent numbers will add up to make bigger numbers. Minimum target of the game is to achieve the number tile 256, though you may achieve much higher than this.");
        appLink.add("https://play.google.com/store/apps/details?id=moderndayeducation.game2048");
*/
        appImage.add(R.drawable.aedugami_3);
        appTitle.add("aedugami CBSE-3");
        appDescription.add("Aedugami CBSE-3 is a Class-3 app with 1000+ questions on various subjects.");
        appLink.add("https://play.google.com/store/apps/details?id=com.aedugami.cbseClass3");

        appImage.add(R.drawable.aedugami_4);
        appTitle.add("aedugami CBSE-4");
        appDescription.add("Aedugami CBSE-4 is a Class-4 app with 1000+ questions on various subjects.");
        appLink.add("https://play.google.com/store/apps/details?id=com.aedugami.cbseClass4");

        appImage.add(R.drawable.aedugami_5);
        appTitle.add("aedugami CBSE-5");
        appDescription.add("Aedugami CBSE-5 is a Class-5 app with 1000+ questions on various subjects.");
        appLink.add("https://play.google.com/store/apps/details?id=com.aedugami.cbseClass5");

        appImage.add(R.drawable.aedugami_7);
        appTitle.add("aedugami CBSE-7");
        appDescription.add("Aedugami CBSE-7 is a Class-7 app with 1000+ questions on various subjects.");
        appLink.add("https://play.google.com/store/apps/details?id=com.aedugami.cbseClass7");

        _listAdapter = new ListViewAdapterForGetMore(GetMore.this, appImage, appTitle, appDescription, appLink);
        _listView.setAdapter(_listAdapter);
    }
}
