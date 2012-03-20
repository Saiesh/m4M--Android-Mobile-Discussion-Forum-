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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;

public class AskQuestionScreen extends Activity 
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
	
	EditText txtTitle;
	EditText txtQuestion;
	Button btnPost;
	
	String qtitle,question;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.askquestion);
		txtTitle = (EditText)findViewById(R.id.qtitle);
		txtQuestion = (EditText)findViewById(R.id.question);
		btnPost = (Button)findViewById(R.id.postquestion);
		
		btnPost.setOnClickListener(new View.OnClickListener()
		{
			class PostQuestion extends AsyncTask<Void,Void,HttpResponse>
			{
				ProgressDialog d;
				HttpPost questionPost = new HttpPost("http://" + GlobalBO.IP + "/postquestion.php");
				HttpClient m4MClient = new DefaultHttpClient();
				
				@Override
				protected void onPreExecute() 
				{
					d=ProgressDialog.show(AskQuestionScreen.this,"Please Wait","Posting your question...",true);
					
					try
					{
						List<NameValuePair> questionParams = new ArrayList<NameValuePair>(4);
						questionParams.add(new BasicNameValuePair("idasker", Integer.toString(GlobalBO.loginID)));
						questionParams.add(new BasicNameValuePair("title", qtitle));
						questionParams.add(new BasicNameValuePair("question", question));
						questionParams.add(new BasicNameValuePair("version", GlobalBO.version));
						questionPost.setEntity(new UrlEncodedFormEntity(questionParams));		
					}
					catch(Exception e)
					{
						Toast.makeText(AskQuestionScreen.this, "Error logging into Server", Toast.LENGTH_SHORT).show();
					}
				}
			
				@Override
				protected HttpResponse doInBackground(Void... params) 
				{
					m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
					try
					{
						return m4MClient.execute(questionPost);
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
						Toast.makeText(AskQuestionScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
					}
					else
					{
						if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
						{
							try 
							{
								int solvedResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
				            	if(solvedResponse == 0)
								{
									Toast.makeText(AskQuestionScreen.this, "Unable to post question", Toast.LENGTH_SHORT).show();
								}
				            	else if(solvedResponse == -2)
				            	{
				            		if(GlobalBO.loginID != 0)
				            		{
										Toast.makeText(AskQuestionScreen.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
										GlobalBO.loginID = 0;
										GlobalBO.loginUser = "";
										GlobalBO.isMod = false;
										GlobalBO.rememberMe = false;
				            		}
				            		final AlertDialog.Builder box = new AlertDialog.Builder(AskQuestionScreen.this);
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
									        	AskQuestionScreen.this.finish();
									        }
									        catch(Exception e)
									        {
									        	Toast.makeText(AskQuestionScreen.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
									        	AskQuestionScreen.this.finish();
									        }
										}
									})
									.setNegativeButton("Exit",new DialogInterface.OnClickListener()
									{
										
										public void onClick(DialogInterface dialog, int which) 
										{
											AskQuestionScreen.this.finish();
										}
									})
									.setCancelable(false)
									.create()
									.show();
				            	}
				            	else
				            	{
				            		if(solvedResponse == -1)
				            		{
					            		Toast.makeText(AskQuestionScreen.this, "Your account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
					            		GlobalBO.loginID = 0;
					            		GlobalBO.loginUser = "";
					            		GlobalBO.isMod = false;
					            		GlobalBO.rememberMe = false;
				            		}
				            		Intent i = new Intent(AskQuestionScreen.this, QuestionsScreen.class);
									startActivity(i);
									AskQuestionScreen.this.finish();
				            	}
							}
							catch(Exception e)
							{
								Toast.makeText(AskQuestionScreen.this, "Unable to post question", Toast.LENGTH_SHORT).show();
							}
						}
						else
						{
							Toast.makeText(AskQuestionScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
						}						
					}
				}
			}
			public void onClick(View arg0) 
			{
				try
				{
					qtitle = txtTitle.getText().toString();
					question = txtQuestion.getText().toString();
					if(qtitle.length() == 0 || question.length() == 0)
						Toast.makeText(AskQuestionScreen.this, "Question title or body must not be blank", Toast.LENGTH_SHORT).show();
					else
						new PostQuestion().execute();
				}
				catch(Exception e)
				{
					Toast.makeText(AskQuestionScreen.this, "Unable to post question", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	@Override
	public void onBackPressed()
	{
		if(txtTitle.getText().toString().length() == 0 && txtQuestion.getText().toString().length() == 0)
		{
			Intent back = new Intent(AskQuestionScreen.this, QuestionsScreen.class);
			startActivity(back);
			AskQuestionScreen.this.finish();
		}
		else
		{
			final AlertDialog.Builder box = new AlertDialog.Builder(AskQuestionScreen.this);
			box.setTitle("Discard Question")
			.setMessage("Do you want to discard the question?")
			.setPositiveButton("Yes",new DialogInterface.OnClickListener()
			{
				
				
				public void onClick(DialogInterface dialog, int which) 
				{
					Intent back = new Intent(AskQuestionScreen.this, QuestionsScreen.class);
					startActivity(back);
					AskQuestionScreen.this.finish();
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
