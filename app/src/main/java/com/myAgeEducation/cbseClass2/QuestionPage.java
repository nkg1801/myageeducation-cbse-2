package com.myAgeEducation.cbseClass2;

import static com.myAgeEducation.cbseClass2.QuestionTemplate.DAYS_IN_MONTH_TEMPLATE;
import static com.myAgeEducation.cbseClass2.QuestionTemplate.MONTH_TYPE_TEMPLATES;
import static com.myAgeEducation.cbseClass2.QuestionTemplate.TEMPLATES;
import static com.myAgeEducation.cbseClass2.QuestionTemplate.YEAR_TEMPLATE;

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
import com.myAgeEducation.cbsecommon.Question;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
  	public int totalSeconds = 0;
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
	private TextView textViewCounter;
	private int counter;
	boolean _serverTimeRetrieved = false;
	String _lastAttemptTime;
	String _monthYear;
	int _scoreSavedIntermediately = 0;

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
		counter = 61;

		revisionQuestions = new ArrayList<>();

		displayInitialScore();

		if(isExit.equalsIgnoreCase("true"))
		{
			finish();
		}
	
		displayScore();

		buttonNext = findViewById(R.id.buttonNext);
        tableLayout1 = findViewById(R.id.tableLayout1);
		//textViewCounter = findViewById(R.id.textViewCounter);

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

    private void DisplayTime(int seconds)
	{
		int minutes = seconds / 60;
		int second = seconds % 60;
		int hours = minutes / 60;
		minutes = minutes %60;

		String elapsedTime;
		TextView textView = findViewById(R.id.textViewTimer);
		elapsedTime = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", second) + " ";
		textView.setText(elapsedTime);
	}
  
  public void addTimer()
  {
	  Timer timer = new Timer();
	  timer.scheduleAtFixedRate(new TimerTask() {
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

		//imageData = "use_clock_generator_code;01_05";
		//imageData = "use_calendar_generator_code;6_2026";

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
			img.setImageBitmap(ClockGenerator.generateClock(hours, minute, 800, Util.BackgroundTheme.RANDOM));
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
			Bitmap bitmap =
					CalendarGenerator.generateCalendar(
							year,
							month - 1, // this is 0 based
							1200);

			img.setImageBitmap(bitmap);
			img.setVisibility(View.VISIBLE);

			int calendarWeekday = DateTimeUtils.getRandomCalendarWeekday();
			String question = "How many " + DateTimeUtils.WEEKDAYS[calendarWeekday - 1] + "s are there in August 2026?";
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

	private static final Random RANDOM = new Random();
	private static final int MIN_YEAR = 2020;
	private static final int MAX_YEAR = 2030;

	private Question generateCalendarQuestionOld()
	{
		int year = MIN_YEAR + RANDOM.nextInt(MAX_YEAR - MIN_YEAR + 1);
		Month month = Month.of(RANDOM.nextInt(12) + 1);
		DayOfWeek weekday = DayOfWeek.of(RANDOM.nextInt(7) + 1);

		String question =
				"How many "
						+ weekday.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
						+ "s are there in "
						+ month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
						+ " "
						+ year
						+ "?";

		YearMonth ym = YearMonth.of(year, month);

		int count = getWeekdayCount(ym, weekday);

		List<String> options = new ArrayList<>();

		options.add(String.valueOf(count));

		if (count == 4) {
			options.add("5");
		}
		else {
			options.add("4");
		}

		options.add("3");
		options.add("6");

		Collections.shuffle(options, RANDOM);

		Question questionObj = new Question();
		questionObj.setQuestion(question);
		questionObj.setAnswer(String.valueOf(count));
		questionObj.setOption1(options.get(0));
		questionObj.setOption2(options.get(1));
		questionObj.setOption3(options.get(2));
		questionObj.setOption4(options.get(3));
		questionObj.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);
		return questionObj;
	}

	private Question generateCalendarBasedQuestions()
	{
		int year = MIN_YEAR + RANDOM.nextInt(MAX_YEAR - MIN_YEAR + 1);
		Month month = Month.of(RANDOM.nextInt(12) + 1);
		Question question = new Question();
		QuestionTemplate.CalendarQuestionType type =
				QuestionTemplate.CalendarQuestionType.values()[RANDOM.nextInt(QuestionTemplate.CalendarQuestionType.values().length)];

		switch (type) {

			case WEEKDAY_COUNT:
				return generateWeekdayCountQuestion(year, month);

			case MONTH_NAME:
				return generateMonthQuestion(year, month);

			case YEAR:
				return generateYearQuestion(year, month);

			case DAYS_IN_MONTH:
				return generateDaysInMonthQuestion(year, month);

			case FIRST_DAY:
				return generateFirstDayOfMonthQuestion(year, month);

			case LAST_DAY:
				return generateLastDayOfMonthQuestion(year, month);

			case DAY_OF_DATE:
				return generateRelativeDayQuestion(year, month, RelativeDayType.CURRENT);

			case NEXT_DAY:
				return generateRelativeDayQuestion(year, month, RelativeDayType.NEXT);

			case PREVIOUS_DAY:
				return generateRelativeDayQuestion(year, month, RelativeDayType.PREVIOUS);
		}
		return question;
	}

	private Question generateWeekdayCountQuestion(int year, Month month)
	{
		DayOfWeek weekday = DayOfWeek.of(RANDOM.nextInt(7) + 1);

		YearMonth ym = YearMonth.of(year, month);

		int count = getWeekdayCount(ym, weekday);

		List<String> options = new ArrayList<>();

		options.add(String.valueOf(count));

		if (count == 4) {
			options.add("5");
		}
		else {
			options.add("4");
		}

		options.add("3");
		options.add("6");

		Collections.shuffle(options, RANDOM);

		Question questionObj = new Question();

		QuestionTemplate template = TEMPLATES.get(RANDOM.nextInt(TEMPLATES.size()));
		questionObj.setQuestion(template.format(weekday, month, year));

		//questionObj.setQuestion(question);
		questionObj.setAnswer(String.valueOf(count));
		questionObj.setOption1(options.get(0));
		questionObj.setOption2(options.get(1));
		questionObj.setOption3(options.get(2));
		questionObj.setOption4(options.get(3));
		questionObj.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);
		return questionObj;
	}

	private Question generateMonthQuestion(int year, Month month) {

		List<String> options = new ArrayList<>();

		options.add(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

		while (options.size() < 4) {

			Month m = Month.of(RANDOM.nextInt(12) + 1);

			String name = m.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

			if (!options.contains(name))
				options.add(name);
		}

		Collections.shuffle(options, RANDOM);

		Question q = new Question();

		QuestionTemplate template = MONTH_TYPE_TEMPLATES.get(RANDOM.nextInt(MONTH_TYPE_TEMPLATES.size()));
		DayOfWeek weekday = DayOfWeek.of(RANDOM.nextInt(7) + 1);
		q.setQuestion(template.format(weekday, month, year));
		//q.setQuestion("Which month calendar is shown below?");

		q.setAnswer(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

		q.setOption1(options.get(0));
		q.setOption2(options.get(1));
		q.setOption3(options.get(2));
		q.setOption4(options.get(3));

		q.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);

		return q;
	}

	private Question generateYearQuestion(int year, Month month) {

		List<String> options = new ArrayList<>();

		options.add(String.valueOf(year));

		while (options.size() < 4) {

			int y = 2020 + RANDOM.nextInt(11);

			String value = String.valueOf(y);

			if (!options.contains(value))
				options.add(value);
		}

		Collections.shuffle(options, RANDOM);

		String selectedQuestion;

		int variant = RANDOM.nextInt(YEAR_TEMPLATE.length);

		selectedQuestion = YEAR_TEMPLATE[variant];

		Question q = new Question();

		q.setQuestion("Which year's calendar is shown below?");
		q.setQuestion(selectedQuestion);

		q.setAnswer(String.valueOf(year));

		q.setOption1(options.get(0));
		q.setOption2(options.get(1));
		q.setOption3(options.get(2));
		q.setOption4(options.get(3));

		q.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);
		return q;
	}

	private Question generateDaysInMonthQuestion(int year, Month month) {

		//int year = 2020 + RANDOM.nextInt(11);
		//Month month = Month.of(RANDOM.nextInt(12) + 1);

		YearMonth yearMonth = YearMonth.of(year, month);

		int days = yearMonth.lengthOfMonth();

		String question = "How many days are there in "
				+ month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
				+ " "
				+ year
				+ "?";

		String selectedQuestion;

		int variant = RANDOM.nextInt(DAYS_IN_MONTH_TEMPLATE.length);

		if (variant == 0) {
			selectedQuestion = String.format(
					DAYS_IN_MONTH_TEMPLATE[variant],
					month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
					year);
		} else {
			selectedQuestion = DAYS_IN_MONTH_TEMPLATE[variant];
		}

		List<String> options = new ArrayList<>();
		options.add(String.valueOf(days));

		// Generate plausible distractors
		if (days == 28) {
			options.add("29");
			options.add("30");
			options.add("31");
		}
		else if (days == 29) {
			options.add("28");
			options.add("30");
			options.add("31");
		}
		else if (days == 30) {
			options.add("28");
			options.add("29");
			options.add("31");
		}
		else { //31
			options.add("28");
			options.add("29");
			options.add("30");
		}

		Collections.shuffle(options, RANDOM);

		Question q = new Question();

		q.setQuestion(selectedQuestion);

		q.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);

		q.setAnswer(String.valueOf(days));

		q.setOption1(options.get(0));
		q.setOption2(options.get(1));
		q.setOption3(options.get(2));
		q.setOption4(options.get(3));

		return q;
	}

	private Question generateFirstDayOfMonthQuestion(int year, Month month)
	{
		YearMonth ym = YearMonth.of(year, month);
		DayOfWeek answer = ym.atDay(1).getDayOfWeek();

		String[] FIRST_DAY_QUESTIONS = {
				"Which day does %s %d start on?",
				"Which day is the first day of the given month?",
				"Look at the calendar below. Which day comes first in the month?",
				"The first day of %s %d is:",
				"Observe the calendar. Which weekday is the 1st of the month?"
		};


		String questionText;

		int variant = RANDOM.nextInt(FIRST_DAY_QUESTIONS.length);

		switch (variant) {

			case 0:
			case 3:
				questionText = String.format(
						FIRST_DAY_QUESTIONS[variant],
						month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
						year);
				break;

			default:
				questionText = FIRST_DAY_QUESTIONS[variant];
		}

		Question question = new Question();
		question.setQuestion(questionText);


		List<String> options = generateDayOptions(answer);

		question.setOption1(options.get(0));
		question.setOption2(options.get(1));
		question.setOption3(options.get(2));
		question.setOption4(options.get(3));
		question.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);

		String answerText =
				answer.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

		question.setAnswer(answerText);

		return question;
	}

	private Question generateLastDayOfMonthQuestion(int year, Month month)
	{
		YearMonth ym = YearMonth.of(year, month);
		DayOfWeek answer = ym.atEndOfMonth().getDayOfWeek();

		String[] questionVariants = {
				"Which day does %s %d end on?",
				"Which day is the last day of the given month?",
				"Look at the calendar below. Which day comes last in the month?",
				"The last day of %s %d is:",
				"Observe the calendar. Which weekday is the last day of the month?"
		};

		int variant = RANDOM.nextInt(questionVariants.length);

		String questionText;

		switch (variant)
		{
			case 0:
			case 3:
				questionText = String.format(
						questionVariants[variant],
						month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
						year);
				break;

			default:
				questionText = questionVariants[variant];
		}

		List<String> options = generateDayOptions(answer);

		String answerText = answer.getDisplayName(
				TextStyle.FULL,
				Locale.ENGLISH);

		Question question = new Question();

		question.setQuestion(questionText);

		question.setOption1(options.get(0));
		question.setOption2(options.get(1));
		question.setOption3(options.get(2));
		question.setOption4(options.get(3));

		question.setAnswer(answerText);

		question.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);

		return question;
	}

	public enum RelativeDayType {
		CURRENT,
		NEXT,
		PREVIOUS
	}
	private Question generateRelativeDayQuestion(
			int year,
			Month month,
			RelativeDayType type)
	{
		YearMonth ym = YearMonth.of(year, month);

		// Pick a random valid date
		int date = RANDOM.nextInt(ym.lengthOfMonth()) + 1;

		LocalDate localDate = ym.atDay(date);

		DayOfWeek answer;

		String[] questionVariants;

		switch (type)
		{
			case NEXT:
				answer = localDate.getDayOfWeek().plus(1);

				questionVariants = new String[] {
						"Which day comes after %d %s %d?",
						"The next day after %d %s %d is:",
						"What day comes after %d %s %d?",
						"Look at the calendar below. Which day comes after %d?",
						"Observe the calendar. What day comes after the %dth?"
				};

				break;

			case PREVIOUS:
				answer = localDate.getDayOfWeek().minus(1);

				questionVariants = new String[] {
						"Which day comes before %d %s %d?",
						"The previous day before %d %s %d is:",
						"What day comes before %d %s %d?",
						"Look at the calendar below. Which day comes before %d?",
						"Observe the calendar. What day comes before the %dth?"
				};

				break;

			default:
				answer = localDate.getDayOfWeek();

				questionVariants = new String[] {
						"On which day does %d %s %d fall?",
						"What day is %d %s %d?",
						"Which day of the week is %d %s %d?",
						"Look at the calendar below. Which day is %d?",
						"Observe the calendar. %d falls on which day?"
				};
		}

		int variant = RANDOM.nextInt(questionVariants.length);

		String questionText;

		switch (variant)
		{
			case 0:
			case 1:
			case 2:
				questionText = String.format(
						questionVariants[variant],
						date,
						month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
						year);
				break;

			default:
				questionText = String.format(
						questionVariants[variant],
						date);
		}

		List<String> options = generateDayOptions(answer);

		String answerText =
				answer.getDisplayName(
						TextStyle.FULL,
						Locale.ENGLISH);

		Question question = new Question();

		question.setQuestion(questionText);

		question.setOption1(options.get(0));
		question.setOption2(options.get(1));
		question.setOption3(options.get(2));
		question.setOption4(options.get(3));

		question.setAnswer(answerText);

		question.setImage("use_calendar_generator_code;" + month.getValue() + "_" + year);

		return question;
	}
	private int getWeekdayCount(
			YearMonth yearMonth,
			DayOfWeek weekday) {

		int count = 0;

		for (int d = 1; d <= yearMonth.lengthOfMonth(); d++) {

			if (yearMonth.atDay(d).getDayOfWeek() == weekday) {
				count++;
			}
		}

		return count;
	}

	private List<String> generateDayOptions(
			DayOfWeek answer) {

		List<String> days = Arrays.asList(
				"Monday",
				"Tuesday",
				"Wednesday",
				"Thursday",
				"Friday",
				"Saturday",
				"Sunday");

		Collections.shuffle(days, RANDOM);

		List<String> options =
				new ArrayList<>();

		options.add(answer.getDisplayName(
				TextStyle.FULL,
				Locale.ENGLISH));

		for (String day : days) {

			if (!day.equals(options.get(0))) {

				options.add(day);

				if (options.size() == 4)
					break;
			}
		}

		Collections.shuffle(options, RANDOM);

		return options;
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
	  Firebase.goOffline();
	  //question.setQuestion("generate_this_question_calendar_type_weekday_count");

	  if(question.getQuestion().contains("generate_this_question_calendar_type"))
	  {
		  //question = generateWeekdayCountQuestion();
		  question = generateCalendarBasedQuestions();
	  }

	  else if(question.getImage() != null) {
		  if (question.getImage().contains("use_clock_generator_code")) {
			  question = replaceTimeQuestionWithGeneratedQuestion(question);
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
		TimeGenerator timeGenerator = new TimeGenerator();
	  private Question replaceTimeQuestionWithGeneratedQuestion(Question question)
	  {
		  Question temp = new Question();
		  temp.setQuestion("What is the time on the clock?");
		  ClockTime clockTime = timeGenerator.nextTime();
		  temp.setImage("use_clock_generator_code;" + clockTime.hour + "_" + clockTime.minute);
		  int twelveHourFormat = clockTime.hour	> 12 ? clockTime.hour - 12 : clockTime.hour;
		  String minute = clockTime.minute < 10 ? "0" + clockTime.minute : String.valueOf(clockTime.minute);
		  String answer = twelveHourFormat + ":" + minute;

		  List<String> options = generateOptions(clockTime);
		  temp.setOption1(options.get(0));
		  temp.setOption2(options.get(1));
		  temp.setOption3(options.get(2));
		  temp.setOption4(options.get(3));
		  temp.setAnswer(answer);
		  temp.setSupportiveText(question.getSupportiveText());
		  return temp;
	  }

	private List<String> generateOptions(ClockTime correctTime) {

		Set<String> options = new HashSet<>();

		String correct = formatTime(correctTime);

		options.add(correct);

		Random random = new Random();

		while (options.size() < 4) {

			int type = random.nextInt(4);

			int hour = correctTime.getHour();
			int minute = correctTime.getMinute();

			switch (type) {

				// Wrong hour
				case 0:
					hour = (hour % 12) + 1;
					break;

				// Previous hour
				case 1:
					hour = hour == 1 ? 12 : hour - 1;
					break;

				// +5 minutes
				case 2:
					minute += 5;
					if (minute >= 60) {
						minute = 0;
						hour = (hour % 12) + 1;
					}
					break;

				// -5 minutes
				case 3:
					minute -= 5;
					if (minute < 0) {
						minute = 55;
						hour = hour == 1 ? 12 : hour - 1;
					}
					break;
			}

			options.add(formatTime(new ClockTime(hour, minute)));
		}

		List<String> list = new ArrayList<>(options);

		Collections.shuffle(list);

		return list;
	}

	private String formatTime(ClockTime time) {
		return String.format("%d:%02d",
				time.getHour(),
				time.getMinute());
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

	/*private void displayQuestionSetAndQuestionNumber()
	{
		((TextView) findViewById(R.id.textViewSubject)).setText("Subject: " + Util.Subject + "/" + _questionSet + "/" + _questionIndex);
	}*/

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
	
	// Listener for the Submit button
	/*ImageButton submitButton = (ImageButton) findViewById(R.id.buttonSubmit);
	
	submitButton.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			clearState(); // Test is completed.. so remove the saved state
			openTestReportActivity();
			finish();
			showInterstitialAdAd();
		}
	});*/
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
