package com.myAgeEducation.cbseClass2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.util.Map;

public class LoginSignupActivity extends Activity {
    Button signup;
    String usernametxt;
    String passwordtxt;
    EditText name;
    EditText phone;
    SharedPreferences sharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup);

        //Util.addBannerAd((AdView) findViewById(R.id.adView));

        final String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d("Android", "Android ID : " + android_id);

        //Util.Android_id = android_id;

        name = findViewById(R.id.name);
        signup = findViewById(R.id.signup);

        signup.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                usernametxt = android_id;
                passwordtxt = "";
                final String name = ((EditText)findViewById(R.id.name)).getText().toString().trim();
                final String city = ((EditText)findViewById(R.id.editTextCity)).getText().toString().trim();

                if (name.isEmpty())
                {
                    displayAlertBox("Name cannot be blank", "Please provide your name");
                    return;
                }

                if(city.isEmpty())
                {
                    displayAlertBox("Enter city", "Please enter your city");
                    return;
                }

                TextView textView = (TextView) findViewById(R.id.textViewWaitingForData);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Connecting to the server, please wait...");
                enableButton(false);

                createUser(Util.generateEmailId(android_id), "password");
            }
        });
    }

    private void createUser(final String email, final String password)
    {
        Firebase ref = new Firebase(Util.FirebaseRoot);
        ref.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Toast.makeText(getApplicationContext(), "Successfully created user", Toast.LENGTH_LONG).show();
                loginAndSaveNewUserData(email, password);
                //System.out.println("Successfully created user account with uid: " + result.get("uid"));
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                switch(firebaseError.getCode())
                {
                    case FirebaseError.EMAIL_TAKEN:
                        break;

                    default:
                        displayAlertBox("Error", firebaseError.getMessage());
                        break;
                }
            }
        });
    }

    private void saveUserData(String uid)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefEdit = sharedPreferences.edit();
        prefEdit.putString("uuid", uid);
        prefEdit.commit();

        Util.UserUid = uid;

        String name = ((EditText)findViewById(R.id.name)).getText().toString().trim();
        String city = ((EditText)findViewById(R.id.editTextCity)).getText().toString().trim();

        Firebase ref = new Firebase(Util.UserRoot);
        ref.child(uid).child("name").setValue(name);
        ref.child(uid).child("city").setValue(city);
        ref.child(uid).child("dateOfSignUp").setValue(Util.getCurrentDateTime());
        ref.child(uid).child("numberOfTimesTestTaken").child("science").setValue(0);
        ref.child(uid).child("numberOfTimesTestTaken").child("maths").setValue(0);
        ref.child(uid).child("numberOfTimesTestTaken").child("computers").setValue(0);
        ref.child(uid).child("numberOfTimesTestTaken").child("moralscience").setValue(0);
        ref.child(uid).child("numberOfTimesTestTaken").child("english").setValue(0);
        ref.child(uid).child("numberOfTimesTestTaken").child("gk").setValue(0);
    }

    private void loginAndSaveNewUserData(String email, String password)
    {
        Firebase ref = new Firebase(Util.FirebaseRoot);
        ref.authWithPassword(email, password, new Firebase.AuthResultHandler(){
            @Override
            public void onAuthenticated(AuthData authData){
                Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_LONG).show();
                saveUserData(authData.getUid());
                Intent intent = new Intent(LoginSignupActivity.this, SubjectList.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError){
                displayAlertBox("Error", firebaseError.getMessage());
            }
        });
    }

	public void displayAlertBox(String title, String message)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(message);
        alert.setTitle(title);
        alert.setPositiveButton("OK", null);
        alert.setCancelable(true);
        alert.create().show();
    }

    private void enableButton(boolean isEnable)
    {
        Button signupButton = (Button)findViewById(R.id.signup);
        signupButton.setEnabled(isEnable);
        if(isEnable)
        {
            signupButton.setBackgroundColor(Color.BLACK);
        }
        else
        {
            signupButton.setBackgroundColor(Color.GRAY);
        }
    }
}
