package com.myAgeEducation.cbseClass2;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.graphics.Color;
import com.firebase.client.Firebase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import androidx.annotation.NonNull;

import com.myAgeEducation.cbseClass2.maths.calendar.CalendarImageGenerator;
import com.myAgeEducation.cbseClass2.maths.calendar.CalendarQuestionGenerator;
import com.myAgeEducation.cbseClass2.maths.clock.ClockImageGenerator;
import com.myAgeEducation.cbseClass2.maths.clock.ClockQuestionGenerator;
import com.myAgeEducation.cbseClass2.maths.clock.ClockTime;
import com.myAgeEducation.cbseClass2.maths.clock.TimeGenerator;
import com.myAgeEducation.cbsecommon.Question;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class QuestionPage extends Activity
{
	private RadioButton radioSelectedButton;
  	private Button buttonNext;
    private TableLayout tableLayout1;
  	private int _currentQuestionNumber;
	private Question _question;
	public static ArrayList<Question> QuestionList;
	private ArrayList<Question> revisionQuestions;
  	private String answer;
  	private int correctAnswerCount;
  	private int questionCount;
  	public int seconds = 0;
  	public int minutes = 0;
  	public int hours = 0;
  	private String isRevision;
  	private ArrayList<Integer> _usedNumbers = new ArrayList<Integer>();
	public ArrayList _questionNumbers = new ArrayList();
  	private SharedPreferences sharedPrefs;
  	private String reward = "";
  	private boolean _automaticallyMoveToNextQuestion = false;
	private boolean _isRandomQuestions = false;
  	private String rewardPoints = "";
	private InterstitialAd mInterstitialAd;
	private boolean _isRecoverMode;
	private String isExit;
	private Bundle _bundle;
	private String _questionSet;
	private int _questionIndex;
	private String _linkText;
	private static final Random RANDOM = new Random();

	@Override
  	public void onCreate(Bundle savedInstanceState)
  	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questionpage);
		_bundle = getIntent().getExtras();
		readBundle();
		if(!storeQuestionNumbers())
		{
			finish();
			return;
		}

		addInterstitialAd();
		addBannerAd();

		setTextViewProperties();
	
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    	_currentQuestionNumber = 1;
		correctAnswerCount = 0;

		revisionQuestions = new ArrayList<>();

		displayInitialScore();

		if(isExit.equalsIgnoreCase("true"))
		{
			finish();
		}
	
		displayScore();

		buttonNext = findViewById(R.id.buttonNext);
        tableLayout1 = findViewById(R.id.tableLayout1);

    	setQuestionTextColor();

		buttonNext.setEnabled(false);
		HideButtonNext();

		if(sharedPrefs.getBoolean("random_questions", true))
		{
			_isRandomQuestions = true;
		}

		if(isRevision.equals("true"))
		{
			QuestionList = (ArrayList<Question>)Util.revisionQuestions.clone();
			_isRandomQuestions = false;
		}

		if(_isRandomQuestions)
		{
			try {
				_questionIndex = getRandomQuestionNumber();
				_usedNumbers.add(_questionIndex);
			}
			catch(Exception e)
			{
                Util.displayAlert("Questions could not be retrieved, please try again", "Error", QuestionPage.this);
				finish();
			}
		}
		else
		{
			_questionIndex = 0;
		}

		_question = QuestionList.get(_questionIndex);
		setControlTexts(_question);
    
    	if(questionCount == 1)
    	{
            buttonNext.setText("Submit");
    	}

		addTimer();

      	if(sharedPrefs.getBoolean("move_to_next_question", false))
		{
			_automaticallyMoveToNextQuestion = true;
			tableLayout1.setVisibility(View.INVISIBLE);
		}

		setActivityTitle();
		addButtonListener();
		addRadioButtonListener();
		setRandomBackgroundForOptions();
  	}

	private boolean storeQuestionNumbers()
	{
		Util.forLogD = "";
		_questionNumbers.clear();
		if(QuestionList == null)
		{
			return false;
		}
		for(int i = 0; i < QuestionList.size(); i++)
		{
			_questionNumbers.add(i);
		}
		return true;
	}

	private void displayScore()
	{
		TextView tvScore = findViewById(R.id.textViewScore);
		if(!sharedPrefs.getBoolean("score_with_every_question", true))
		{
			tvScore.setVisibility(View.GONE);
		}
	}

	private void setTextViewProperties()
	{
		TextView tv = findViewById(R.id.textViewQuestionNumber);
		tv.setTextColor(Color.WHITE);
		tv.setBackgroundColor(Color.DKGRAY);

		tv = findViewById(R.id.textViewTimer);
		tv.setTextColor(Color.WHITE);
		tv.setBackgroundColor(Color.DKGRAY);

		TextView tvScore = findViewById(R.id.textViewScore);
		tvScore.setTextColor(Color.WHITE);
		tvScore.setBackgroundColor(Color.DKGRAY);

		tv = findViewById(R.id.textViewSubject);
		tv.setTextColor(Color.WHITE);
		tv.setBackgroundColor(Color.DKGRAY);
	}

	private void setQuestionTextColor()
	{
		TextView textView = findViewById(R.id.textViewQuestion);

		if(isRevision.equals("true"))
		{
			textView.setTextColor(Color.RED);
		}
		else
		{
			textView.setTextColor(Color.BLUE);
		}
	}

	private void setActivityTitle()
	{
		String title = getTitle().toString();
		setTitle(title + " - " + Util.Subject);

		if(isRevision.equals("true"))
		{
			title = getTitle().toString();
			setTitle(title + " (Revision)");
		}

		String subject;
		if(Util.Subject.equalsIgnoreCase("cs"))
		{
			subject = "Computers";
		}
		else if(Util.Subject.equalsIgnoreCase("evs"))
		{
			subject = "Science";
		}
		else
		{
			subject = Util.Subject;
		}

		((TextView) findViewById(R.id.textViewSubject)).setText("Subject: " + subject);

		/*if(Util.Android_id.equalsIgnoreCase("6d692d322d2df2fb") || Util.Android_id.equalsIgnoreCase("e64b49e28d3e849c")) {
			displayQuestionSetAndQuestionNumber();
		}
		else
		{
			((TextView) findViewById(R.id.textViewSubject)).setText("Subject: " + subject);
		}*/
	}

	private void displayInitialScore()
	{
		TextView tvScore = findViewById(R.id.textViewScore);
		if(_isRecoverMode)
		{
			int lastScore = _bundle.getInt("last_score");
			tvScore.setText("Score: " + String.valueOf(lastScore) + "/" + String.valueOf(questionCount));
		}
		else
		{
			tvScore.setText("Score: 0/" + questionCount);
		}
	}

	private void readBundle()
	{
		Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        questionCount = bundle.getInt("questionCount");
		isRevision = bundle.getString("isRevision");
		isExit = bundle.getString("isExit");
		reward = bundle.getString("reward");
		rewardPoints = bundle.getString("points");
		_isRecoverMode = bundle.getBoolean("recover_mode");
		_questionSet = bundle.getString("question_set");
	}

    private void HideTimerText()
    {
        TextView textViewTimer = findViewById(R.id.textViewTimer);
        textViewTimer.setVisibility(View.GONE);
    }

	private void addBannerAd()
	{
		AdView mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.build();
		mAdView.loadAd(adRequest);
	}

	private void addInterstitialAd()
	{
		AdRequest adRequest = new AdRequest.Builder().build();

		String adUnitId;
		if(Util.isReleaseVersion)
		{
			adUnitId = Util.AdMobInterstitialAdUnitId;
		}
		else
		{
			adUnitId = Util.AdMobInterstitialAdUnitDummyId;
		}

		InterstitialAd.load(this, adUnitId, adRequest,
				new InterstitialAdLoadCallback() {
					@Override
					public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
						mInterstitialAd = interstitialAd;
						mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
							@Override
							public void onAdDismissedFullScreenContent() {
								mInterstitialAd = null;
								Util.isFullPageAdDisplayed = false;
							}

							@Override
							public void onAdShowedFullScreenContent() {
								Util.isFullPageAdDisplayed = true;
							}
						});
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						mInterstitialAd = null;
					}
				});
	}

	private void showInterstitialAdAd()
	{
		if (mInterstitialAd != null)
		{
			mInterstitialAd.show(this);
		}
	}

  public void displayAlertBox(String message)
	{
		if(!QuestionPage.this.isFinishing()) {
			Util.displayAlert(message, "Test Report", QuestionPage.this);
		}
	}

    public void displayAlertWithOkCancel(String message, String title, Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage(message);
        alert.setTitle(title);
        alert.setPositiveButton("Yes", null);
        alert.setCancelable(true);

        alert.setPositiveButton("Yes",new DialogInterface.OnClickListener()
        {
            public void onClick (DialogInterface dialog, int which){
                clearState(); // Test is completed.. so remove the saved state
                openTestReportActivity();
                finish();
                showInterstitialAdAd();
            }
        });

        alert.setNegativeButton("No",null);
        alert.create().show();
    }

  public void addTimer()
  {
	  Timer timer = new Timer();
	  timer.schedule(new TimerTask() {
		  @Override
		  public void run() {
			  runOnUiThread(new Runnable() {

				  @Override
				  public void run() {
					  String elapsedTime;
					  TextView textView = (TextView) findViewById(R.id.textViewTimer);
					  elapsedTime = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + " ";
					  textView.setText(elapsedTime);

					  seconds += 1;

					  if (seconds == 60) {
						  elapsedTime = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + " ";
						  textView.setText(elapsedTime);

						  seconds = 0;
						  minutes = minutes + 1;
					  }

					  if (minutes == 60) {
						  elapsedTime = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + " ";
						  textView.setText(elapsedTime);
						  minutes = 0;
						  hours = hours + 1;
					  }
				  }
			  });
		  }
	  }, 0, 1000);
  }

	private Bitmap loadBitmapFromBase64Encoding(String imageData)
	{
		imageData = imageData.replace("data:image/png;base64,",""); // introduced in Release 1.19
		byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
	}

	private void showAllOptions()
	{
		findViewById(R.id.radio_option1).setVisibility(View.VISIBLE);
		findViewById(R.id.radio_option2).setVisibility(View.VISIBLE);
		findViewById(R.id.radio_option3).setVisibility(View.VISIBLE);
		findViewById(R.id.radio_option4).setVisibility(View.VISIBLE);
	}

	private void setRandomBackgroundForOptions()
	{
		int[] backgrounds = {
				R.drawable.bg_gradient_blue_purple_pill,
				R.drawable.bg_gradient_gold_orange_pill,
				R.drawable.bg_gradient_red_pill,
				R.drawable.bg_gradient_teal_pill,
		};

		int index = new Random().nextInt(backgrounds.length);
		findViewById(R.id.radio_option1).setBackgroundResource(backgrounds[index]);
		findViewById(R.id.radio_option2).setBackgroundResource(backgrounds[index]);
		findViewById(R.id.radio_option3).setBackgroundResource(backgrounds[index]);
		findViewById(R.id.radio_option4).setBackgroundResource(backgrounds[index]);
	}

	private void setImageForQuestion(String imageData)
	{
		ImageView img = findViewById(R.id.imageView1);
		img.setVisibility(View.INVISIBLE);

		if(imageData == null)
		{
			return;
		}

		if(imageData.contains("use_clock_generator_code"))
		{
			//if the question JSON contains something like this: use_clock_generator_code;2_40
			String[] parts = imageData.split(";");
			int hours = Integer.parseInt(parts[1].split("_")[0]);
			int minute = Integer.parseInt(parts[1].split("_")[1]);
			img.setImageBitmap(ClockImageGenerator.generateClock(hours, minute, 800, Util.BackgroundTheme.RANDOM));
			img.setVisibility(View.VISIBLE);

			ViewGroup.LayoutParams params = img.getLayoutParams();
			params.height = 800; // height in pixels
			img.setLayoutParams(params);
		}

		else if(imageData.contains("use_calendar_generator_code"))
		{
			String[] parts = imageData.split(";");
			int month = Integer.parseInt(parts[1].split("_")[0]);
			int year = Integer.parseInt(parts[1].split("_")[1]);
			Bitmap bitmap = CalendarImageGenerator.generateCalendar(year,month - 1, 1200);
			img.setImageBitmap(bitmap);
			img.setVisibility(View.VISIBLE);
		}

		else if(imageData.length() < 20) {
			//if there is file in the app asset
			int resourceIdentifier = getResources().getIdentifier(imageData, "drawable", getPackageName());
			if(resourceIdentifier != 0)
			{
				img.setImageResource(resourceIdentifier);
				img.setVisibility(View.VISIBLE);
			}
		}
		else {
			// if the JSON contains base64 code
			img.setImageBitmap(loadBitmapFromBase64Encoding(imageData));
			img.setVisibility(View.VISIBLE);
		}
	}

	private void setSupportiveText(String supportiveText)
	{
		findViewById(R.id.textViewSupportiveText).setVisibility(View.INVISIBLE);

		if(supportiveText == null)
		{
			return;
		}
		((TextView)findViewById(R.id.textViewSupportiveText)).setText(supportiveText);
		findViewById(R.id.textViewSupportiveText).setVisibility(View.VISIBLE);
	}

	private void setOptions(Question question)
	{
		String option1 = question.getOption1();
		String option2 = question.getOption2();
		String option3 = question.getOption3();
		String option4 = question.getOption4();

		// if 3rd and 4th option is empty, we do not shuffle the options, so setting the first option as first
		if(option3 == null && option4 == null)
		{
			((RadioButton)findViewById(R.id.radio_option1)).setText(option1);
			((RadioButton)findViewById(R.id.radio_option2)).setText(option2);

			findViewById(R.id.radio_option3).setVisibility(View.GONE);
			findViewById(R.id.radio_option4).setVisibility(View.GONE);
		}
		else if(option4 == null)
		{
			((RadioButton)findViewById(R.id.radio_option1)).setText(option1);
			((RadioButton)findViewById(R.id.radio_option2)).setText(option2);
			((RadioButton)findViewById(R.id.radio_option3)).setText(option3);
			findViewById(R.id.radio_option4).setVisibility(View.GONE);
		}
		else
		{
			Random r = new Random();
			int random = r.nextInt(4) + 1; //Generate a random no. from 1 to 4 to shuffle the options

			switch(random)
			{
				case 1:
					((RadioButton)findViewById(R.id.radio_option1)).setText(option1);
					((RadioButton)findViewById(R.id.radio_option2)).setText(option2);
					((RadioButton)findViewById(R.id.radio_option3)).setText(option3);
					((RadioButton)findViewById(R.id.radio_option4)).setText(option4);
					break;

				case 2:
					((RadioButton)findViewById(R.id.radio_option1)).setText(option2);
					((RadioButton)findViewById(R.id.radio_option2)).setText(option3);
					((RadioButton)findViewById(R.id.radio_option3)).setText(option4);
					((RadioButton)findViewById(R.id.radio_option4)).setText(option1);
					break;

				case 3:
					((RadioButton)findViewById(R.id.radio_option1)).setText(option3);
					((RadioButton)findViewById(R.id.radio_option2)).setText(option4);
					((RadioButton)findViewById(R.id.radio_option3)).setText(option1);
					((RadioButton)findViewById(R.id.radio_option4)).setText(option2);
					break;

				case 4:
					((RadioButton)findViewById(R.id.radio_option1)).setText(option4);
					((RadioButton)findViewById(R.id.radio_option2)).setText(option1);
					((RadioButton)findViewById(R.id.radio_option3)).setText(option2);
					((RadioButton)findViewById(R.id.radio_option4)).setText(option3);
					break;

				default:
					break;
			}
		}
	}
  
  public void setControlTexts(Question question)
  {
	  showAllOptions();
	  _linkText = "";

	  if(question.getQuestion().contains("generate_this_question_calendar_type"))
	  {
		  question = CalendarQuestionGenerator.generateQuestion();
	  }

	  else if(question.getImage() != null) {
		  if (question.getImage().contains("use_clock_generator_code")) {
			  question = ClockQuestionGenerator.generateQuestion();
		  }
	  }

	  String questionImage = question.getImage();
	  String supportiveText = question.getSupportiveText();

	  setImageForQuestion(questionImage);
	  setSupportiveText(supportiveText);

	  if(TextUtils.isEmpty(questionImage) && TextUtils.isEmpty(supportiveText))
	  {
		  if(_currentQuestionNumber < 5)
		  {
			  displayAdImage();
		  }
	  }

	  TextView textViewQNum;

	  textViewQNum = findViewById(R.id.textViewQuestionNumber);
	  String myString = " Question " + _currentQuestionNumber + " of " + questionCount;
	  textViewQNum.setText(myString);
	    
	    // Set the Question
	  TextView textView;
	  textView = findViewById(R.id.textViewQuestion);
	  String questionText = question.getQuestion();

	  textView.setText(questionText);
	  setOptions(question);
	    
	    answer = question.getAnswer();
  }
	private void displayAdImage()
	{
		if(Util.adData == null)
		{
			return;
		}

		if(Util.adData.getImage() == null)
		{
			return;
		}

		ImageView img = findViewById(R.id.imageView1);
		if (!Util.adData.getImage().isEmpty()) {
			img.setImageBitmap(loadBitmapFromBase64Encoding(Util.adData.getImage()));
			img.setVisibility(View.VISIBLE);
			_linkText = Util.adData.getLinkText();
		}
	}
  
  public void SetAnswerFeedback()
  {
	  String feedback = "";
	  boolean isAnsCorrect = radioSelectedButton.getText().equals(answer);
	  
	  if(isAnsCorrect)
	  {
		  correctAnswerCount++;
		  feedback = "Correct Answer";
		  findViewById(R.id.imageViewRight).setVisibility(View.VISIBLE);
	  }
	  else
	  {
		  feedback = "Wrong Answer";
		  revisionQuestions.add(_question);
		  findViewById(R.id.imageViewWrong).setVisibility(View.VISIBLE);
	  }

      TextView tv = (TextView)findViewById(R.id.textViewScore);
	  tv.setText("Score: " + String.valueOf(correctAnswerCount) + "/" + String.valueOf(questionCount));
	  
	  if(sharedPrefs.getBoolean("display_answer_feedback", true) && isAnsCorrect)
	  {
		  /*Toast toast= Toast.makeText(getApplicationContext(),
				  feedback, Toast.LENGTH_SHORT);  
				  toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
				  toast.show();*/
	  }
	  
	  if(sharedPrefs.getBoolean("display_correct_ans", false) && !isAnsCorrect)
	  {
		  /*String text = feedback + "\nThe correct Answer is: " + answer;
		  Toast.makeText(QuestionPage.this, text, Toast.LENGTH_SHORT).show();*/
	  }
  }
  
  public void EnableAnswers(boolean val)
  {
	  RadioButton button = findViewById(R.id.radio_option1);
      button.setEnabled(val);

      button = findViewById(R.id.radio_option2);
      button.setEnabled(val);

      button = findViewById(R.id.radio_option3);
      button.setEnabled(val);

      button = findViewById(R.id.radio_option4);
      button.setEnabled(val);

	  findViewById(R.id.imageViewRight).setVisibility(View.INVISIBLE);
	  findViewById(R.id.imageViewWrong).setVisibility(View.INVISIBLE);
  }

    private void ShowSubmitButton()
    {
        //ImageButton buttonSubmit = (ImageButton) findViewById(R.id.buttonSubmit);
        //buttonSubmit.setVisibility(View.VISIBLE);

		Button buttonSubmit = (Button) findViewById(R.id.buttonNext);
		buttonSubmit.setText("Submit");
    }

  private void WhenAnswerSelected()
  {
      try {
          EnableAnswers(false);
          SetAnswerFeedback();
          buttonNext.setEnabled(true);
		  buttonNext.setVisibility(View.VISIBLE);
		  tableLayout1.setVisibility(View.VISIBLE);

          if (_currentQuestionNumber == questionCount)
		  {
              ShowSubmitButton();
          }
		  else
		  {
              if (_automaticallyMoveToNextQuestion)
			  {
                  moveToNextQuestion();
                  EnableAnswers(true);
              }
          }
      }
      catch(Exception e)
      {
          displayAlertBox(e.getMessage());
      }
  }

  public void addRadioButtonListener(){
	  RadioButton b = findViewById(R.id.radio_option1);
	  b.setOnClickListener(v -> {
          RadioButton b1 = findViewById(R.id.radio_option1);
          if(b1.isChecked())
          {
radioSelectedButton = findViewById(R.id.radio_option1);
WhenAnswerSelected();
          }
      });
	  
	  b = findViewById(R.id.radio_option2);
	  b.setOnClickListener(v -> {

          RadioButton b2 = findViewById(R.id.radio_option2);
          if(b2.isChecked())
          {
              radioSelectedButton = findViewById(R.id.radio_option2);
              WhenAnswerSelected();
          }
      });
	  
	  b = findViewById(R.id.radio_option3);
	  b.setOnClickListener(v -> {
          RadioButton b3 = findViewById(R.id.radio_option3);
          if(b3.isChecked())
          {
              radioSelectedButton = findViewById(R.id.radio_option3);
WhenAnswerSelected();
          }
      });
	  
	  b = findViewById(R.id.radio_option4);
	  b.setOnClickListener(v -> {
          RadioButton b4 = findViewById(R.id.radio_option4);
          if (b4.isChecked()) {
              radioSelectedButton = findViewById(R.id.radio_option4);
              WhenAnswerSelected();
          }
      });
  }

    private void HideButtonNext()
	{
        tableLayout1.setVisibility(View.GONE);
	}

  public void moveToNextQuestion()
  {
	  HideButtonNext();

	  try
	  {
          if (_currentQuestionNumber <= questionCount)
		  {
              if (_isRandomQuestions)
			  {
				  try {
					  _questionIndex = getRandomQuestionNumber();
					  _usedNumbers.add(_questionIndex);
				  }
				  catch(Exception e)
				  {
                      Util.displayAlert("Questions could not be retrieved, please try again","Error", QuestionPage.this);
					  finish();
				  }
              }
			  else
			  {
				  _questionIndex = _currentQuestionNumber;
              }

			  /*if(Util.Android_id.equalsIgnoreCase("6d692d322d2df2fb") || Util.Android_id.equalsIgnoreCase("e64b49e28d3e849c")) {
				  displayQuestionSetAndQuestionNumber();
			  }*/

			  _question = QuestionList.get(_questionIndex);
              _currentQuestionNumber++;
			  setControlTexts(_question);
              EnableAnswers(true);

              RadioButton b = findViewById(R.id.radio_option1);
              b.setChecked(false);

              b = findViewById(R.id.radio_option2);
              b.setChecked(false);

              b = findViewById(R.id.radio_option3);
              b.setChecked(false);

              b = findViewById(R.id.radio_option4);
              b.setChecked(false);
              buttonNext.setEnabled(false);
		  }
      }
      catch (Exception e)
      {
          displayAlertBox(e.getMessage());
      }
  }

  public void addButtonListener() {

	  buttonNext.setOnClickListener(new OnClickListener() {

		  @Override
		  public void onClick(View v) {
			  //saveStateOfTest();
			  if(buttonNext.getText().toString().compareToIgnoreCase("submit") == 0)
              {
                  clearState(); // Test is completed.. so remove the saved state
                  openTestReportActivity();
                  finish();
                  showInterstitialAdAd();
			  }
			  else
			  {
                  moveToNextQuestion();
			  }
		  }
	  });

      Button buttonExitTest = (Button)findViewById(R.id.buttonExitTest);
      buttonExitTest.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
              if(!QuestionPage.this.isFinishing()) {
                  displayAlertWithOkCancel("Are you sure to exit the test", "Exit?", QuestionPage.this);
              }
          }
      });
  }

	private void openTestReportActivity()
	{
		Intent testReport = new Intent();
		testReport.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".TestReport");
		testReport.putExtra("correct_ans_count", String.valueOf(correctAnswerCount));
		testReport.putExtra("questionCount", String.valueOf(questionCount));
		testReport.putExtra("isRevision", isRevision);
		testReport.putExtra("reward", reward);
		testReport.putExtra("points", rewardPoints);
		Util.revisionQuestions = revisionQuestions;
		startActivity(testReport);
	}
  
  public void clearState()
  {
	  String fileName = Util.SCHOOL_NAME + "_" + Util.GRADE + "_" + Util.Subject + "_state.txt";

	  try
	  {
		  FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
			
		  OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
		  BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

          bufferedWriter.write("");

          bufferedWriter.close();
		  outputStreamWriter.close();
		  fileOutputStream.close();
	  }
	  catch(IOException e)
	  {
		  displayAlertBox("ERROR-404:" + e.getMessage());
	  }
  }

public void onClickAdImage(View view)
{
	showInterstitialAdAd();
	if(_linkText == null)
	{
		return;
	}

		if(_linkText.isEmpty())
		{
			return;
		}

		try {
			saveIfAdClicked();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(_linkText));
			startActivity(intent);
		}
		catch(Exception e)
		{
			Log.d("ADIMAGEERROR", e.getMessage());
		}
	}

	private void saveIfAdClicked() {
		Firebase.goOnline();
		String AdClickedReportRoot = "https://schooltests.firebaseio.com/adimageclicked";
		Firebase ref = new Firebase(AdClickedReportRoot);
		Firebase childRef = ref.child("000_lastAdClicked");
		childRef.setValue(Util.getCurrentDateTime() + "/" + Util.UserUid);
		childRef = ref.child(UUID.randomUUID().toString());
		childRef.setValue(Util.getCurrentDateTime() + "/" + Util.UserUid);
	}

	public void onClickSupportiveText(View view)
	{
		Intent intent = new Intent();
		intent.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".SupportiveTextLargeView");
		intent.putExtra("SupportiveText", ((TextView) findViewById(R.id.textViewSupportiveText)).getText());
		intent.putExtra("Question", ((TextView)findViewById(R.id.textViewQuestion)).getText());
		startActivity(intent);
	}

	public int getRandomQuestionNumber()
	{
		Random random = new Random();
		int generatedRandomNumber;
		generatedRandomNumber = random.nextInt(_questionNumbers.size());
		Log.d("QuestionNumbersSize", String.valueOf(_questionNumbers.size()));
		int questionNumber = ((Integer)(_questionNumbers.get(generatedRandomNumber))).intValue();
		_questionNumbers.remove(generatedRandomNumber);
		return questionNumber;
	}
}
