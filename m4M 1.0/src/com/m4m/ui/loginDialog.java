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
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;

public class loginDialog implements View.OnClickListener
{
	Context mContext;
	postProcess ppObj;
	
	public interface postProcess
	{
		public void postExecute(int errorState);
		public void userRegistration();
		public void forgotPassword();
	}
	
	public loginDialog(Context mContext, postProcess ppObj)
	{
		this.mContext = mContext;
		this.ppObj = ppObj;
	}
	
	public void onClick(View arg0) 
	{
		if(GlobalBO.loginID == 0)
		{
			final Dialog dialog = new Dialog(mContext);
			
			dialog.setContentView(R.layout.login);
			dialog.setTitle("User Login");
			dialog.show();
				
			final EditText userid = (EditText)dialog.findViewById(R.id.user_id);
			final EditText passwrd = (EditText)dialog.findViewById(R.id.password);
			final CheckBox checkbox = (CheckBox)dialog.findViewById(R.id.RememberMe);
			
			Button signin = (Button)dialog.findViewById(R.id.sign_in);
			TextView signup = (TextView)dialog.findViewById(R.id.new_user);
			TextView forgotpass = (TextView)dialog.findViewById(R.id.forgot_pass);
			
			userid.setText("");
			passwrd.setText("");
			
			signup.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					ppObj.userRegistration();			
				}
			});
			
			forgotpass.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					ppObj.forgotPassword();			
				}
			});
			
			signin.setOnClickListener(new View.OnClickListener()
			{
				class loginTask extends AsyncTask<Void, Void, String>
				{
					ProgressDialog d;
					HttpPost loginPost = new HttpPost("http://" + GlobalBO.IP + "/login.php");
					HttpClient m4MClient = new DefaultHttpClient();
					
					@Override
					protected void onPreExecute()
					{
						super.onPreExecute();
						m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
						m4MClient.getParams().setIntParameter("http.socket.timeout",5000);
						d = ProgressDialog.show(mContext, "Please Wait", "Logging in...", true);
						try
						{
							List<NameValuePair> loginParams = new ArrayList<NameValuePair>(3);
							loginParams.add(new BasicNameValuePair("username", userid.getText().toString().toLowerCase()));
							loginParams.add(new BasicNameValuePair("password", m4MCrypt.encrypt(GlobalBO.seed, passwrd.getText().toString()).toString()));
							loginParams.add(new BasicNameValuePair("version", GlobalBO.version));
							loginPost.setEntity(new UrlEncodedFormEntity(loginParams));		
						}
						catch(Exception e)
						{
							Toast.makeText(mContext, "Error logging into Server", Toast.LENGTH_SHORT).show();
						}
					}
					
					@Override
					protected String doInBackground(Void... params) 
					{
						try
						{
							String details = getDetails();
							if(details.equals("0"))
								return "Invalid Username or Password";
							else if(details.equals("-2"))
								return "mismatch";
							JSONObject jObject = new JSONObject(details);
							if(jObject.getString("exists").compareTo("0") == 0)
								return "Account was terminated by a Moderator";
							else
							{
								GlobalBO.loginID = Integer.parseInt(jObject.getString("iduser"));
								GlobalBO.loginUser = userid.getText().toString().toLowerCase();
								if(jObject.getString("ismod").compareTo("1") == 0)
								{
									return "Moderator";
								}
								else
									GlobalBO.isMod = false;
							}
						}
						catch(Exception e)
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
						if(result.equals("-2"))
						{
							dialog.dismiss();
							ppObj.postExecute(-2);
						}
							
						else if(result.compareTo("Moderator") == 0)
						{
							final Dialog modDialog = new Dialog(mContext);
							
							modDialog.setContentView(R.layout.moderator);
							modDialog.setTitle("Moderator Verification");
							modDialog.show();
								
							final EditText modKey = (EditText)modDialog.findViewById(R.id.mod_key);				
							final Button unlock = (Button)modDialog.findViewById(R.id.mod_unlock);
											
							try
							{
									unlock.setOnClickListener(new View.OnClickListener() 
									{
										
										public void onClick(View v) 
										{
											if(modKey.getText().toString().equals(""))
											{
												Toast.makeText(mContext, "Key field cannot be blank", Toast.LENGTH_SHORT).show();
											}
											else if(Integer.parseInt(modKey.getText().toString()) == GlobalBO.loginID)
											{
												if(checkbox.isChecked()==true)
													GlobalBO.rememberMe=true;
												GlobalBO.isMod = true;
												modDialog.dismiss();
												dialog.dismiss();
												ppObj.postExecute(1);
											}
											else
											{
												Toast.makeText(mContext, "Moderator Key Mismatch", Toast.LENGTH_SHORT).show();
											}
										}
									});
									modDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
										
										@Override
										public void onCancel(DialogInterface dialog) {
											modDialog.dismiss();
											GlobalBO.loginID = 0;
											GlobalBO.loginUser = "";
											GlobalBO.rememberMe = false;
											GlobalBO.isMod = false;
										}
									});
							}
							catch(Exception e)
							{
								Toast.makeText(mContext, "Could not connect to Server",Toast.LENGTH_SHORT).show();
								checkbox.setChecked(false);
								GlobalBO.loginID = 0;
								GlobalBO.loginUser = "";
								GlobalBO.rememberMe = false;
								GlobalBO.isMod = false;
								modDialog.dismiss();
							}
						}
						else if(!result.equals("ok"))
						{
							passwrd.setText("");
							checkbox.setChecked(false);
							Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
						}
						else
						{
							if(checkbox.isChecked()==true)
								GlobalBO.rememberMe=true;
							dialog.dismiss();
							ppObj.postExecute(1);
						}
					}	
					public String getDetails() throws Exception
					{
						StringBuilder builder = new StringBuilder();
						try
						{
							HttpResponse loginResp = m4MClient.execute(loginPost);	
							if(loginResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
							{
								HttpEntity entity = loginResp.getEntity();
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
				
				public void onClick(View arg0) 
				{
					if(userid.getText().toString().equals("") || passwrd.getText().toString().equals(""))
						Toast.makeText(mContext, "Username & Password required", Toast.LENGTH_SHORT).show();
					else
						new loginTask().execute();
				}
			});
		}
		else
		{
			final AlertDialog.Builder box = new AlertDialog.Builder(mContext);
			box.setTitle("Logout")
			.setMessage("Are you sure you want to logout from Mobile Forum?")
			.setPositiveButton("Yes",new DialogInterface.OnClickListener()
			{		
				public void onClick(DialogInterface dialog, int which) 
				{
					String tempUser = GlobalBO.loginUser;
					GlobalBO.loginID = 0;
		    		GlobalBO.loginUser = "";
		    		GlobalBO.isMod = false;
		    		GlobalBO.rememberMe = false;
		    		Toast.makeText(mContext, "Successfully logged out as " + tempUser, Toast.LENGTH_SHORT).show();
		    		ppObj.postExecute(1);
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
	}
}
