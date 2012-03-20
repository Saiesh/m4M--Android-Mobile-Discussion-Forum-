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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;

public class RegistrationScreen extends Activity 
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
	
	Button Register;
	Button Reset;
	EditText Name;
	EditText Password;
	EditText EmailID;
	EditText PhoneNo;
	EditText CPassword;
	EditText Username;
	int listPos;
	int QuestionId;
	CheckBox Teacher;
	String dPassword, dEmailID, dPhoneNo, dCPassword, dUsername, dName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	try {
    		super.onCreate(savedInstanceState);
            setContentView(R.layout.registration);
            
            if(GlobalBO.mContext.getClass() == ViewQuestion.class)
            {
            	Bundle b = getIntent().getExtras();
        		listPos = b.getInt("position");
        		QuestionId = b.getInt("qid");
            }
            
            Reset = (Button)findViewById(R.id.Reset);
            Register = (Button)findViewById(R.id.Register);
            EmailID = (EditText)findViewById(R.id.EmailID);
            Username = (EditText)findViewById(R.id.Username);
            Password = (EditText)findViewById(R.id.Password);
            CPassword = (EditText)findViewById(R.id.CPassword);
            PhoneNo = (EditText)findViewById(R.id.PhoneNo);
            Name = (EditText)findViewById(R.id.Name);
            Teacher = (CheckBox)findViewById(R.id.isteacher);
            
            EmailID.addTextChangedListener(new TextWatcher(){

    			public void afterTextChanged(Editable arg0) { }

    			public void beforeTextChanged(CharSequence s, int start, int count,
    					int after) { }

    			public void onTextChanged(CharSequence s, int start, int before,
    					int count) {
    				if(!EmailID.getText().toString().contains("@") && EmailID.getText().length() <= 32)
    					Username.setText(EmailID.getText().toString());
    			}
            });
            
            Reset.setOnClickListener(new View.OnClickListener() {
    			
    			public void onClick(View v) {
    				EmailID.setText("");
    				Password.setText("");
    				CPassword.setText("");
    				PhoneNo.setText("");
    				Username.setText("");
    				Name.setText("");
    				Teacher.setChecked(false);
    			}
    		});
            
            Register.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					dPhoneNo = PhoneNo.getText().toString();
            		dPassword = Password.getText().toString();
            		dCPassword = CPassword.getText().toString();
            		dEmailID = EmailID.getText().toString();
            		dUsername = Username.getText().toString();
            		dName = Name.getText().toString();
            		boolean valid = true;
            		if(dName.length() == 0)
            		{
            			Toast.makeText(RegistrationScreen.this, "Name field cannot be blank", Toast.LENGTH_SHORT).show();
            			valid = false;
            		}
            		else if(!dEmailID.contains("@"))
            		{
            			Toast.makeText(RegistrationScreen.this, "Invalid Email ID format", Toast.LENGTH_SHORT).show();
            			valid = false;
            		}
            		else if(dUsername.length() == 0)
            		{
            			Toast.makeText(RegistrationScreen.this, "Username field cannot be blank", Toast.LENGTH_SHORT).show();
            			valid = false;
            		}
            		else if(!dUsername.matches("[_a-z0-9]+"))
            		{
            			Toast.makeText(RegistrationScreen.this, "Username can only contain lowercase alphabets, numbers or underscore", Toast.LENGTH_SHORT).show();
            			valid = false;
            		}
            		else if(dPassword.length() < 8)
            		{
            			Toast.makeText(RegistrationScreen.this, "Password must be minimum 8 characters", Toast.LENGTH_SHORT).show();
            			valid = false;
            		}
            		else if(dPassword.compareTo(dCPassword) != 0)
            		{
            			Toast.makeText(RegistrationScreen.this, "Password / Confirmation mismatch", Toast.LENGTH_SHORT).show();
            			valid = false;
            		}	
            		else if(dPhoneNo.length() < 10)
            		{
            			Toast.makeText(RegistrationScreen.this, "Invalid mobile phone number", Toast.LENGTH_SHORT).show();
            			valid = false;
            		}
    				if(valid)
    				{
    					try {
							dPassword = m4MCrypt.encrypt(GlobalBO.seed, dPassword).toString();
							new registrationTask().execute();
						} 
    					catch (Exception e) 
						{
							Toast.makeText(RegistrationScreen.this, "AES Engine Failure", Toast.LENGTH_SHORT).show();
						}
    				}
				}
			});
    	}
    	catch(Exception e)
    	{
    		Toast.makeText(RegistrationScreen.this, e.toString(), Toast.LENGTH_SHORT);
    	}
    }
    class registrationTask extends AsyncTask<Void, Void, HttpResponse>
	{
		ProgressDialog d;
		HttpPost registerPost = new HttpPost("http://" + GlobalBO.IP + "/register.php");
		HttpClient m4MClient = new DefaultHttpClient();
		GMailSender sender = new GMailSender("no.reply.m4m", "m4mhabibul");;
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			d = ProgressDialog.show(RegistrationScreen.this, "Please Wait", "Registering with m4M...", true);
			try
			{
				List<NameValuePair> registerParams = new ArrayList<NameValuePair>(6);
				registerParams.add(new BasicNameValuePair("username", dUsername.toLowerCase()));
				registerParams.add(new BasicNameValuePair("password", dPassword));
				registerParams.add(new BasicNameValuePair("name", dName));
				registerParams.add(new BasicNameValuePair("email", dEmailID));
				registerParams.add(new BasicNameValuePair("phone", dPhoneNo));
				registerParams.add(new BasicNameValuePair("version", GlobalBO.version));
				registerPost.setEntity(new UrlEncodedFormEntity(registerParams));
			}
			catch(Exception e)
			{
				Toast.makeText(RegistrationScreen.this, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		protected HttpResponse doInBackground(Void... params) 
		{
			m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
			try
			{
				return m4MClient.execute(registerPost);
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
			if(result == null)
				Toast.makeText(RegistrationScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
			else
			{
				if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
	    		{
					try
					{
						int registerResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
		            	
						if(registerResponse == -2)
						{
							d.dismiss();
							if(GlobalBO.loginID != 0)
							{
								Toast.makeText(RegistrationScreen.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
								GlobalBO.loginID = 0;
								GlobalBO.loginUser = "";
								GlobalBO.isMod = false;
								GlobalBO.rememberMe = false;
							}
							final AlertDialog.Builder box = new AlertDialog.Builder(RegistrationScreen.this);
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
							        	RegistrationScreen.this.finish();
							        }
							        catch(Exception e)
							        {
							        	Toast.makeText(RegistrationScreen.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
							        	RegistrationScreen.this.finish();
							        }
								}
							})
							.setNegativeButton("Exit",new DialogInterface.OnClickListener()
							{
								
								public void onClick(DialogInterface dialog, int which) 
								{
									RegistrationScreen.this.finish();
								}
							})
							.setCancelable(false)
							.create()
							.show();
						}
						else if(registerResponse == 0)
						{
							d.dismiss();
							Toast.makeText(RegistrationScreen.this,"Registration unsuccessful, duplicate e-mail address or username",Toast.LENGTH_SHORT).show();
							Password.setText("");
							CPassword.setText("");
							dPassword = "";
							dCPassword = "";
						}
						else 
						{
							if(Teacher.isChecked())
							{
								class SendMail extends AsyncTask<Void,Void,Boolean>
								{																
									@Override
									protected Boolean doInBackground(Void... params) {
										try {
											sender.sendMail("Teacher Application",   
							                		dName + " having Username: " + dUsername + " has applied as a teacher, contact details are E-Mail: " + dEmailID + ", Phone: " + dPhoneNo,   
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
											Toast.makeText(RegistrationScreen.this, "Registration unsuccessful", Toast.LENGTH_SHORT).show(); 
										}
										else
										{
											Toast.makeText(RegistrationScreen.this, "You are now a member of Mobile Forum", Toast.LENGTH_SHORT).show();
											cleanup();
										}
									}
								}
								new SendMail().execute();
							}
							else
							{
								d.dismiss();
								Toast.makeText(RegistrationScreen.this, "You are now a member of Mobile Forum", Toast.LENGTH_LONG).show();
								cleanup();
							}
						}
					}
					catch(Exception e)
					{
						Toast.makeText(RegistrationScreen.this, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
					}
	    		}
			}
		}		
	}	
    
    private void cleanup()
    {
    	Intent i = new Intent(RegistrationScreen.this, GlobalBO.mContext.getClass());
    	if(GlobalBO.mContext.getClass() == ViewQuestion.class)
    	{
    		i.putExtra("position",listPos);
			i.putExtra("qid",QuestionId);
    	}
    	startActivity(i);
		RegistrationScreen.this.finish();
    }
    
    public void onBackPressed()
    {
    	cleanup();
    }
}
