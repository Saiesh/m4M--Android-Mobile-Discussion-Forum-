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

import com.m4m.BO.GlobalBO;

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
import android.widget.TextView;
import android.widget.Toast;

public class AnswerQuestion extends Activity 
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
	
	Button bttn_PostAnswer;
	EditText Answer;
	String answer;
	int listPos;
	int QuestionId;
	TextView txt_qtitle,txt_question;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		try {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.answerquestion);
		
		txt_qtitle=(TextView)findViewById(R.id.qtitle);
		txt_question=(TextView)findViewById(R.id.question);
		
		Bundle b = getIntent().getExtras();
		listPos = b.getInt("position");
		QuestionId = b.getInt("qid");
		
		bttn_PostAnswer = (Button)findViewById(R.id.bttn_postAnswer);
		Answer = (EditText)findViewById(R.id.answerGiven);
		
		txt_qtitle.setText(GlobalBO.questions.get(listPos).get_title());
		txt_question.setText(GlobalBO.questions.get(listPos).get_question());
		
		
		bttn_PostAnswer.setOnClickListener(new View.OnClickListener()
		{
			class PostQuestion extends AsyncTask<Void,Void,HttpResponse>
			{
				ProgressDialog d;
				HttpPost answerPost = new HttpPost("http://" + GlobalBO.IP + "/postanswer.php");
				HttpClient m4MClient = new DefaultHttpClient();
				
				@Override
				protected void onPreExecute() 
				{
					d=ProgressDialog.show(AnswerQuestion.this,"Please Wait","Posting your answer...",true);
					
					try
					{
						List<NameValuePair> answerParams = new ArrayList<NameValuePair>(4);
						answerParams.add(new BasicNameValuePair("idanswerer", Integer.toString(GlobalBO.loginID)));
						answerParams.add(new BasicNameValuePair("answer",answer));
						answerParams.add(new BasicNameValuePair("idquestion",Integer.toString(QuestionId)));
						answerParams.add(new BasicNameValuePair("version", GlobalBO.version));
						answerPost.setEntity(new UrlEncodedFormEntity(answerParams));		
					}
					catch(Exception e)
					{
						Toast.makeText(AnswerQuestion.this, "Error logging into Server", Toast.LENGTH_SHORT).show();
					}
				}
			
				@Override
				protected HttpResponse doInBackground(Void... params) 
				{
					m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
					try
					{
						return m4MClient.execute(answerPost);
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
						Toast.makeText(AnswerQuestion.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
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
									Toast.makeText(AnswerQuestion.this, "Unable to post answer",Toast.LENGTH_SHORT).show();
								}
				            	else if(solvedResponse == -2)
				            	{
				            		if(GlobalBO.loginID != 0)
				            		{
										Toast.makeText(AnswerQuestion.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
										GlobalBO.loginID = 0;
										GlobalBO.loginUser = "";
										GlobalBO.isMod = false;
										GlobalBO.rememberMe = false;
				            		}
				            		final AlertDialog.Builder box = new AlertDialog.Builder(AnswerQuestion.this);
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
									        	AnswerQuestion.this.finish();
									        }
									        catch(Exception e)
									        {
									        	Toast.makeText(AnswerQuestion.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
									        	AnswerQuestion.this.finish();
									        }
										}
									})
									.setNegativeButton("Exit",new DialogInterface.OnClickListener()
									{
										
										public void onClick(DialogInterface dialog, int which) 
										{
											AnswerQuestion.this.finish();
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
					            		Toast.makeText(AnswerQuestion.this, "Your account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
					            		GlobalBO.loginID = 0;
					            		GlobalBO.loginUser = "";
					            		GlobalBO.isMod = false;
					            		GlobalBO.rememberMe = false;
				            		}
				            		Intent i = new Intent(AnswerQuestion.this, ViewQuestion.class);
									i.putExtra("position",listPos);
									i.putExtra("qid",QuestionId);
									startActivity(i);
									AnswerQuestion.this.finish();
				            	}
							}
							catch(Exception e)
							{
								Toast.makeText(AnswerQuestion.this, "Unable to post answer",Toast.LENGTH_SHORT).show();
							}
						}
						else
						{
							Toast.makeText(AnswerQuestion.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
						}
												
					}
				}
			}
			public void onClick(View arg0) 
			{
				try
				{
					answer = Answer.getText().toString();
					if(answer.length()==0)
						Toast.makeText(AnswerQuestion.this, "Answer body must not be blank", Toast.LENGTH_SHORT).show();
					else
						new PostQuestion().execute();
				}
				catch(Exception e)
				{
					Toast.makeText(AnswerQuestion.this, "Unable to post answer", Toast.LENGTH_SHORT).show();
				}
			}
		});
		}
		catch (Exception e)
		{
			
		}
	}
	@Override
	public void onBackPressed()
	{
		if(Answer.getText().toString().length() == 0)
		{
			Intent i = new Intent(AnswerQuestion.this, ViewQuestion.class);
			i.putExtra("position",listPos);
			i.putExtra("qid",QuestionId);
			startActivity(i);
			AnswerQuestion.this.finish();
		}
		else
		{
			final AlertDialog.Builder box = new AlertDialog.Builder(AnswerQuestion.this);
			box.setTitle("Discard Answer")
			.setMessage("Do you want to discard the answer?")
			.setPositiveButton("Yes",new DialogInterface.OnClickListener()
			{		
				public void onClick(DialogInterface dialog, int which) 
				{
					Intent i = new Intent(AnswerQuestion.this, ViewQuestion.class);
					i.putExtra("position",listPos);
					i.putExtra("qid",QuestionId);
					startActivity(i);
					AnswerQuestion.this.finish();
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
	


