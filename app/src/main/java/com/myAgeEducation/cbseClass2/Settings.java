package com.myAgeEducation.cbseClass2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Settings extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		addButtonListener();
	}
	
	public void addButtonListener()
	{
		ImageButton mainMenu = (ImageButton) findViewById(R.id.buttonMainMenu);

		mainMenu.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) 
		{
			finish();
		}
	 });
	}
}
