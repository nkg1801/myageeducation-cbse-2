package com.myAgeEducation.cbseClass2;

import static com.myAgeEducation.cbseClass2.Util.QUESTION_DATABASE_VERSION_NODE_PATH;
import static com.myAgeEducation.cbseClass2.Util.QuestionSetRoot;
import static com.myAgeEducation.cbseClass2.Util.SET_INDEX;
import static com.myAgeEducation.cbseClass2.Util.SUBJECT_INDEX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.app.Activity;
import android.content.Intent;
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.myAgeEducation.cbsecommon.AdData;
import com.myAgeEducation.cbsecommon.Question;

public class SubjectList extends Activity
{
	int _randomQuestionSet;
    DataBaseHelper _databaseHelper;
    int _cloudVersion = 0;
    private ArrayList<String> _downloadLinks = new ArrayList<>();
    private ArrayList<String> _pendingDownloads = new ArrayList<>();
    private ArrayList<Question> _questionList = new ArrayList<>();

	private BaseAdapter _listAdapter;
	ListView _listView;
    ProgressDialog ringProgressDialog;// = new ProgressDialog(SubjectList.this);
    private SharedPreferences _sharedPreferences;
    private boolean isAddToLocalDatabaseCompleted = true;
    private Runnable runnable;
    private Handler handler = new Handler();
    private boolean runnableStarted = false;

    final int SCIENCE = 0;
    final int MATHS = 1;
    final int COMPUTERS = 2;
    final int GK = 3;
    final int ENGLISH = 4;
    final int MORALSCIENCE = 5;
    final int SCORE = 6;
    final int SHARE_APP_LINK = 7;
    final int APP_RATING = 8;
    final int GETMORE = 9;
    final int EXIT = 10;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subject_list);
        FirebaseApp.initializeApp(this);

        _listView = findViewById(android.R.id.list);
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ringProgressDialog = new ProgressDialog(SubjectList.this);

        try {
            // this is the first time the database is opened.
            openDatabase();
        }
        catch(Exception e)
        {
            Util.displayAlert("Error-SUB-001: " + e.getMessage(), "ERROR-SUB-001", SubjectList.this);
        }

        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position)
                {
                    case SCIENCE:
                        Util.Subject = "science";
                        break;

                    case MATHS:
                        Util.Subject = "maths";
                        break;

                    case COMPUTERS:
                        Util.Subject = "computers";
                        break;

                    case GK:
                        Util.Subject = "gk";
                        break;

                    case ENGLISH:
                        Util.Subject = "english";
                        break;

                    case MORALSCIENCE:
                        Util.Subject = "moralscience";
                        break;

                    case SCORE:
                        Util.Subject = "";
                        Intent subPage = new Intent();
                        subPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".ScoreHistory");
                        startActivity(subPage);
                        break;

                    /*case OFFLINE_VERSION:
                        Util.Subject = "";
                        openOfflineVersionActivity();
                        break;*/

                    case SHARE_APP_LINK:
                        Util.Subject = "";
                        shareAppLink();
                        //saveIfShareButtonClicked();
                        break;

                    case APP_RATING:
                        Util.Subject = "";
                        openPlayStoreForRating();
                        break;

                    case GETMORE:
                        Util.Subject = "";
                        Intent intentGetMore = new Intent(SubjectList.this, GetMore.class);
                        startActivity(intentGetMore);
                        break;

                    case EXIT:
                        Util.Subject = "";
                        finish();
                        break;

                    default:
                        break;
                }

                if (!Util.Subject.isEmpty())
                {
                    GetDatabaseLocation();
                }
            }
        });

        MobileAds.initialize(this); 

        populateAdapter();
        //MobileAds.initialize(this,Util.ADMOB_APP_ID);

        addBannerAd();
		findViewById(R.id.adView).setVisibility(View.VISIBLE);
		findViewById(R.id.openPlaystore).setVisibility(View.GONE);

        if(Util.AdDetail == null) {
            FirebaseManager.readAds(new FirebaseCallback() {
                @Override
                public void onCallback(String value) {

                }
            });
        }
	}

    @Override
    public void onStop()
    {
        if(runnableStarted) {
            handler.removeCallbacks(runnable);
        }
        super.onStop();
    }

    private boolean isLastAccessToday()
    {
        String lastAccessDate = _sharedPreferences.getString("LastAccessDate", "");
        String todaysDate = Util.getCurrentDate();

        if(lastAccessDate.compareToIgnoreCase(todaysDate) == 0)
        {
            Log.d("CBSE_", "last access was today");
            return true;
        }
        Log.d("CBSE_", "last access was not today");
        saveLastAccessDate(todaysDate);
        return false;
    }

    private int GetLocallySavedCloudVersion()
    {
        return _sharedPreferences.getInt("CloudVersion", 0);
    }

    private void saveLastAccessDate(String todaysDate)
    {
        SharedPreferences.Editor prefEdit = _sharedPreferences.edit();
        prefEdit.putString("LastAccessDate", todaysDate);
        prefEdit.apply();
    }

    private void saveLastCloudVersion(int cloudVersion)
    {
        SharedPreferences.Editor prefEdit = _sharedPreferences.edit();
        prefEdit.putInt("CloudVersion", cloudVersion);
        prefEdit.apply();
    }

	private void addBannerAd()
	{
		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				//.addTestDevice("750a657019b49e621c42ce9a20c2cc30")
				.build();
		mAdView.loadAd(adRequest);
	}

	private void openDatabase()
	{
		_databaseHelper = new DataBaseHelper(getApplicationContext());
		try
		{
			_databaseHelper.createDataBase();
		}
		catch(IOException e)
        {
            Log.d("CBSE_ERROR_OPENDATABASE", Objects.requireNonNull(e.getMessage()));
        }

		_databaseHelper.openDataBase();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.menu_settings) {
			openSettingsPage();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void openSettingsPage()
	{
		Intent subPage = new Intent();
		subPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".SettingsActivity");
		startActivity(subPage);
	}

	private void downloadAdData()
	{
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("extras/ads/activeAd");
		//Firebase ref = new Firebase(Util.SubjectRoot + "/extras/ads/activeAd");
		databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				long count = snapshot.getChildrenCount();
				Log.d("ImageDataCount", String.valueOf(count));
				for (DataSnapshot postSnapshot : snapshot.getChildren()) {
					Util.adData = postSnapshot.getValue(AdData.class);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError firebaseError) {
				Log.d("Exception: ", firebaseError.getMessage());
			}
		});
	}
	
	/*private void incrementTestTaken() {
		Firebase ref = new Firebase(Util.UserRoot);
		ref.child("000_timeOfLastTest").setValue(Util.getCurrentDateTime() + "/" + Util.Subject + "/" + Util.UserUid);
		final Firebase childRef = ref.child(Util.UserUid).child("numberOfTimesTestTaken").child(Util.Subject);

		childRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				int testTaken;
				if (snapshot != null && snapshot.getValue() != null) {
					try {
						testTaken = Integer.parseInt(snapshot.getValue().toString());
						childRef.setValue(testTaken + 1);
					}
					catch(NumberFormatException nfe)
					{
						//do nothing
					}
					catch (Exception e)
					{
						//do nothing
					}
				} else {
					testTaken = 0;
					childRef.setValue(testTaken + 1);
				}

				Firebase.goOffline();
			}

			@Override
			public void onCancelled(FirebaseError firebaseError) {
			}
		});
	}*/

	/*private void updateLastTestTaken()
	{
		Firebase ref = new Firebase(Util.UserRoot);
		final Firebase childRef = ref.child(Util.UserUid).child("lastTest");
		childRef.setValue(Util.Subject + "/" + Util.getCurrentDateTime());
	}*/

	/*private void updateAppVersion()
	{
		Firebase ref = new Firebase(Util.UserRoot);
		final Firebase childRef = ref.child(Util.UserUid).child("appVersion");
		childRef.setValue(BuildConfig.VERSION_CODE);
	}*/

	/*public void openStartMode()
	{
		Intent subPage = new Intent();
		subPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".StartMode");
		subPage.putStringArrayListExtra("wrongAnswer_list", wrongAnswer_list);
		subPage.putIntegerArrayListExtra("used_numbers", usedNumbers);
		subPage.putExtra("last_question_number", lastQuestionNumber);
		subPage.putExtra("last_score", lastScore);
		 
		startActivity(subPage);
	}*/
	
	public void openChapters(String questionSet)
	{
		Intent chapterIntent = new Intent();
		chapterIntent.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".Chapters");
		chapterIntent.putExtra("question_set", questionSet);
		startActivity(chapterIntent);
	}
	
	private int getRandomQuestionSet()
	{
		Random random = new Random();
		return random.nextInt(9) + 11;
	}

	public void onClickOpenPlayStore(View view)
	{
        openPlayStore();
	}

    private void shareAppLink()
    {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = Util.ShareLinkTitle + System.getProperty("line.separator") + Util.PlayStoreLink;
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Util.ShareLinkTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
        //setShareIntent(sharingIntent);
    }

    /*private void saveIfShareButtonClicked() {
        Firebase.goOnline();
        String ShareButtonClickedReportRoot = "https://schooltests.firebaseio.com/sharebuttonclicked";
        Firebase ref = new Firebase(ShareButtonClickedReportRoot);
        Firebase childRef = ref.child("000_lastSharedButtonClicked-" + Util.ClassName);
        childRef.setValue(Util.getCurrentDateTime());
        childRef = ref.child(UUID.randomUUID().toString());
        childRef.setValue(Util.ClassName + "/" + Util.getCurrentDateTime());
    }*/

    private void openPlayStore()
    {
        try {
            //saveIfAdClicked();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.myAgeEducation.cbseClass2Paid"));
            startActivity(intent);
        }
        catch(Exception e)
        {
            Util.displayAlert("Cannot open play store. Open play store manually and search for CBSE Class 2", "Error", SubjectList.this);
        }
    }

    private void openPlayStoreForRating()
    {
        try {
            //saveIfAdClicked();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.myAgeEducation.cbseClass2"));
            startActivity(intent);
        }
        catch(Exception e)
        {
            Util.displayAlert("Cannot open play store. Open play store manually and search for CBSE Class 2", "Error", SubjectList.this);
        }
    }

    private void openOfflineVersionActivity()
    {
        try {
            Intent subPage = new Intent();
            subPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".OfflineVersionActivity");
            startActivity(subPage);
        }
        catch(Exception e)
        {
            Util.displayAlert(e.getMessage(), "Error", SubjectList.this);
        }
    }

	/*private void saveIfAdClicked() {
		Firebase ref = new Firebase(Util.UserRoot);
		Firebase childRef = ref.child("000_lastAdClicked");
		childRef.setValue(Util.UserUid + "/" + Util.getCurrentDateTime());
		childRef = ref.child(Util.UserUid).child("isAdClicked");
		childRef.setValue("Yes/" + Util.getCurrentDateTime());
	}*/

    private void DownloadQuestionsOnlyIfAllowed()
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("questionDatabaseVersionNew/cbse/settings/disableDownload");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int temp = snapshot.getValue(Integer.class);
                int isDownloadDisabled = 0;
                try
                {
                    isDownloadDisabled = temp;
                    Log.d("CBSE_isDownloadDisabled", String.valueOf(isDownloadDisabled));
                }

                catch(Exception e)
                {
                    Log.d("CBSE_Exception", Objects.requireNonNull(e.getMessage()));
                }

                if(isDownloadDisabled == 1) // download is disabled
                {
                    ArrayList<Integer> downloadedSets = _databaseHelper.getDownloadedQuestionSets(Util.Subject);
                    int downloadedSetsSize = downloadedSets.size();

                    // :-( no sets available, must download
                    if(downloadedSetsSize == 0)
                    {
                        downloadQuestions(_downloadLinks.get(0));
                    }
                    else // some downloads are available, will use the downloaded sets
                    {
                        Log.d("CBSE_downloaddisabled", "Download disabled, using local database, size is:" + String.valueOf(downloadedSetsSize));
                        Random random = new Random();
                        _randomQuestionSet = random.nextInt(downloadedSets.size());
                        _randomQuestionSet = downloadedSets.get(_randomQuestionSet);

                        runnable = () -> {
                            runnableStarted = true;
                            readQuestionsFromLocalDatabase();
                            runnableStarted = false;
                        };
                        new Thread(runnable).start();
                    }
                }
                else
                {
                    downloadQuestions(_downloadLinks.get(0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError firebaseError) {
                //ringProgressDialog.dismiss();
                Log.d("Exception: ", firebaseError.getMessage());
                //displayAlert("Unable to connect", "Unable to connect to the server. Make sure you are connected to the internet and try again");
            }
        });
    }

	private void GetCloudQuestionDatabaseVersion()
	{
        showProgressDialog("Checking database for newer version...");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(QUESTION_DATABASE_VERSION_NODE_PATH);
		//Firebase ref = new Firebase("schools/question_database_version/cbse/" + Util.Subject +"/cbseClass" + Util.GRADE);
		databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Integer version = snapshot.getValue(Integer.class);

                if (version == null) {
                    Log.d("myVersion", "Version is null");

                    if (ringProgressDialog != null && ringProgressDialog.isShowing()) {
                        ringProgressDialog.dismiss();
                    }

                   version = 1;
                }


                if(ringProgressDialog!=null && ringProgressDialog.isShowing()) {
                    ringProgressDialog.dismiss();
                }
				Log.d("myVersion", String.valueOf(version));
				
				//todo, use TextUtils.isEmpty(version) here
				
				try
				{
                    _cloudVersion = version;
                    saveLastCloudVersion(_cloudVersion);
					Log.d("CloudVersion", String.valueOf(_cloudVersion));
				}

				catch(Exception e)
				{
					Log.d("CloudVersionException", Objects.requireNonNull(e.getMessage()));
		    	}

                try {
                    int localVersion = _databaseHelper.getLocalQuestionDatabaseVersion(Util.Subject);
                    Log.d("CBSE_LocalVersion", String.valueOf(localVersion));

                    if (_cloudVersion > localVersion) {
                        Log.d("CBSE_CloudVersion", String.valueOf(_cloudVersion));
                        Log.d("CBSE_CloudVersionInfo", "Cloud Version is greater than local version");
                        _databaseHelper.resetDownloadStatus(Util.Subject);
                    }
                }
                catch(Exception e)
                {
                    Util.displayAlert("ERROR-121: " + e.getMessage(), "ERROR-121", SubjectList.this);
                }

                getQuestions();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError firebaseError) {
                dismissProgressDialog();
				Log.d("CBSE_Exception: ", firebaseError.getMessage());
                Util.displayAlert("Unable to connect to the server. Make sure you are connected to the internet and try again","Unable to connect", SubjectList.this);
            }
		});
	}

    private void getQuestions()
    {
        _randomQuestionSet = Util.getRandomQuestionSet();
        _pendingDownloads = _databaseHelper.pendingDownloads(Util.Subject, _randomQuestionSet);
        Log.d("CBSE_PendingDownloads", String.valueOf(_pendingDownloads.size()));

        if(!_pendingDownloads.isEmpty())
        {
            addDownloadLinksToDownload();
            DownloadQuestionsOnlyIfAllowed();
        }
        else {
            try {
                runnable = () -> {
                    runnableStarted = true;
                    readQuestionsFromLocalDatabase();
                    runnableStarted = false;
                };
                new Thread(runnable).start();
            }

            catch(Exception e)
            {
                Util.displayAlert("reading questions from local database failed. " + e.getMessage(), "Error", SubjectList.this);
            }
        }
    }

	public void readQuestionsFromLocalDatabase()
	{
        //showProgressDialog("Reading from local database");
		String tableName = Util.SCHOOL_NAME + "_" + Util.Subject;
		if(Util.allQuestions != null) {
            Util.allQuestions.clear();
        }
		Util.allQuestions = _databaseHelper.getAllQuestions(tableName.toUpperCase(), _randomQuestionSet);
        if(Util.allQuestions == null)
        {
            //Something went wrong, the database has returned null. will use the questions from the cloud .. hope it does not rain
            String downloadLink = Util.Subject + "/set" + _randomQuestionSet;
            downloadQuestions(downloadLink);
            return;
        }
		Log.d("CBSE_QuestionSet", String.valueOf(_randomQuestionSet));
		Log.d("CBSE_QuestionCount", String.valueOf(Util.allQuestions.size()));

		if(!Util.allQuestions.isEmpty()) {
			{
				openChapters("set" + String.valueOf(_randomQuestionSet));
			}
		}
	}

	private void GetDatabaseLocation()
    {
        GetCloudQuestionDatabaseVersion();
        downloadAdData();

        /*showProgressDialog("Connecting to the online database..., make sure you are connected to the net");
        Firebase.goOnline();

        downloadAdData();

        Firebase ref = new Firebase(Util.Firebase_DatabaseLocationSetting);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String database_location = snapshot.getValue(String.class);
                if(database_location.trim().isEmpty())
                {
                    database_location = Util.DefaultDatabaseLocation;
                }

                AssignFirebaseLocations(database_location);

                dismissProgressDialog();

                Log.d("CBSE_DatabaseLocation", Util.DatabaseLocation);

                //if(!isLastAccessToday())
                {
                    GetCloudQuestionDatabaseVersion();
                }
                //else
                {
                    //_cloudVersion = GetLocallySavedCloudVersion();
                    //getQuestions();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

                dismissProgressDialog();
                Log.d("CBSE_Exception: ", firebaseError.getMessage());
                //displayAlert("Unable to connect", "Unable to connect to the server. Make sure you are connected to the internet and try again");
                Util.displayAlert("Unable to connect to the server. Make sure you are connected to the internet and try again", "Unable to connect", SubjectList.this);
            }
        });*/
    }

	/*private void AssignFirebaseLocations(String databaseLocation)
	{
		Util.DatabaseLocation = databaseLocation;
		Util.TestReportRoot = databaseLocation + "/testReport/cbse/" + Util.ClassName;
		Util.SubjectRoot = databaseLocation + "/schools/" + Util.SCHOOL_NAME.toLowerCase() + "/" + Util.ClassName;
	}*/

    private void addQuestionToLocalDatabase(String downloadLink) {
        isAddToLocalDatabaseCompleted = false;
        String[] tokens = downloadLink.split("/");
        assert tokens.length > 1;
        String subject = tokens[SUBJECT_INDEX];
        String set = tokens[SET_INDEX]; /// will be of the form SetNN (NN = 11 to NN = 19)
        int setNumber = Integer.parseInt(set.substring(set.length() - 2)); // get the last 2 chars

        ArrayList<Question> questions = (ArrayList<Question>)_questionList.clone();
        _questionList.clear();

        if (_databaseHelper.addQuestions("CBSE_" + subject, questions, setNumber)) {
            _databaseHelper.updateDownloadStatus(subject, setNumber);
            if(_cloudVersion > 0) {
                _databaseHelper.updateLocalQuestionDatabaseVersionInfo(subject, _cloudVersion);
            }
        }

        isAddToLocalDatabaseCompleted = true;
    }

    private void showProgressDialog(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ringProgressDialog.setTitle("Please wait ...");
                    ringProgressDialog.setMessage(message);
                    ringProgressDialog.setCancelable(false);
                    ringProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    if (ringProgressDialog != null && (!ringProgressDialog.isShowing())) {
                        ringProgressDialog.show();
                        ringProgressDialog.setCancelable(true);
                    }
                }
                catch (Exception e)
                {
                    //displayAlert(e.getMessage(), "Progress Dialog stopped");
                }
            }
        });
    }

    private void dismissProgressDialog()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(ringProgressDialog!=null && ringProgressDialog.isShowing()) {
                    ringProgressDialog.dismiss();
                }
            }
        });
    }

    public void downloadQuestions(final String downloadLink)
    {
        showProgressDialog("Connecting to online question database...");
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(QuestionSetRoot + downloadLink);

        //Firebase ref = new Firebase(downloadLink);
        Query queryRef = databaseReference.orderByChild("chapter");
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        try {
                            Question question = postSnapshot.getValue(Question.class);
                            _questionList.add(question);
                        } catch (Exception e) {
                            Log.d("CBSE_ERROR", Objects.requireNonNull(e.getMessage()));
                        }
                    }

                    Log.d("CBSE_", String.valueOf(_questionList.size()) + " were downloaded");

                    if (!_questionList.isEmpty()) {
                        Util.allQuestions = (ArrayList<Question>) _questionList.clone();
                        if (isAddToLocalDatabaseCompleted)  // if the previous addition is completed, then only we add this, otherwise just ignore adding this set
                        {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    addQuestionToLocalDatabase(downloadLink);
                                }
                            };
                            new Thread(runnable).start();
                        }
                        else {
                            _questionList.clear();
                        }
                    } else {
                        if(Util.allQuestions != null) {
                            Util.allQuestions.clear();
                        }
                        dismissProgressDialog();
                        Util.displayAlert("No questions available for this subject", "Questions not available", SubjectList.this);
                        return;
                    }

                    dismissProgressDialog();

                    openChapters("set" + String.valueOf(_randomQuestionSet));
                }
                catch(Exception e)
                {
                    Util.displayAlert(e.getMessage(), "ERROR_SUB_589", SubjectList.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError firebaseError) {
                Log.d("Exception: ", firebaseError.getMessage());
            }
        });
    }

    private void addDownloadLinksToDownload()
    {
        _downloadLinks.clear();

        for(int i = 0; i < _pendingDownloads.size(); i++)
        {
            _downloadLinks.add(_pendingDownloads.get(i));
            Log.d("CBSE_PENDING_DOWNLOADS", _pendingDownloads.get(i));
        }
    }

	private void populateAdapter() {
		ArrayList<Integer> subjectImage = new ArrayList<>();
		ArrayList<String> subjectName = new ArrayList<>();

		subjectImage.add(R.drawable.science);
		subjectImage.add(R.drawable.maths);
		subjectImage.add(R.drawable.computers);
		subjectImage.add(R.drawable.gk);
		subjectImage.add(R.drawable.books_english);
		subjectImage.add(R.drawable.books_moralscience);
		subjectImage.add(R.drawable.score);
        subjectImage.add(R.drawable.share);
        subjectImage.add(R.drawable.rating);
        subjectImage.add(R.drawable.getmore);
        subjectImage.add(R.drawable.exit);

		subjectName.add("Science");
		subjectName.add("Maths");
		subjectName.add("Computers");
		subjectName.add("GK");
        subjectName.add("English");
        subjectName.add("Moral Science");
		subjectName.add("Score");
        subjectName.add("Share App Link");
        subjectName.add("Rate this app");
        subjectName.add("Get More");
        subjectName.add("Exit");

		_listAdapter = new ListViewAdapterForSubjectList(SubjectList.this, subjectImage, subjectName);
		_listView.setAdapter(_listAdapter);
	}
}
