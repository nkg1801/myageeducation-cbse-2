package com.myAgeEducation.cbseClass2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

public class RewardsTable extends Activity {
	
	private GridView gridView;
	
	 @Override
	  public void onCreate(Bundle savedInstanceState) 
	  {
		 super.onCreate(savedInstanceState);
		 setContentView(com.myAgeEducation.cbseClass2.R.layout.rewards_table);
		 populateGrid(this);
		 addButtonListener();
	  }
	 
	 @Override
		public boolean onCreateOptionsMenu(Menu menu)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.rewards_settings, menu);
			return true;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			int id = item.getItemId();
			if (id == R.id.menu_clear_rewards) {
				clearRewards();
				return true;
			} else {
				return super.onOptionsItemSelected(item);
			}
		}
	 
	 public void populateGrid(final Context context)
	 {
		 ArrayList<String> rewardsTable = readRewardsTable();
			
		 ArrayList<String> list = new ArrayList<String>();
			
		String[] token;
		list.add("Sl No");
		list.add("Reward Name");
		list.add("Points");
		list.add("Reward");
		
		String slNo = "";
		
		for(int i = 0; i < rewardsTable.size(); i++)
		{
			token = rewardsTable.get(i).split(";");
			slNo = String.valueOf(i + 1);
			list.add(slNo);
			list.add(token[0]); // reward Name
			list.add(token[1]); // for points
			list.add(token[2]); // reward
		}
			
		gridView = (GridView) findViewById(R.id.gridViewRewardsTable);
	
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				R.layout.customstyle, list);
 
		gridView.setAdapter(adapter);
	 }
	 
	 public ArrayList<String> readRewardsTable()
	 {
		 ArrayList<String> rewardsTable = new ArrayList<String>();
		 
		 try
		 {
			FileInputStream fis = getApplicationContext().openFileInput("rewards.txt");
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
			while ((line = bufferedReader.readLine()) != null) 
			{
				rewardsTable.add(line);
			}
		 }
		catch(IOException io){}
		 
		 return rewardsTable;
	 }
	 
	 public void clearRewards()
	 {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Clear all rewards?");
 
		alertDialogBuilder
				.setMessage("Are you sure to clear all rewards!")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int id) 
					{
						File dir = getFilesDir();
						String fileName = "rewards.txt";
						File file = new File(dir, fileName);
						if(file.delete())
						{
							populateGrid(getApplicationContext());
						}
					}
				})
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
 
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	 }
	 
	 public void addButtonListener()
	 {
		 Button button = (Button) findViewById(R.id.buttonAddReward);

			button.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					addReward();
				}
			});

	 }
	 
	 public void displayAlert(String message, String title)
	 {
		 AlertDialog.Builder alert = new AlertDialog.Builder(this);
		 alert.setMessage(message);
		 alert.setTitle(title);
		 alert.setPositiveButton("OK", null);
		 alert.setCancelable(true);
		 alert.create().show();
	 }
	 
	 public void addReward()
	 {
		 String rewardName = "";
		 String forPoints = "";
		 String reward = "";
		 String pictureName = "";
		 
		 EditText editTextRewardName = (EditText)findViewById(R.id.editTextRewardName);
		 EditText editTextForPoints = (EditText)findViewById(R.id.editText1);
		 EditText editTextReward = (EditText)findViewById(R.id.editText2);
		 
		 rewardName = editTextRewardName.getText().toString();
		 forPoints = editTextForPoints.getText().toString();
		 reward = editTextReward.getText().toString();
		 pictureName = "picNameWithPath";
		 
		 if(rewardName.trim().isEmpty())
		 {
			 displayAlert("Reward Name cann't be empty","Error");
			 editTextRewardName.requestFocus();
			 return;
		 }
		 else if(forPoints.trim().isEmpty())
		 {
			 displayAlert("Points cann't be empty","Error");
			 editTextForPoints.requestFocus();
			 return;
		 }
		 else if(reward.trim().isEmpty())
		 {
			 displayAlert("Reward cann't be empty","Error");
			 editTextReward.requestFocus();
			 return;
		 }
		 
		 String rewardString = rewardName + ";" + forPoints + ";" + reward + ";" + pictureName;
			
		 String fileName = "rewards.txt";
			
		try
		{
			FileOutputStream fos = getApplicationContext().openFileOutput(fileName,Context.MODE_APPEND);
			
			OutputStreamWriter out = new OutputStreamWriter(fos);
			BufferedWriter bwriter = new BufferedWriter(out);
			
			bwriter.write(rewardString);
			bwriter.newLine();
			
			bwriter.close();
			out.close();
			fos.close();
		}
		
		catch(IOException io)
		{
			displayAlert("ERROR-701-" + io.getMessage(),"Error");
		}
		
		 editTextRewardName.setText("");
		 editTextForPoints.setText("");
		 editTextReward.setText("");
		
		populateGrid(this);
	 }
}
