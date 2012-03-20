package com.m4m.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;

public class ForgotPassword extends Activity 
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
	
	Button btnEmail;
	Button btnSms;
	Button btnVerify;
	TextView TPhone;
	TextView TEmail;
	EditText txtUsername, txtMobile;
	String Username, Email, Phone, Password;
	int listPos;
	int QuestionId;
	
	private void UpdateBox()
	{
		if(GlobalBO.loginID != 0)
		{
			Toast.makeText(ForgotPassword.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
			GlobalBO.loginID = 0;
			GlobalBO.loginUser = "";
			GlobalBO.isMod = false;
			GlobalBO.rememberMe = false;
		}
		final AlertDialog.Builder box = new AlertDialog.Builder(ForgotPassword.this);
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
		        	ForgotPassword.this.finish();
		        }
		        catch(Exception e)
		        {
		        	Toast.makeText(ForgotPassword.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
		        	ForgotPassword.this.finish();
		        }
			}
		})
		.setNegativeButton("Exit",new DialogInterface.OnClickListener()
		{
			
			public void onClick(DialogInterface dialog, int which) 
			{
				ForgotPassword.this.finish();
			}
		})
		.setCancelable(false)
		.create()
		.show();
	}
	
	interface MsgSys
	{
		public void msgFunc();
	}
	
	class UpdatePassword extends AsyncTask<MsgSys,Void,HttpResponse>
	{
		ProgressDialog d;
		HttpClient m4MClient = new DefaultHttpClient();
		HttpPost updatePost = new HttpPost("http://" + GlobalBO.IP + "/pwdmod.php");
		MsgSys msgObj;
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			m4MClient.getParams().setIntParameter("http.connection.timeout", 5000);
			d = ProgressDialog.show(ForgotPassword.this, "Please Wait", "Updating Password...", true);
	    	try
			{
				List<NameValuePair> updateParams = new ArrayList<NameValuePair>(3);
				updateParams.add(new BasicNameValuePair("username", txtUsername.getText().toString().toLowerCase()));
				updateParams.add(new BasicNameValuePair("newpwd", m4MCrypt.encrypt(GlobalBO.seed, Password).toString()));
				updateParams.add(new BasicNameValuePair("version", GlobalBO.version));
				updatePost.setEntity(new UrlEncodedFormEntity(updateParams));		
			}
			catch(Exception e)
			{
				Toast.makeText(ForgotPassword.this, "Password Update Failure", Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		protected HttpResponse doInBackground(MsgSys... arg0) 
		{
			this.msgObj = arg0[0];
			try
			{
				return m4MClient.execute(updatePost);
			}
			catch(Exception e)
			{
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(HttpResponse result) 
		{
			super.onPostExecute(result);
			d.dismiss();
			if(result==null)
			{
				Toast.makeText(ForgotPassword.this, "Password Update Failure", Toast.LENGTH_SHORT).show();
			}
			else
			{
				if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					try 
					{
						int updateResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
						if(updateResponse == -2)
						{
							UpdateBox();
						}
						else if(updateResponse == -1)
						{
							if(GlobalBO.loginID == 0)
								Toast.makeText(ForgotPassword.this, "Account was terminated by a Moderator", Toast.LENGTH_SHORT).show();
							else
							{
								Toast.makeText(ForgotPassword.this, "Account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
								GlobalBO.loginID = 0;
								GlobalBO.loginUser = "";
								GlobalBO.isMod = false;
								GlobalBO.rememberMe = false;
							}
							cleanup();
						}
						else if(updateResponse == 0)
						{
							Toast.makeText(ForgotPassword.this, "Password Update Failure", Toast.LENGTH_SHORT).show();
						}
		            	else
		            	{
		            		msgObj.msgFunc();
		            	}
					}
					catch(Exception e)
					{
						Toast.makeText(ForgotPassword.this, "Password Update Failure", Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					Toast.makeText(ForgotPassword.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
				}						
			}
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	try {
    		super.onCreate(savedInstanceState);
            setContentView(R.layout.forgotpassword);
            if(GlobalBO.mContext.getClass() == ViewQuestion.class)
            {
            	Bundle b = getIntent().getExtras();
        		listPos = b.getInt("position");
        		QuestionId = b.getInt("qid");
            }
            
            btnVerify = (Button)findViewById(R.id.Verify);
            btnEmail = (Button)findViewById(R.id.Email);
            btnSms = (Button)findViewById(R.id.Sms);
            TPhone = (TextView)findViewById(R.id.TPhone);
            TEmail = (TextView)findViewById(R.id.TEmail);
            txtUsername = (EditText)findViewById(R.id.Username);
            txtMobile = (EditText)findViewById(R.id.Mobile);
            
            btnEmail.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					
					new UpdatePassword().execute(new MsgSys(){
						public void msgFunc()
						{
							class SendMail extends AsyncTask<Void,Void,Boolean>
							{
								ProgressDialog d;
								GMailSender sender;
								
								@Override
								protected void onPreExecute()
								{
									d = ProgressDialog.show(ForgotPassword.this,"Please Wait","Sending E-Mail...",true);
									sender = new GMailSender("no.reply.m4m", "m4mhabibul");
								}
								
								@Override
								protected Boolean doInBackground(Void... params) {
									try {
										sender.sendMail("m4M Password Recovery",   
					                    		txtUsername.getText().toString() + ",\n\n" 
					                    		+ "Your temporary password is: " 
					                    		+ Password + ". You can change it in m4M Settings.\n\n"
					                    		+ "Mobile Forum",   
					                            "no.reply.m4m@gmail.com",   
					                            Email); 
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
										Toast.makeText(ForgotPassword.this, "Error sending E-Mail", Toast.LENGTH_SHORT).show(); 
									}
									else
									{
										Toast.makeText(ForgotPassword.this, "An E-Mail has been sent to you containing your password", Toast.LENGTH_SHORT).show();
										cleanup();
									}
								}
							}
							
							new SendMail().execute();
						}
					});
				}
			});
            
            btnSms.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) 
				{
					new UpdatePassword().execute(new MsgSys(){
						public void msgFunc()
						{
							try {
								SmsManager sms = SmsManager.getDefault();
								PendingIntent sentPI = PendingIntent.getBroadcast(ForgotPassword.this, 0, new Intent("SMS_SENT"), 0);
						        sms.sendTextMessage(Phone, null, txtUsername.getText().toString() + ", your temporary password is:  " + Password + ". You can change it in m4M Settings.", sentPI, null);
						        
						     
						            //---when the SMS has been sent---
						            registerReceiver(new BroadcastReceiver(){
						                @Override
						                public void onReceive(Context arg0, Intent arg1) {
						                    switch (getResultCode())
						                    {
						                        case Activity.RESULT_OK:
						                        	Toast.makeText(ForgotPassword.this, "An SMS has been sent to you containing your password", Toast.LENGTH_SHORT).show();
						                        	cleanup();
						                            break;
						                        case SmsManager.RESULT_ERROR_NO_SERVICE:
						                        	Toast.makeText(ForgotPassword.this, "No network found", Toast.LENGTH_SHORT).show();
						                            break;
						                        default:
						                        	Toast.makeText(ForgotPassword.this, "Unable to send SMS", Toast.LENGTH_SHORT).show();
						                        	break;
						                    }
						                }
						            }, new IntentFilter("SMS_SENT"));
						        
							}
							catch(Exception e)
							{
								Toast.makeText(ForgotPassword.this, e.toString(), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			});
            
            btnVerify.setOnClickListener(new View.OnClickListener() {
            	
				public void onClick(View v) {
					if(txtUsername.getText().length() == 0)
						Toast.makeText(ForgotPassword.this, "Username field cannot be blank", Toast.LENGTH_SHORT).show();
					else if(txtMobile.getText().length() == 0)
						Toast.makeText(ForgotPassword.this, "Mobile field cannot be blank", Toast.LENGTH_SHORT).show();
					else
						new VerifyMembership().execute();
				}
			});
    	}
    	catch(Exception e) 
    	{
    		Toast.makeText(ForgotPassword.this, e.toString(), Toast.LENGTH_SHORT).show();
    	}        
    }
    class VerifyMembership extends AsyncTask<Void, Void, String>
	{
		ProgressDialog d;
		HttpClient m4MClient = new DefaultHttpClient();
		HttpPost verifyPost = new HttpPost("http://" + GlobalBO.IP + "/forgotpwd.php");
								
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			m4MClient.getParams().setIntParameter("http.connection.timeout", 5000);
			d = ProgressDialog.show(ForgotPassword.this, "Please Wait", "Verifying membership...", true);
	    	try
			{
				List<NameValuePair> verifyParams = new ArrayList<NameValuePair>(3);
				verifyParams.add(new BasicNameValuePair("username", txtUsername.getText().toString().toLowerCase()));
				verifyParams.add(new BasicNameValuePair("mobile", txtMobile.getText().toString()));
				verifyParams.add(new BasicNameValuePair("version", GlobalBO.version));
				verifyPost.setEntity(new UrlEncodedFormEntity(verifyParams));		
			}
			catch(Exception e)
			{
				Toast.makeText(ForgotPassword.this, "Verification failed", Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		protected String doInBackground(Void... params) 
		{	
			try 
			{
				String details = getDetails();
				if(details.compareTo("0") == 0)
				{
					return "Invalid Username or Mobile Number";
				}
				else if(details.compareTo("-2") == 0)
				{
					return "mismatch";
				}
				JSONObject jObject = new JSONObject(details);
				if(jObject.getString("exists").equals("0"))
					return "terminated";
				Email = jObject.getString("email");
				Phone = jObject.getString("phone");
				Password = jObject.getString("password").substring(0, 8);
			} 
			catch (Exception e)
			{
				return "Could not connect to Server";
			}
			return "ok";
		}
		
		@Override
		protected void onPostExecute(String result) 
		{
			super.onPostExecute(result);
			d.dismiss();
			
			if(result.equals("ok"))
			{
				try
				{
					btnEmail.setEnabled(true);
					btnSms.setEnabled(true);
					btnVerify.setEnabled(false);
					txtUsername.setEnabled(false);
					txtMobile.setEnabled(false);
					TEmail.setText("E-Mail: " + Email);
					TPhone.setText("Phone: " + Phone);
				}
				catch(Exception e)
				{
					Toast.makeText(ForgotPassword.this, "Verification failed", Toast.LENGTH_SHORT).show();
				}
			}
			else if(result.equals("mismatch"))
			{
				UpdateBox();
			}
			else if(result.equals("terminated"))
			{
				if(GlobalBO.loginID == 0)
					Toast.makeText(ForgotPassword.this, "Account was terminated by a Moderator", Toast.LENGTH_SHORT).show();
				else
				{
					Toast.makeText(ForgotPassword.this, "Account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
					GlobalBO.loginID = 0;
	        		GlobalBO.loginUser = "";
	        		GlobalBO.isMod = false;
	        		GlobalBO.rememberMe = false;
				}
				cleanup();
			}
			else
			{
				Toast.makeText(ForgotPassword.this, result, Toast.LENGTH_SHORT).show();
			}
		}	
		public String getDetails() throws Exception
		{
			StringBuilder builder = new StringBuilder();
			try
			{
				HttpResponse verifyResp = m4MClient.execute(verifyPost);	
				if(verifyResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					HttpEntity entity = verifyResp.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) 
					{
						builder.append(line);
					}
				}
			}
			catch(Exception e)
			{
				throw e;
			}		
			return builder.toString();
	    }
	}
    
    private void cleanup()
    {
    	Intent i = new Intent(ForgotPassword.this, GlobalBO.mContext.getClass());
    	if(GlobalBO.mContext.getClass() == ViewQuestion.class)
    	{
    		i.putExtra("position",listPos);
			i.putExtra("qid",QuestionId);
    	}
    	startActivity(i);
    	ForgotPassword.this.finish();
    }
    
    public void onBackPressed()
    {
    	cleanup();
    }
}
