package com.m4m.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;

public class SplashScreen extends Activity 
{
	ImageView img1,img2,img3,img4,img5,img6;
	Animation rotateCW,rotateCCW;
	TextView prefCheck, vCheck, m4MStart;
	ProgressBar bar;
	
	@Override
	public void onBackPressed()
	{
		
	}
	
	private void loadPrefs()
	{
		try
		{
			SharedPreferences m4MPrefs = getSharedPreferences("m4MPrefs", MODE_PRIVATE);
			GlobalBO.rememberMe = m4MPrefs.getBoolean("rememberMe", false);
			GlobalBO.loginUser = m4MPrefs.getString("loginUser", "");
			GlobalBO.loginID = m4MPrefs.getInt("loginID", 0);
			GlobalBO.isMod = m4MPrefs.getBoolean("isMod", false);
			GlobalBO.retrievalCount = m4MPrefs.getInt("retrievalCount", 25);
			GlobalBO.IP = m4MPrefs.getString("IP", "m4m.x10.mx");
		}
		catch(Exception e)
		{
			GlobalBO.rememberMe = false;
			GlobalBO.loginUser = "";
			GlobalBO.loginID = 0;
			GlobalBO.isMod = false;
			GlobalBO.retrievalCount = 25;
			GlobalBO.IP = "m4m.x10.mx";
			Toast.makeText(SplashScreen.this, "Fatal Error: Unable to load preferences. Fail-safe defaults loaded", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
		
		prefCheck = (TextView)findViewById(R.id.prefcheck);
		vCheck = (TextView)findViewById(R.id.vcheck);
		m4MStart = (TextView)findViewById(R.id.startm4m);
		bar=(ProgressBar)findViewById(R.id.bar1);
		img1=(ImageView)findViewById(R.id.img1);
		img2=(ImageView)findViewById(R.id.img2);
		img3=(ImageView)findViewById(R.id.img3);
		
		loadPrefs();
		prefCheck.setText("Loading Preferences... Done.");
		vCheck.setVisibility(0);
		
		rotateCW = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.rotate_indefinitely);
        rotateCW.setInterpolator(new LinearInterpolator());
        rotateCCW = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.rotate_indefinitely_ccw);
        rotateCCW.setInterpolator(new LinearInterpolator());
        
        PackageManager manager=SplashScreen.this.getPackageManager();
        try 
        {
			PackageInfo info = manager.getPackageInfo(SplashScreen.this.getPackageName(),0);
			GlobalBO.version=info.versionName;
		} 
        catch (NameNotFoundException e1) 
		{
			Toast.makeText(SplashScreen.this, "Fatal Error: Unable to determine version",Toast.LENGTH_SHORT).show();
		}
        new check().execute();
	}
	
	class check extends AsyncTask<Void,Void,HttpResponse>
	{
		HttpClient m4MClient = new DefaultHttpClient();
		HttpPost splashPost = new HttpPost("http://" + GlobalBO.IP + "/splash.php");
		
		@Override
		protected void onPreExecute() 
		{
			
			super.onPreExecute();
			
			    img1.startAnimation(rotateCW);
		        img2.startAnimation(rotateCCW);
		        img3.startAnimation(rotateCW);
		        
			try
			{
				List<NameValuePair> splashParams = new ArrayList<NameValuePair>(1);
				splashParams.add(new BasicNameValuePair("version",GlobalBO.version));
				splashPost.setEntity(new UrlEncodedFormEntity(splashParams));
			}
			catch(Exception e)
			{
				Toast.makeText(SplashScreen.this, "Error starting m4M", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		
		@Override
		protected HttpResponse doInBackground(Void... params) 
		{
			
			try
			{
				m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
				return m4MClient.execute(splashPost);
			}
			catch(Exception e)
			{
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(HttpResponse result) 
		{
			try
			{
					img1.setAnimation(null);
					img2.setAnimation(null);
					img3.setAnimation(null);
												        	        
					if(result==null)
					{
						Toast.makeText(SplashScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
						finish();
					}
					else
					{
						vCheck.setText("Verifying Version... Done.");
						m4MStart.setVisibility(0);
						if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
						{
							try 
							{
								int solvedResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
								
				            	if(solvedResponse == -2)
								{
				            		bar.setVisibility(8);
				            		//loadText.setText("Aborted: Version Error");
				            		if(GlobalBO.loginID != 0)
									{
										Toast.makeText(SplashScreen.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
										GlobalBO.loginID = 0;
										GlobalBO.loginUser = "";
										GlobalBO.isMod = false;
										GlobalBO.rememberMe = false;
									}
				            		final AlertDialog.Builder box = new AlertDialog.Builder(SplashScreen.this);
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
									        	SplashScreen.this.finish();
									        }
									        catch(Exception e)
									        {
									        	Toast.makeText(SplashScreen.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
									        	SplashScreen.this.finish();
									        }
										}
									})
									.setNegativeButton("Exit",new DialogInterface.OnClickListener()
									{
										
										public void onClick(DialogInterface dialog, int which) 
										{
											SplashScreen.this.finish();
										}
									})
									.setCancelable(false)
									.create()
									.show();
								}
				            	else 
				            	{
				            		m4MStart.setText("Starting m4M... Done.");
				            		GlobalBO.seed = "ke_d9b+ow11s*a27";
				            		Intent load = new Intent(SplashScreen.this,MainScreen.class);
				            		startActivity(load);
				            		SplashScreen.this.finish();
				            	}
							}
							catch(Exception e)
							{
								Toast.makeText(SplashScreen.this, "Error starting m4M",Toast.LENGTH_SHORT).show();
								finish();
							}
						}
						else
						{
							Toast.makeText(SplashScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
							finish();
						}
												
					}
			}
			catch(Exception e)
			{
				Toast.makeText(SplashScreen.this,"Fatal Error: "+e.toString(), Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}
