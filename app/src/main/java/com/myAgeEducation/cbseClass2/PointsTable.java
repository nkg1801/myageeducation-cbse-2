package com.myAgeEducation.cbseClass2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;

public class PointsTable extends Activity {
	
	private GridView gridView;
	
	 @Override
	  public void onCreate(Bundle savedInstanceState) 
	  {
		 try{
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.points_table);
		 populateGrid(this);
		 addButtonListener();
		 }
		 catch(Exception e)
		 {
			 displayAlertBox(e.getMessage(),"Error");
		 }
	  }
	 
	 public void displayAlertBox(String message, String title)
	 {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(message);
		alert.setTitle(title);
		alert.setPositiveButton("OK", null);
		alert.setCancelable(true);
		alert.create().show();
	 }
	 
	 @Override
		public boolean onCreateOptionsMenu(Menu menu)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.points_table, menu);
			return true;
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			switch(item.getItemId())
			{
			//case R.id.menu_add_reward_points:
			//	openPointSettingsPage();
				//return true;
			//case R.id.menu_clear_all_reward_points:
			//	clearRewardPoints();
				//gridView.setAdapter(null);
			//	return true;
			//case R.id.menu_reward_settings:
				//openRewardSettingsPage();
			//	return true;
			//case R.id.menu_exit:
			//	finish();
			//	return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		
		public void addButtonListener() 
		{
			ImageButton buttonMainMenu = (ImageButton)findViewById(R.id.buttonMainMenu);
			buttonMainMenu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) 
				{
					finish();
				}
			});
		 }
		
		public void clearRewardPoints()
		{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Clear all reward points?");
	 
			alertDialogBuilder
					.setMessage("Are you sure to clear all reward points!")
					.setCancelable(false)
					.setPositiveButton("Yes",new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int id) 
						{
							File dir = getFilesDir();
							String fileName = "points.txt";
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
		
		public void openPointSettingsPage()
		{
			Intent subPage = new Intent();
			subPage.setClassName(Util.PACKAGE_NAME, Util.PACKAGE_NAME + ".PointSettings");
			startActivity(subPage);
			finish();
		}
	 
	 public void populateGrid(final Context context)
	 {
		 ArrayList<String> pointsTable = readPointsTable();
			
		 ArrayList<String> list = new ArrayList<String>();
			
		 //if(pointsTable.size() == 0)
		 //{
				// display message - "No Score History available"
		 //}
		//else
		{
			String[] token;
			list.add("No");
			list.add("Name");
			list.add("Subj");
			list.add("Chapters");
			list.add("Q's");
			list.add("Score");
			list.add("Points");
			
			String slNo = "";
			
			for(int i = 0; i < pointsTable.size(); i++)
			{
				try
				{
					token = pointsTable.get(i).split(";");
					slNo = String.valueOf(i + 1);
					list.add(slNo);
					list.add(token[0]);
					list.add(token[1]);
					list.add(token[2]);
					list.add(token[3]);
					list.add(token[4]);
					list.add(token[5]);
				}
				catch(IndexOutOfBoundsException iobe)
				{
				}
			}
		}
			
		gridView = (GridView) findViewById(R.id.gridView1);
	
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				R.layout.customstyle2, list);
 
		gridView.setAdapter(adapter);
	 }
	 
	 public ArrayList<String> readPointsTable()
	 {
		 ArrayList<String> pointsTable = new ArrayList<String>();
		 
		 try
		 {
			String fileName = "points.txt";
			FileInputStream fis = getApplicationContext().openFileInput(fileName);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
			while ((line = bufferedReader.readLine()) != null) 
			{
				pointsTable.add(line);
			}
			
			bufferedReader.close();
			isr.close();
			fis.close();
		 }
		catch(IOException io){}
		 
		return pointsTable;
	 }
}
