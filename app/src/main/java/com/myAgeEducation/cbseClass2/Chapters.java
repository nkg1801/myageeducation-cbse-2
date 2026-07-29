package com.myAgeEducation.cbseClass2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.myAgeEducation.cbsecommon.Question;

public class Chapters  extends Activity
{
	private ArrayList<Question> questionList;
	private ArrayList<Question> filteredQuestions;
	private int startChapter;

	private String reward="";
	private ArrayList<String> rewardTable = new ArrayList<String>();
	private ArrayList<String> pointsTable = new ArrayList<String>();
	private String points = "";
	private SharedPreferences prefMgr;
	private String _prefixForChapterSelectionPreferences;
	private InterstitialAd mInterstitialAd;
	int endChapter;
	private ArrayList<Integer> selectedChapters = new ArrayList<>();


	@SuppressWarnings("unchecked")
    @Override
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
		setContentView(R.layout.chapters);

		//addInterstitialAd();
		addBannerAd();

		_prefixForChapterSelectionPreferences = Util.SCHOOL_NAME + "_" + Util.GRADE + "_" + Util.Subject + "_";

		prefMgr = PreferenceManager.getDefaultSharedPreferences(this);

		SeekBar seekBarNoOfQuestions = findViewById(R.id.seekBarNoOfQuestions);

		questionList = (ArrayList<Question>)Util.allQuestions.clone();

		try {
			Question firstQuestion = questionList.get(0);
			Question lastQuestion = questionList.get(questionList.size() - 1);

			filteredQuestions = (ArrayList<Question>)questionList.clone();

			startChapter = firstQuestion.getChapter();
			endChapter = lastQuestion.getChapter();
		}
		catch(Exception e)
		{
			startChapter = 0;
			endChapter = 0;
            displayAlertBox("Questions could not be retrieved, please try again", "Error");
            finish();
		}
		
		int chapters = endChapter - startChapter + 1;
		
		CheckBox checkBoxAllChapters = findViewById(R.id.checkBoxAllChapters);
		checkBoxAllChapters.setText(checkBoxAllChapters.getText() + " (" + String.valueOf(chapters) + " Chapters)");
		
		CheckBox checkBoxAllQuestions = findViewById(R.id.checkBoxAllQuestions);
		int questionCount = questionList.size();
		checkBoxAllQuestions.setText(checkBoxAllQuestions.getText() + " (" + String.valueOf(questionCount) + " Questions)");
		checkBoxAllQuestions.setChecked(true);

		int questions_savedPref = prefMgr.getInt(_prefixForChapterSelectionPreferences + "questions", questionCount);
		boolean isAllQuestionsSelected = prefMgr.getBoolean(_prefixForChapterSelectionPreferences + "isAllQuestionSelected", true);

        getSelectedChaptersFromSavedPref();

		TextView textViewChapterRange = findViewById(R.id.textViewChapterRange);
		textViewChapterRange.setText("");

		Util.filteredQuestions = filterQuestionsOnSelectedChapters(0, 0);

		questionCount = Util.filteredQuestions.size();
		seekBarNoOfQuestions.setMax(questionCount - 1);

		if(isAllQuestionsSelected)
		{
			questions_savedPref = questionCount;
		}
		else if(questions_savedPref > questionCount)
		{
			questions_savedPref = questionCount;
		}

		SetSubjectTitle();
		populateChapterTableEx();
        highlightChapters();
        SetSeekbarValues(startChapter, endChapter);
        String str = "No of Questions selected for test: " + Integer.toString(questions_savedPref);

        TextView textViewQuestionCount = findViewById(R.id.textViewQuestionCount);
        textViewQuestionCount.setText(str);

        SetInitialStatusOfCheckBoxAllQuestions(questionCount, questions_savedPref);

		addButtonStartTestListener();
		addSeekBarListener();
		addCheckBoxListener();
	}

    private void getSelectedChaptersFromSavedPref()
    {
        String selectedChapter_savedPref = prefMgr.getString(Util.Subject +  "_SelectedChapters", "");

        String[] tokens = selectedChapter_savedPref.split(",");
        for(int i=0;i<tokens.length;i++)
        {
            if(tokens[i].trim() != "")
            {
                int chapter = Integer.parseInt(tokens[i]);

                if(!selectedChapters.contains(chapter)) {
                    selectedChapters.add(chapter);
                }
            }
        }

        if(selectedChapters.isEmpty())
        {
            selectedChapters.add(1);
        }
    }

	private void addBannerAd()
	{
		AdView mAdView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.build();
		mAdView.loadAd(adRequest);
	}

	private void SetSeekbarValues(int startChapter, int endChapter)
	{
		SeekBar seekBarNoOfQuestions = findViewById(R.id.seekBarNoOfQuestions);

		int questions_savedPref = prefMgr.getInt(_prefixForChapterSelectionPreferences + "questions", seekBarNoOfQuestions.getMax() + 1);
		boolean isAllQuestionsSelected = prefMgr.getBoolean(_prefixForChapterSelectionPreferences + "isAllQuestionSelected", true);

		if(isAllQuestionsSelected) {
			seekBarNoOfQuestions.setProgress(seekBarNoOfQuestions.getMax());
		}
		else{
			seekBarNoOfQuestions.setProgress(questions_savedPref - 1);
		}
	}

	private void SetInitialStatusOfCheckBoxAllQuestions(int questionCount, int questions_savedPref)
	{
		CheckBox checkBoxAllQuestions = findViewById(R.id.checkBoxAllQuestions);
		checkBoxAllQuestions.setText("All Available Questions (" + String.valueOf(questionCount) + " Questions)");

		if(questionCount == questions_savedPref)
		{
			checkBoxAllQuestions.setChecked(true);
		}
		else
		{
			checkBoxAllQuestions.setChecked(false);
		}
	}

	private void FormatTextViewBackgroundAndForegroundColor(TextView textView, int backcolor, int forecolor)
	{
		textView.setBackgroundColor(backcolor);
		textView.setTextColor(forecolor);
	}

	private void SetSubjectTitle()
	{
		String subject = Util.Subject;

		if(subject.equalsIgnoreCase("cs"))
		{
			((TextView) findViewById(R.id.textViewSubject)).setText("Subject: Computer Science");
		}
		else if(subject.equalsIgnoreCase("evs"))
		{
			((TextView) findViewById(R.id.textViewSubject)).setText("Subject: Science");
		}
		else
		{
			((TextView) findViewById(R.id.textViewSubject)).setText("Subject: " + subject);
		}
	}

	public void addCheckBoxListener()
	{
		final CheckBox checkBoxAllChapters = (CheckBox)findViewById(R.id.checkBoxAllChapters);
		checkBoxAllChapters.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					//SeekBar seekBarChaptersFrom = (SeekBar) findViewById(R.id.seekBarChaptersFrom);
//					seekBarChaptersFrom.setProgress(0);

					//SeekBar seekBarChaptersTo = (SeekBar) findViewById(R.id.seekBarChaptersTo);
					//seekBarChaptersTo.setProgress(seekBarChaptersTo.getMax());
					//highlightSelectedChapters(1, seekBarChaptersTo.getMax()+1);
                    //highlightSelectedChapters(true);
				}
                else
                {
                    //highlightSelectedChapters(false);
                }
				//showRewards();
			}
		});

        checkBoxAllChapters.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkBoxAllChapters.isChecked()) {
                    highlightSelectedChapters(true);
                }
                else
                {
                    highlightSelectedChapters(false);
                }
            }
        });
		
		CheckBox checkBoxAllQuestions = findViewById(R.id.checkBoxAllQuestions);
		checkBoxAllQuestions.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					SeekBar seekBarNoOfQuestions = findViewById(R.id.seekBarNoOfQuestions);
					seekBarNoOfQuestions.setProgress(seekBarNoOfQuestions.getMax());
				}
			}
		});
	}
	
	public void saveChoices()
	{
		SeekBar seekBarNoOfQuestions = findViewById(R.id.seekBarNoOfQuestions);

		String prefKeyQuestions = _prefixForChapterSelectionPreferences + "questions";
		int prefValueQuestions = seekBarNoOfQuestions.getProgress() + 1;

		String prefKeyIsAllQuestionsSelected = _prefixForChapterSelectionPreferences + "isAllQuestionSelected";

		Editor prefEdit = prefMgr.edit();

		String selectedChaptersString = "";

		for(int i=0;i < selectedChapters.size();i++)
		{
			selectedChaptersString = selectedChaptersString + String.valueOf(selectedChapters.get(i)) + ",";
		}

        prefEdit.putString(Util.Subject +  "_SelectedChapters", selectedChaptersString);
		prefEdit.putInt(prefKeyQuestions, prefValueQuestions);
		prefEdit.putBoolean(prefKeyIsAllQuestionsSelected, ((CheckBox) findViewById(R.id.checkBoxAllQuestions)).isChecked());
		prefEdit.apply();
	}
	
	public void addSeekBarListener()
	{
		SeekBar seekBar3 = findViewById(R.id.seekBarNoOfQuestions);
		seekBar3.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) {
				SeekBar seekBar = findViewById(R.id.seekBarNoOfQuestions);
				String str = "No of questions selected for test: " + Integer.toString(seekBar.getProgress() + 1);
				TextView tv = findViewById(R.id.textViewQuestionCount);
				tv.setText(str);

				CheckBox checkBox = findViewById(R.id.checkBoxAllQuestions);
				if (seekBar.getProgress() == seekBar.getMax()) {
					checkBox.setChecked(true);
				} else {
					checkBox.setChecked(false);
				}

				if(ValidateChapterSelection()) {
                    saveChoices();
                }
			}

			public void onStartTrackingTouch(SeekBar seek) {
			}

			public void onStopTrackingTouch(SeekBar seek) {
			}
		});
	}
	
	public ArrayList<Question> filterQuestionsOnSelectedChapters(int start, int end)
	{
		String str;
		ArrayList<Question> filteredQuestions = new ArrayList<>();
		for(int i = 0; i < questionList.size(); i++)
		{
			Question question = questionList.get(i);
			int chapter = question.getChapter();

            if(selectedChapters.contains(chapter))
			{
				filteredQuestions.add(question);
			}
		}
        CheckBox checkBoxAllQuestions = findViewById(R.id.checkBoxAllQuestions);
        int questionCount = filteredQuestions.size();
        checkBoxAllQuestions.setText("All Available Questions (" + String.valueOf(questionCount) + " Questions)");

        SeekBar seekBarNoOfQuestions = findViewById(R.id.seekBarNoOfQuestions);
        seekBarNoOfQuestions.setMax(questionCount - 1);
        seekBarNoOfQuestions.setProgress(seekBarNoOfQuestions.getMax());

        str = "No of Questions Selected for test: " + Integer.toString(seekBarNoOfQuestions.getProgress() + 1);
        TextView tv = findViewById(R.id.textViewQuestionCount);
        tv.setText(str);

		str = "Chapter Selection - " + String.valueOf(selectedChapters.size()) + " chapters selected";
		tv = findViewById(R.id.textViewChapterSelection);
		tv.setText(str);
		
        return filteredQuestions;
	}
	
	public void addButtonStartTestListener()
	{
		Button button = findViewById(R.id.buttonStartTest);

		button.setOnClickListener(v -> {
            if(ValidateChapterSelection())
            {
				saveChoices();
				openTestActivity(v);
            }
			else
			{
				displayAlertBox("Select at least 1 chapter to take the test", "No chapter selected");
			}
        });
	}

	private boolean	ValidateChapterSelection()
	{
        return !selectedChapters.isEmpty();
	}
	
	public void openTestActivity(View v)
	{
		Bundle bundle = getIntent().getExtras();
		if(QuestionPage.QuestionList == null)
		{
			QuestionPage.QuestionList = new ArrayList<>();
		}
		QuestionPage.QuestionList = (ArrayList<Question>)Util.filteredQuestions.clone();

		if(QuestionPage.QuestionList.isEmpty())
		{
			finish();
		}

		  Intent testPage = new Intent();
		  testPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".QuestionPage");
		  SeekBar seekBar = findViewById(R.id.seekBarNoOfQuestions);
		  int questionCount = seekBar.getProgress() + 1;
		  testPage.putExtra("questionCount", questionCount);
		  testPage.putExtra("isRevision", "false");
		  testPage.putExtra("isExit", "false");
		  testPage.putExtra("reward", reward);
		  testPage.putExtra("points", points);
		  testPage.putExtra("recover_mode", false);
			assert bundle != null;
			testPage.putExtra("question_set", bundle.getString("question_set"));

		  testPage.putStringArrayListExtra("wrongAns_list", null);
		  testPage.putIntegerArrayListExtra("used_numbers", null);
		  
		  startActivity(testPage);
		  finish();
		  //showInterstitialAdAd();
	}

	private void getChapterNames()
	{
		for(int i = 0; i < Util.allQuestions.size(); i++)
		{
			String chapterName = Util.allQuestions.get(i).getChapterName();
			if(chapterName != null) {
				if (!chapterName.isEmpty()) {
					int key = Util.allQuestions.get(i).getChapter();
					if(!linkedHashMap.containsKey(key)) {
						linkedHashMap.put(key, chapterName);
					}
				}
			}
		}
	}

	TableLayout table_layout;
	LinkedHashMap linkedHashMap = new LinkedHashMap();

    private void highlightSelectedChapters(boolean highlight)
    {
        table_layout = findViewById(R.id.tableLayoutChapters);

		final int childCount = table_layout.getChildCount(); //gets the number of rows
        selectedChapters.clear();
		
		for (int i = 0; i < childCount; i++) {
			TableRow row = (TableRow)table_layout.getChildAt(i);
            int rowChildCount = row.getChildCount();
			for(int j = 0; j < rowChildCount; j++) {
                View tv = row.getChildAt(j);
                if(highlight)
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.rounded_corner_selected));
                    tv.setTag(R.id.TAG_SELECTION,1);
                    int chapter = (Integer)tv.getTag();
                    if(!selectedChapters.contains(chapter)) {
                        selectedChapters.add(chapter);
                    }
                }
                else
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                    tv.setTag(R.id.TAG_SELECTION,0);
                }
            }
		}

        Util.filteredQuestions = filterQuestionsOnSelectedChapters(0, 0);
    }

    private void highlightChapters()
    {
        boolean isAllSelected = true;
        table_layout = findViewById(R.id.tableLayoutChapters);

        final int childCount = table_layout.getChildCount(); //gets the number of rows

        for (int i = 0; i < childCount; i++) {
            TableRow row = (TableRow)table_layout.getChildAt(i);
            int rowChildCount = row.getChildCount();
            for(int j = 0; j < rowChildCount; j++) {
                View tv = row.getChildAt(j);
                int temp = (Integer)tv.getTag();
                if(selectedChapters.contains(temp))
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.rounded_corner_selected));
                    tv.setTag(R.id.TAG_SELECTION,1);
                }
                else
                {
                    tv.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                    tv.setTag(R.id.TAG_SELECTION,0);
                    isAllSelected = false;
                }
            }
        }

        if(isAllSelected)
        {
            ((CheckBox)findViewById(R.id.checkBoxAllChapters)).setChecked(true);
        }

        Util.filteredQuestions = filterQuestionsOnSelectedChapters(0, 0);
    }

	private void populateChapterTableEx()
	{
		getChapterNames();
		Log.d("HashMapSize", String.valueOf(linkedHashMap.size()));

        int chapterCount = linkedHashMap.size();

		if(chapterCount == 0)
		{
			//findViewById(R.id.textViewChapterDetails).setVisibility(View.GONE);
			findViewById(R.id.textViewSelectedChapters).setVisibility(View.GONE);
			findViewById(R.id.textViewSelectedColor).setVisibility(View.GONE);
			return;
		}

        int maxRow = 4;

        if(chapterCount < maxRow)
        {
            maxRow = chapterCount;
        }

        int columnCount = chapterCount / maxRow;

        if(chapterCount % maxRow != 0)
        {
            columnCount = columnCount + 1;
        }

		table_layout = findViewById(R.id.tableLayoutChapters);

        ArrayList<TableRow> tableRows = new ArrayList<>();

        TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        llp.setMargins(0,0,10,0);

        for(int i=0; i < maxRow; i++)
        {
            TableRow row = new TableRow(this);
            row.setPadding(30,10,30,10);
            row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tableRows.add(row);
        }

        for(int i = 1, row = 1; i <= chapterCount; i++, row++) {

            TextView tv = new TextView(this);
            tv.setLayoutParams(llp);
            tv.setPadding(30,50,30,50);
            String chapterName;

            if(linkedHashMap.get(i) != null) {
                chapterName = linkedHashMap.get(i).toString();
            }
            else
            {
                chapterName = "NA";
            }
            String key = String.valueOf(i);
            tv.setGravity(Gravity.LEFT);
            tv.setText(key + ". " + chapterName);
            tv.setPadding(30,30,30,30);
            tv.setBackgroundColor(Color.GRAY);
            tv.setTag(i);
            tv.setTag(R.id.TAG_SELECTION,0);
            tv.setBackground(ContextCompat.getDrawable(this,R.drawable.rounded_corner));
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickChapterTextView(v);
                }
            });

            tableRows.get(row-1).addView(tv);
            if(row % maxRow == 0)
            {
                row = 0;
            }
        }

        for(int i=0;i<tableRows.size();i++)
        {
            table_layout.addView(tableRows.get(i));
        }
	}

    private void onClickChapterTextView(View v)
    {
        if((int)v.getTag(R.id.TAG_SELECTION) != 1)
        {
            v.setBackground(getResources().getDrawable(R.drawable.rounded_corner_selected));
            v.setTag(R.id.TAG_SELECTION,1);
            int chapter = (Integer)v.getTag();

            if(!selectedChapters.contains(chapter)) {
                selectedChapters.add(chapter);
            }

            if(isAllChapterSelected())
            {
				selectAllChapterSelectedCheckBox(true);
            }
        }
        else
        {
            v.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
            v.setTag(R.id.TAG_SELECTION,0);
            selectedChapters.remove(v.getTag());
            selectAllChapterSelectedCheckBox(false);
        }

        Util.filteredQuestions = filterQuestionsOnSelectedChapters(0, 0);
    }

	private void selectAllChapterSelectedCheckBox(boolean isSelected)
	{
		((CheckBox)findViewById(R.id.checkBoxAllChapters)).setChecked(isSelected);
	}

    private boolean isAllChapterSelected()
    {
        table_layout = findViewById(R.id.tableLayoutChapters);

        final int childCount = table_layout.getChildCount(); //gets the number of rows

        for (int i = 0; i < childCount; i++) {
            TableRow row = (TableRow)table_layout.getChildAt(i);
            int rowChildCount = row.getChildCount();
            for(int j = 0; j < rowChildCount; j++) {
                View tv = row.getChildAt(j);
                if((int)tv.getTag(R.id.TAG_SELECTION) != 1)
                {
                    return false;
                }
            }
        }
        return true;
    }

	 public void displayAlertBox(String message, String title)
	 {
		 if(!Chapters.this.isFinishing()) {
			 Util.displayAlert(message, title, Chapters.this);
		 }
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

	public void onClickAdButton(View view)
	{
		openLink();
	}

	public void onClickViewFlipper(View view) {
		openLink();
	}

	private void openLink()
	{
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			//intent.setData(Uri.parse("https://bit.ly/2Z52Byj"));
			intent.setData(Uri.parse(Util.AdDetail.getLink()));
			startActivity(intent);
		}
		catch(Exception e)
		{
			//displayAlert(e.getMessage(), "Error", _context);
		}
	}
}
