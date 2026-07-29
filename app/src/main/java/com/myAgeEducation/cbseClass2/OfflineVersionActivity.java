package com.myAgeEducation.cbseClass2;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class OfflineVersionActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_version);
    }

    public void onClickPlayStoreImage(View view)
    {
        try {
            //saveIfAdClicked();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.myAgeEducation.cbseClass3Paid"));
            startActivity(intent);
        }
        catch(Exception e)
        {
            Util.displayAlert("Cannot open play store. Open play store manually and search for CBSE Class 3", "Error", OfflineVersionActivity.this);
        }
    }
}
