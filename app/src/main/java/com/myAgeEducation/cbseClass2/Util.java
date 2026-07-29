package com.myAgeEducation.cbseClass2;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.myAgeEducation.cbsecommon.AdData;
import com.myAgeEducation.cbsecommon.Question;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Util
{
    static Ads AdDetail;
    //public static String Android_id = "";
	public static ArrayList<Question> allQuestions = new ArrayList<>();
	public static ArrayList<Question> filteredQuestions = new ArrayList<>();
	public static ArrayList<Question> revisionQuestions = new ArrayList<>();

    public static String UserUid = "";
	 static String ClassName = "class-2";
    public static final String SCHOOL_NAME = "CBSE";

    public static String forLogD = "";
    public static String removedQuestionNumbers = "";
    public static AdData adData;
    public static String UserNamePrefix = "cbse2";

    /// Firebase related
	static String DefaultDatabaseLocation = "";
    static String FirebaseRoot = "";
    static String Firebase_DatabaseLocationSetting = "settings/database_location";
    static String DatabaseLocation = DefaultDatabaseLocation;
    static String TestReportRoot = "testReport/cbse/" + ClassName;
    static String UserRoot = "users/" + SCHOOL_NAME.toLowerCase() + "/" + ClassName;
    static String SubjectRoot = "schools/" + SCHOOL_NAME.toLowerCase() + "/" + ClassName;
    public static String SyllabusAndGrade = "cbse-2";
    static boolean isFullPageAdDisplayed = false;
    static String QuestionSetRoot = "v24/";
    static int SUBJECT_INDEX = 0;
    static int SET_INDEX = 1;

    //public static String AdDataRoot = SubjectRoot + "/extras/ads/activeAd";

    public static final String PACKAGE_NAME = "com.myAgeEducation.cbseClass2";
    public static final String GRADE = "2";
    public static String Subject = "";
    public static boolean isFreeApp = true;
    public static boolean isReleaseVersion = true;
    public static final String AdMobInterstitialAdUnitId = "ca-app-pub-4837855590190532/1249633007"; //cbse class-2
    public static final String AdMobInterstitialAdUnitDummyId = "ca-app-pub-3940256099942544/1033173712";
    public static final String ADMOB_APP_ID = "ca-app-pub-4837855590190532~7296166604"; // class-2

    public static final String PlayStoreLink = "https://play.google.com/store/apps/details?id=com.myAgeEducation.cbseClass" + GRADE;
    public static final String ShareLinkTitle = "Link for " + SCHOOL_NAME + "-" + GRADE + " app download";

    /*
    public static int getRandomQuestionNumber()
    {
        Random random = new Random();
        int generatedRandomNumber;
        generatedRandomNumber = random.nextInt(Util.questionNumbers.size());
        Log.d("QuestionNumbersSize", String.valueOf(Util.questionNumbers.size()));
        int questionNumber = ((Integer)(Util.questionNumbers.get(generatedRandomNumber))).intValue();
        Util.questionNumbers.remove(generatedRandomNumber);
        return questionNumber;
    }*/

    public enum BackgroundTheme {
        WHITE,
        LIGHT_BLUE,
        LIGHT_GREEN,
        LIGHT_YELLOW,
        LIGHT_PINK,
        LIGHT_PURPLE,
        LIGHT_ORANGE,
        LIGHT_CYAN,
        CREAM,
        RANDOM
    }

    static final String QUESTION_DATABASE_VERSION_NODE_PATH = "questionDatabaseVersionNew/cbse/" + Util.Subject +"/cbseClass" + Util.GRADE;

    static final int FIRST_SET = 21;
    static final int LAST_SET = 29;

    static int getRandomQuestionSet() {
        return ThreadLocalRandom.current().nextInt(FIRST_SET, LAST_SET + 1); // this will generate random number 21 to 29
    }

    public static String getCurrentDateTime()
    {
        Date date = new Date();
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
        return monthFormat.format(date);
    }

    public static String getCurrentTime()
    {
        Date date = new Date();
        SimpleDateFormat monthFormat = new SimpleDateFormat("hh:mm a");
        return monthFormat.format(date);
    }

    public static String getCurrentDate()
    {
        Date date = new Date();
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd-MM-yyyy");
        return monthFormat.format(date);
    }

    public static String generateEmailId(String id)
    {
        return SCHOOL_NAME.toLowerCase() + id + "@gmail.com";
    }
	
	public static void displayAlert(String message, String title, Context context)
    {
        if(!((Activity)context).isFinishing()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setMessage(message);
            alert.setTitle(title);
            alert.setPositiveButton("OK", null);
            alert.setCancelable(true);
            alert.create().show();
        }
    }

    public static Bitmap LoadBitmapFromBase64Encoding(String imageData)
    {
        imageData = imageData.replace("data:image/png;base64,","");
        byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
