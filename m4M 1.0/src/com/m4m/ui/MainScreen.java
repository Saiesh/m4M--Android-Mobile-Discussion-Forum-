package com.m4m.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;
import com.m4m.BO.SettingsOptions;

public class MainScreen extends Activity 
{
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
	
    private Button login;
	private Button glance;
	private Button settings;
	private Button exit;
	private Button about;
	private Button community;    
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first);
        login =(Button)findViewById(R.id.login_bttn);
        glance=(Button)findViewById(R.id.glance_bttn);
        community=(Button)findViewById(R.id.community_bttn);
        settings=(Button)findViewById(R.id.settings_bttn);
        exit=(Button)findViewById(R.id.exit_bttn);
        about=(Button)findViewById(R.id.about_bttn);
        
        if(GlobalBO.loginID!=0)
        	login.setText("LOGOUT");
        
        community.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					if(GlobalBO.loginID == 0)
						Toast.makeText(MainScreen.this, "You must be logged in to access m4M Community",Toast.LENGTH_SHORT).show();
					else
					{
						Intent i = new Intent(MainScreen.this, CommSplash.class);
						startActivity(i);
						MainScreen.this.finish();
					}
				}
				catch(Exception e)
				{
					Toast.makeText(MainScreen.this,e.toString(),Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        about.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try
				{
					Intent i = new Intent(MainScreen.this, About.class);
					startActivity(i);
				}
				catch(Exception e)
				{
					Toast.makeText(MainScreen.this,e.toString(),Toast.LENGTH_SHORT).show();
				}
			}
		});

        glance.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View arg0) 
			{
				try
				{
					Intent i = new Intent(MainScreen.this, QuestionsScreen.class);
					startActivity(i);
					MainScreen.this.finish();
				}
				catch(Exception e)
				{
					Toast.makeText(MainScreen.this, e.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        settings.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0) 
			{
				try {
				    GlobalBO.settings.clear();
			        GlobalBO.settings.add(new SettingsOptions("Remote Server Address", 
			        		"Use this option to change the address of the remote server, only when directed to do so by the developers."));
			        GlobalBO.settings.add(new SettingsOptions("Question Retrieval Limit", 
			        		"Use this option to limit the number of questions downloaded at each glance. Set this value depending on bandwidth constraints."));
			        GlobalBO.settings.add(new SettingsOptions("Change Password", 
	        		"Use this option to change your m4M account password. You will be required to provide your existing password."));
					Intent i = new Intent(MainScreen.this,SettingsScreen.class);
					startActivity(i);
					MainScreen.this.finish();
				}
				catch(Exception e)
				{
					Toast.makeText(MainScreen.this, e.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
        login.setOnClickListener(new loginDialog(MainScreen.this, new loginDialog.postProcess() 
        {
			public void postExecute(int errorState) 
			{
				if(errorState == -2)
				{
					if(GlobalBO.loginID != 0)
					{
						Toast.makeText(MainScreen.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
						GlobalBO.loginID = 0;
						GlobalBO.loginUser = "";
						GlobalBO.isMod = false;
						GlobalBO.rememberMe = false;
					}
					final AlertDialog.Builder box = new AlertDialog.Builder(MainScreen.this);
					box.setTitle("Upgrade m4M")
					.setMessage("A new version of m4M has been released, kindly upgrade to continue using m4M")
					.setPositiveButton("Upgrade Now",new DialogInterface.OnClickListener()
					{
						
						public void onClick(DialogInterface dialog, int which) 
						{
							try
					        {
								Intent intent = new Intent(Intent.ACTION_VIEW);
					        	intent.setData(Uri.parse("market://details?id=com.m4m.ui"));
					        	startActivity(intent);
					        	MainScreen.this.finish();
					        }
					        catch(Exception e)
					        {
					        	Toast.makeText(MainScreen.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
					        	MainScreen.this.finish();
					        }
						}
					})
					.setNegativeButton("Exit",new DialogInterface.OnClickListener()
					{
						
						public void onClick(DialogInterface dialog, int which) 
						{
							MainScreen.this.finish();
						}
					})
					.setCancelable(false)
					.create()
					.show();
				}
				else
				{
					Intent i = new Intent(MainScreen.this, MainScreen.class);
					startActivity(i);
					MainScreen.this.finish();
				}
			}
			
			public void userRegistration()
			{
				GlobalBO.mContext = MainScreen.this;
				Intent i = new Intent(MainScreen.this, RegistrationScreen.class);
				startActivity(i);
				MainScreen.this.finish();
			}
			
			public void forgotPassword()
			{
				GlobalBO.mContext = MainScreen.this;
				Intent i = new Intent(MainScreen.this, ForgotPassword.class);
				startActivity(i);
				MainScreen.this.finish();
			}
		}));
        
        exit.setOnClickListener(new View.OnClickListener()
        {
			
			public void onClick(View arg0) 
			{
				final AlertDialog.Builder box = new AlertDialog.Builder(MainScreen.this);
				box.setTitle("Exit Mobile Forum")
				.setMessage("Are you sure you want to close Mobile Forum?")
				.setPositiveButton("Yes",new DialogInterface.OnClickListener()
				{
					
					public void onClick(DialogInterface dialog, int which) 
					{
						if(GlobalBO.rememberMe==false)
						{
							GlobalBO.loginID = 0;
				    		GlobalBO.loginUser = "";
				    		GlobalBO.isMod = false;
						}
						Toast.makeText(MainScreen.this,"Thank You for using m4M",Toast.LENGTH_SHORT).show();
						finish();
					}
				})
				.setNegativeButton("No",new DialogInterface.OnClickListener()
				{
					
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.create()
				.show();
				
			}
		});
    }

    @Override
	public void onBackPressed() 
    {
		
	}
}