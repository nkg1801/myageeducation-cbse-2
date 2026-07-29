package com.myAgeEducation.cbseClass2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class MainActivity extends Activity {

    private String android_id;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.before_login);
        //android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        //Util.Android_id = android_id;
        //Log.d("AndroidId", android_id);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String savedLogin = sharedPreferences.getString("uuid", "");

        if(savedLogin.isEmpty() || savedLogin.length() < 32) {
            try {
                Firebase.goOnline();
                login();
            } catch (Exception ex) {
                findViewById(R.id.buttonRetry).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.txtHeading)).setText(ex.getMessage());
            }
        }
        else
        {
            Util.UserUid = savedLogin;
            Intent intent = new Intent(MainActivity.this, SubjectList.class);
            startActivity(intent);
            finish();
        }
    }

    private void login()
    {
        String email = Util.generateEmailId(android_id);
        String password = "password";
        Firebase ref = new Firebase(Util.FirebaseRoot);
        ref.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Util.UserUid = authData.getUid();

                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor prefEdit = sharedPreferences.edit();
                prefEdit.putString("uuid", Util.UserUid);
                prefEdit.commit();

                Intent intent = new Intent(MainActivity.this, SubjectList.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Intent intent;
                Log.d("Firebase Error", firebaseError.getMessage());
                switch (firebaseError.getCode()) {
                    case FirebaseError.USER_DOES_NOT_EXIST:
                        // user does not exist, send the bloody idiot to the sign-up page
                        intent = new Intent(MainActivity.this, LoginSignupActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case FirebaseError.PROVIDER_ERROR:
                        Log.d("Provider Error", firebaseError.getMessage());
                        findViewById(R.id.buttonRetry).setVisibility(View.VISIBLE);
                        break;

                    default:
                        findViewById(R.id.buttonRetry).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.txtHeading)).setText(firebaseError.getMessage());
                        break;
                }
            }
        });
    }

    public void onClickRetry(View view)
    {
        view.setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.txtHeading)).setText("Trying to connect to the server. Make sure you are connected to the Internet");
        login();
    }
}
