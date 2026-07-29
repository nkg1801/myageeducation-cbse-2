package com.myAgeEducation.cbseClass2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ScoreHistory extends Activity 
{
	private GridView _gridView;

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_history);

		if(Util.isFreeApp) {
			addBannerAd();
		}
		
		try
		{
		
		ArrayList<String> scoreHistory = readScore();
		
		ArrayList<String> list = new ArrayList<String>();
		
		if(scoreHistory.size() > 0)
		{
			String[] token;
			list.add("Subject");
			list.add("Time");
			list.add("Score");

			for(int i = scoreHistory.size()-1; i > 0; i--)
			{
				token = scoreHistory.get(i).split(";");
				if(token[0].equalsIgnoreCase("cs"))
				{
					list.add("Computer Science");
				}
				else if(token[0].equalsIgnoreCase("evs"))
				{
					list.add("EVS\\Science");
				}
				else
				{
					list.add(token[0]);
				}
				list.add(token[1]);
				list.add(token[2]);
			}
		}
		
		_gridView = (GridView) findViewById(R.id.gridView1);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.customstyle, list);
		_gridView.setAdapter(adapter);

		}
		catch(Exception e)
		{
			displayAlertBox("ERROR-701-" + e.getMessage());
		}
	}

	private void addBannerAd()
	{
		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				//.addTestDevice("750a657019b49e621c42ce9a20c2cc30")
				.build();
		mAdView.loadAd(adRequest);
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
		try
		{
			FileInputStream fis = getApplicationContext().openFileInput("score.txt");
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
			while ((line = bufferedReader.readLine()) != null) 
			{
				scoreList.add(line);
			}
		}
		catch(IOException io){}
		return scoreList;
	}
}
