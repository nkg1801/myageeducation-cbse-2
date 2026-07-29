package com.myAgeEducation.cbseClass2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class TestReport extends Activity 
{
	private int correctAnsCount;
	private int questionCount;
	int MAX_NO_OF_SCORES = 100;
	String isRevision;
	private String reward;
	private String rewardPoints="";
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_report);

		if(Util.isFreeApp) {
			addBannerAd();
		}

		Bundle bundle = getIntent().getExtras();
		isRevision = bundle.getString("isRevision");
		reward = bundle.getString("reward");
		rewardPoints = bundle.getString("points");
		
		try
		{
			correctAnsCount = Integer.parseInt(bundle.getString("correct_ans_count"));
			questionCount = Integer.parseInt(bundle.getString("questionCount"));
		}
		catch(NumberFormatException nfe)
		{
			displayAlertBox(nfe.getMessage());
		}
		
		TextView textViewYouHaveWon = (TextView)findViewById(R.id.textViewYouHaveWon);
		ImageView imageView1 = (ImageView)findViewById(R.id.imageView1);
		TextView textViewRewardText = (TextView)findViewById(R.id.textViewRewardText);
		
		textViewYouHaveWon.setVisibility(View.INVISIBLE);
		imageView1.setVisibility(View.INVISIBLE);
		textViewRewardText.setVisibility(View.INVISIBLE);

		displayScore();

		if(Util.revisionQuestions.size() == 0)
		{
			findViewById(R.id.buttonStartRevision).setVisibility(View.GONE);

			try
			{
				displayReward();
			}
			catch(Exception e)
			{
				displayAlertBox("ERROR-402-" + e.getMessage());
			}
		}
		
		try
		{
			// write the score only if not in revision mode
			if(!isRevision.equalsIgnoreCase("true"))
			{
				String score = String.valueOf(correctAnsCount) + "/" + String.valueOf(questionCount);
				writeScore(score);
			}
		}
		catch(Exception e)
		{
			displayAlertBox("ERROR-401-" + e.getMessage());
		}
		addButtonListener();
	}
	
	public void displayScore()
	{
		TextView textViewScore = (TextView) findViewById(R.id.textViewScore);
		String score = String.valueOf(correctAnsCount) + "/" + String.valueOf(questionCount);
		textViewScore.setText(score);
		
		if(Util.revisionQuestions.size() == 0)
		{
			textViewScore.setTextColor(Color.BLUE);
		}
		
		else if(correctAnsCount < questionCount/2)
		{
			textViewScore.setTextColor(Color.RED); // red score for less than 50% marks
		}
	}
	
	public void displayReward()
	{
		TextView textViewYouHaveWon = (TextView)findViewById(R.id.textViewYouHaveWon);
		ImageView imageView1 = (ImageView)findViewById(R.id.imageView1);
		TextView textViewRewardText = (TextView)findViewById(R.id.textViewRewardText);
		
		textViewYouHaveWon.setVisibility(View.INVISIBLE);
		imageView1.setVisibility(View.INVISIBLE);
		textViewRewardText.setVisibility(View.INVISIBLE);
		
		if(!reward.isEmpty())
		{
			int rid = getResources().getIdentifier(reward, "drawable", getPackageName());
			if(rid > 0)
			{
				try
				{
					imageView1.setImageResource(rid);
				}
				catch(Exception e)
				{
					displayAlertBox("ERROR-407-" + e.getMessage());
				}
				imageView1.setVisibility(View.VISIBLE);
			}
			else
			{
				imageView1.setVisibility(View.INVISIBLE);
				textViewRewardText.setVisibility(View.VISIBLE);
				textViewRewardText.setText(reward);
			}
			
			textViewYouHaveWon.setVisibility(View.VISIBLE);
		}
		else
		{
			textViewYouHaveWon.setVisibility(View.INVISIBLE);
			imageView1.setVisibility(View.INVISIBLE);
		}
	}
	
	/*public void writeQuestionsForRevisions(String subject)
	{
		String fileName = "revision_" + subject + ".txt";
		
		try
		{
			FileOutputStream fos = getApplicationContext().openFileOutput(fileName,Context.MODE_PRIVATE);
			
			OutputStreamWriter out = new OutputStreamWriter(fos);
			BufferedWriter bwriter = new BufferedWriter(out);
			
			int startPos = 0;
			
			for(startPos = 0; startPos < revisionQuestions.size(); startPos++)
			{
				bwriter.write(revisionQuestions.get(startPos));
				bwriter.newLine();
			}
			
			bwriter.close();
			out.close();
			fos.close();
		}
		
		catch(IOException io)
		{
			displayAlertBox("ERROR-406-" + io.getMessage());
		}
		catch(IndexOutOfBoundsException e)
		{
			displayAlertBox("ERROR-405-" + e.getMessage());
		}
	}*/
	
	public void writeScore(String score)
	{
		String fileName = "score.txt";
		
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        String formattedDate = df.format(calendar.getTime());
		
		String text = Util.Subject + ";" + formattedDate + ";" + score + ";" + rewardPoints;
		
		ArrayList<String> existingScores = readScore();
		
		int accumulatedPoints = 0;
		int rp = 0;
		if(existingScores.size() > 0)
		{
			try
			{
				accumulatedPoints = Integer.parseInt(existingScores.get(0));
				existingScores.remove(0);
			}
			catch(NumberFormatException nfe)
			{
				accumulatedPoints = 0;
			}
		}
		
		try
		{
			rp = Integer.parseInt(rewardPoints);
		}
		catch(NumberFormatException nfe)
		{
			rp = 0;
		}
		
		accumulatedPoints = accumulatedPoints + rp;
		
		existingScores.add(0, String.valueOf(accumulatedPoints));
		
		existingScores.add(text);
		
		try
		{
			FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(fileName,Context.MODE_PRIVATE);
			
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			
			int startPos = 0;
			
			if(existingScores.size() > MAX_NO_OF_SCORES)
			{
				startPos = existingScores.size() - MAX_NO_OF_SCORES;
			}
			
			for(; startPos < existingScores.size(); startPos++)
			{
				bufferedWriter.write(existingScores.get(startPos));
				bufferedWriter.newLine();
			}

			bufferedWriter.close();
			outputStreamWriter.close();
			fileOutputStream.close();
		}
		
		catch(IOException io)
		{
			displayAlertBox(io.getMessage());
		}
		catch(IndexOutOfBoundsException e)
		{
			displayAlertBox("ERROR-401-" + e.getMessage());
		}
	}
	
	public void displayAlertBox(String message)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(message);
		alert.setTitle("Test Report");
		alert.setPositiveButton("OK", null);
		alert.setCancelable(true);
		alert.create().show();
	}
	
	public ArrayList<String> readScore()
	{
		ArrayList<String> scoreList = new ArrayList<String>();
		String fileName = "score.txt";

		try
		{
			FileInputStream fis = getApplicationContext().openFileInput(fileName);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
			while ((line = bufferedReader.readLine()) != null) 
			{
				scoreList.add(line);
			}
		}
		catch(FileNotFoundException fnfe)
		{
			return scoreList;
		}
		catch(IOException io)
		{
		}
		return scoreList;
	}
	
	public ArrayList<String> readRevisionQuestions(String subject)
	{
		String fileName = "revision_" + subject + ".txt";
		ArrayList<String> revisionQList = new ArrayList<String>();
		try
		{
			FileInputStream fis = getApplicationContext().openFileInput(fileName);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
			while ((line = bufferedReader.readLine()) != null) 
			{
				revisionQList.add(line);
			}
		}
		catch(IOException io)
		{
			displayAlertBox("ERROR-404-" + io.getMessage());
		}
		return revisionQList;
	}
	
	public void addButtonListener() 
	{
		ImageButton buttonStartRevision = (ImageButton) findViewById(R.id.buttonStartRevision);
		buttonStartRevision.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				openRevision(v);
			}
		});
		ImageButton buttonExit = (ImageButton) findViewById(R.id.buttonExit);
		buttonExit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				finish();
			}
		});
		ImageButton buttonMainMenu = (ImageButton) findViewById(R.id.buttonMainMenu);
		buttonMainMenu.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				finish();
			}
		});
	}
	
	public void openMainMenuPage(View v)
	{
		Intent mainMenuPage = new Intent();
		mainMenuPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".SubjectList");
		startActivity(mainMenuPage);
		finish();
	}

	private void addBannerAd()
	{
		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				//.addTestDevice("750a657019b49e621c42ce9a20c2cc30")
				.build();
		mAdView.loadAd(adRequest);
	}
				
	public void openRevision(View v)
	  {
		  Intent testPage = new Intent();
		  testPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".QuestionPage");
		  Util.filteredQuestions = Util.revisionQuestions;
		  testPage.putExtra("questionCount", Util.revisionQuestions.size());
		  testPage.putExtra("isRevision", "true");
		  testPage.putExtra("isExit", "false");
		  testPage.putExtra("reward", reward);

		  startActivity(testPage);
		  finish();
	  }
}
