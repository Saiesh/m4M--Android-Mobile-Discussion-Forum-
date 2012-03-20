package com.m4m.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;
public class About extends Activity
{
	TextView version;
	TextView release_date;
	Button about_bttn;
	
	@Override
	protected void onPause()
	{
		super.onPause();
		SharedPreferences m4MPrefs = getSharedPreferences("m4MPrefs", MODE_PRIVATE);  
		SharedPreferences.Editor prefEditor = m4MPrefs.edit();
		prefEditor.putBoolean("rememberMe", GlobalBO.rememberMe);
		
		if(GlobalBO.rememberMe)
		{
			prefEditor.putString("loginUser", GlobalBO.loginUser);  
			prefEditor.putInt("loginID", GlobalBO.loginID);
			prefEditor.putBoolean("isMod", GlobalBO.isMod);
		}
		else
		{
			prefEditor.putString("loginUser", "");  
			prefEditor.putInt("loginID", 0);
			prefEditor.putBoolean("isMod", false);
		}
		prefEditor.putString("IP", GlobalBO.IP);
		prefEditor.putInt("retrievalCount", GlobalBO.retrievalCount);
		prefEditor.commit();  
	}
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		try {
			super.onCreate(savedInstanceState);
	        setContentView(R.layout.about);
	      
	        version =(TextView)findViewById(R.id.version);
	        release_date =(TextView)findViewById(R.id.release_date);
	        about_bttn = (Button)findViewById(R.id.about_bttn);
	        
	        version.setText("Version " + GlobalBO.version);
	        release_date.setText("Released on " + GlobalBO.releasedate);
	        
	        about_bttn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final Dialog sendDialog = new Dialog(About.this);
					
					sendDialog.setContentView(R.layout.aboutdialog);
					sendDialog.setTitle("User Report");
					sendDialog.show();
						
					final EditText about_msg = (EditText)sendDialog.findViewById(R.id.about_msg);				
					Button about_send = (Button)sendDialog.findViewById(R.id.about_send);
					
					about_send.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View v) {
							if(about_msg.getText().toString().compareTo("") == 0)
							{
								Toast.makeText(About.this, "Message body cannot be blank", Toast.LENGTH_SHORT).show();
							}
							else
							{
								class SendMail extends AsyncTask<Void,Void,Boolean>
								{
									ProgressDialog d;
									GMailSender sender;
									
									@Override
									protected void onPreExecute()
									{
										d = ProgressDialog.show(About.this,"Please Wait","Sending report...",true);
										sender = new GMailSender("no.reply.m4m", "m4mhabibul");
									}
									
									@Override
									protected Boolean doInBackground(
											Void... params) {
										// TODO Auto-generated method stub
										try {
											sender.sendMail("User Report",   
					                    		about_msg.getText().toString(),   
					                            "no.reply.m4m@gmail.com",   
					                            "no.reply.m4m@gmail.com");
											return true;
										}
										catch(Exception e) {
											return false;
										}
									}
									
									@Override
									protected void onPostExecute(Boolean result) 
									{
										super.onPostExecute(result);
										d.dismiss();
										if(!result)
										{
											Toast.makeText(About.this, "Error sending report", Toast.LENGTH_SHORT).show();
										}
										else
										{
											 Toast.makeText(About.this, "Report successfully sent", Toast.LENGTH_SHORT).show();
							                    sendDialog.dismiss();
										}
									}
								}
								
								new SendMail().execute();
							}
						}
					});
					
				}
			});
	        
		}
		catch(Exception e) {
			Toast.makeText(About.this, e.toString(), Toast.LENGTH_SHORT).show();
		}
	}
}
