package com.m4m.ui;

import android.os.AsyncTask;
import android.widget.TextView;

import com.m4m.BO.GlobalBO;

public class loginTv extends AsyncTask<Void,Void,Void>
{
	private TextView tv;
	
	loginTv(TextView tv)
	{
		this.tv = tv;
	}
	
	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		tv.setText("Welcome to m4M, "+GlobalBO.loginUser);
	}

	@Override
	protected Void doInBackground(Void... params) 
	{
		try
		{
			Thread.sleep(6000);
			
		}
		catch(Exception e){}
		return null;
	}
	

	@Override
	protected void onPostExecute(Void result) 
	{
		super.onPostExecute(result);
		tv.setText("Logged in as "+GlobalBO.loginUser);
	}	
}
