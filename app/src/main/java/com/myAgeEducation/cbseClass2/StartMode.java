package com.myAgeEducation.cbseClass2;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class StartMode extends Activity 
{
	private SharedPreferences prefMgr;

    @Override
	  public void onCreate(Bundle savedInstanceState) 
	  {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.start_mode);

          if(Util.isFreeApp) {
              addBannerAd();
          }

          prefMgr = PreferenceManager.getDefaultSharedPreferences(this);
          addStartNewTestButtonListener();
          addStartUnfinishedTestButtonListener();
	  }

    private void addBannerAd()
    {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("750a657019b49e621c42ce9a20c2cc30")
                .build();
        mAdView.loadAd(adRequest);
    }

    private void StartNewTest()
    {
        Intent subPage = new Intent();
        subPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".Chapters");
        startActivity(subPage);
    }

    /*
    private void StartUnfinishedTest()
    {
        Bundle bundle = getIntent().getExtras();

        ArrayList<Integer> usedNumbers = new ArrayList<Integer>();
        ArrayList<String> wrongAns = new ArrayList<String>();
        wrongAns = bundle.getStringArrayList("wrongAnswer_list");
        usedNumbers = bundle.getIntegerArrayList("used_numbers");
        int lastQuestionNumber = bundle.getInt("last_question_number");
        int lastScore = bundle.getInt("last_score");

        Intent questionPage = new Intent();
        questionPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".QuestionPage");

        String prefix = Util.SCHOOL_NAME + "_" + Util.GRADE + "_" + Util.Subject + "_";

        int chapter_from = prefMgr.getInt(prefix + "chapter_from", 1);
        int chapter_to = prefMgr.getInt(prefix + "chapter_to", 1);
        int questionCount = prefMgr.getInt(prefix + "questions", 1);

        questionPage.putExtra("questionCount", questionCount);
        questionPage.putExtra("isRevision", "false");
        questionPage.putExtra("isExit", "false");
        questionPage.putExtra("last_question_number", lastQuestionNumber);
        questionPage.putExtra("last_score", lastScore);

        ArrayList<String> questionList = Util.allQuestions;
        ArrayList<String> filteredQuestions = filterQuestionsOnChapters(questionList, chapter_from, chapter_to);

        questionPage.putExtra("reward", "");
        questionPage.putExtra("points", "");

        questionPage.putStringArrayListExtra("question_list", filteredQuestions);

        questionPage.putStringArrayListExtra("wrongAnswer_list", wrongAns);
        questionPage.putIntegerArrayListExtra("used_numbers", usedNumbers);
        questionPage.putExtra("user_choice", "");
        questionPage.putExtra("recover_mode", true);

        startActivity(questionPage);
    }
    */

    public void addStartNewTestButtonListener()
    {
        ImageButton buttonStartNewTest = (ImageButton)findViewById(R.id.buttonStartNewTest);
        buttonStartNewTest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                StartNewTest();
                finish();
            }
        });
    }

    public void addStartUnfinishedTestButtonListener()
    {
        ImageButton buttonStartUnfinishedTest = (ImageButton)findViewById(R.id.buttonStartUnfinishedTest);
        buttonStartUnfinishedTest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //StartUnfinishedTest();
                finish();
            }
        });
    }
	 
   	 public ArrayList<String> filterQuestionsOnChapters(ArrayList<String> questionList, int start, int end)
	 {
			String str = "";
			String[] tokens;
			ArrayList<String> filteredQuestions = new ArrayList<String>();
			for(int i = 0; i < questionList.size(); i++)
			{
				str = questionList.get(i);
				tokens = str.split(";");
				int ch = 0;
				try
				{
					ch = Integer.parseInt(tokens[0]);
					if(ch >= start && ch <= end)
					{
						filteredQuestions.add(str);
					}
				}
				catch(NumberFormatException nfe){}
			}
			return filteredQuestions;
	 }
}
