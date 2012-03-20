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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.AllQuestions;
import com.m4m.BO.GlobalBO;

public class QuestionsScreen extends ListActivity 
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
	
	private LayoutInflater mInflater;
	private TextView tv, retrievemore, noQues;
	private Button ask; 
	private EditText glanceSearch;
	private ArrayList<AllQuestions> q = GlobalBO.questions;
	public String attributeId;
	private ListView list;
	private View footer;
	ArrayAdapter<AllQuestions> glanceAdapter;
	private ArrayList<AllQuestions> filtered = new ArrayList<AllQuestions>();
	private boolean topicSearch = false;
	private int searchLength = 0;
	private void UpdateBox()
	{
		if(GlobalBO.loginID != 0)
		{
			Toast.makeText(QuestionsScreen.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
			GlobalBO.loginID = 0;
			GlobalBO.loginUser = "";
			GlobalBO.isMod = false;
			GlobalBO.rememberMe = false;
		}
		final AlertDialog.Builder box = new AlertDialog.Builder(QuestionsScreen.this);
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
		        	QuestionsScreen.this.finish();
		        }
		        catch(Exception e)
		        {
		        	Toast.makeText(QuestionsScreen.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
		        	QuestionsScreen.this.finish();
		        }
			}
		})
		.setNegativeButton("Exit",new DialogInterface.OnClickListener()
		{
			
			public void onClick(DialogInterface dialog, int which) 
			{
				QuestionsScreen.this.finish();
			}
		})
		.setCancelable(false)
		.create()
		.show();
	}
		
	@Override
	public void onBackPressed() 
	{
		if(glanceSearch.getVisibility() == 0)
		{
			glanceSearch.setVisibility(8);
			if(filtered.size() != q.size())
			{
				filtered.clear();
				for(int i = 0 ; i < q.size() ; ++i)
					filtered.add(q.get(i));
				noQues.setVisibility(8);
				glanceAdapter.notifyDataSetChanged();
			}
			list.addFooterView(footer);
		}
		else
		{
			Intent back = new Intent(QuestionsScreen.this,MainScreen.class);
			startActivity(back);
			QuestionsScreen.this.finish();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.allquestions);
				GlobalBO.questions.clear();
				GlobalBO.base = 0;
				mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				glanceSearch = (EditText)findViewById(R.id.glancesearch);
				glanceSearch.setHint("Type to search");
				glanceSearch.setVisibility(8);
				tv = (TextView)findViewById(R.id.userdetails);
				ask = (Button)findViewById(R.id.ask_question_bttn);
				noQues = (TextView)findViewById(R.id.noques);
				list = (ListView)findViewById(android.R.id.list);
				footer = View.inflate(QuestionsScreen.this, R.layout.listfooter, null);
				list.addFooterView(footer);
				retrievemore = (TextView)findViewById(R.id.retrievemore);
				retrievemore.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						GlobalBO.base += GlobalBO.retrievalCount;
						retrieveQuestions();
					}
				});

				glanceSearch.addTextChangedListener(new TextWatcher() {


					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						try 
						{
							filtered.clear();
							searchLength = glanceSearch.getText().length();
							if(searchLength == 0)
							{
								noQues.setVisibility(8);
								for(int i = 0 ; i < q.size() ; ++i)
									filtered.add(q.get(i));
							}
							else if(topicSearch == true)
							{
								for(int i = 0 ; i < q.size() ; ++i)
								{
									String x = q.get(i).get_title();
									if(searchLength <= x.length())
									{
										if(x.toLowerCase().contains(glanceSearch.getText().toString().toLowerCase()))
										{
											filtered.add(q.get(i));
										}
									}
									if(filtered.size() > 0)
									{
										noQues.setVisibility(8);
									}
									else
									{
										noQues.setVisibility(0);
									}
								}
							}
							else
							{
								for(int i = 0 ; i < q.size() ; ++i)
								{
									String x = q.get(i).get_askerid();
									if(searchLength <= x.length())
									{
										if(x.toLowerCase().contains(glanceSearch.getText().toString().toLowerCase()))
										{
											filtered.add(q.get(i));
										}
									}
									if(filtered.size() > 0)
									{
										noQues.setVisibility(8);
									}
									else
									{
										noQues.setVisibility(0);
									}
								}
							}
						}
						catch(Exception e)
						{
							Toast.makeText(QuestionsScreen.this, "Search Error", Toast.LENGTH_SHORT).show();
						}
						glanceAdapter.notifyDataSetChanged();
					}

					@Override
					public void afterTextChanged(Editable s) { }
				});
				
				if(GlobalBO.loginID != 0)
				{	
					new loginTv(tv).execute();
					tv.setEnabled(false);
				}
				
				tv.setOnClickListener(new loginDialog(QuestionsScreen.this,  new loginDialog.postProcess() {
					
					public void postExecute(int errorState) 
					{
						if(errorState == -2)
						{
							UpdateBox();
						}
						else
						{
							new loginTv(tv).execute();
							tv.setEnabled(false);
						}
					}
					
					public void userRegistration()
					{
						GlobalBO.mContext = QuestionsScreen.this;
						Intent i = new Intent(QuestionsScreen.this, RegistrationScreen.class);
						startActivity(i);
						QuestionsScreen.this.finish();
					}
					
					public void forgotPassword()
					{
						GlobalBO.mContext = QuestionsScreen.this;
						Intent i = new Intent(QuestionsScreen.this, ForgotPassword.class);
						startActivity(i);
						QuestionsScreen.this.finish();
					}
				}));
				
				ask.setOnClickListener(new View.OnClickListener()
				{
					
					public void onClick(View arg0) 
					{
						if(GlobalBO.loginID != 0)
						{
							Intent i = new Intent(QuestionsScreen.this,AskQuestionScreen.class);
							startActivity(i);
							QuestionsScreen.this.finish();
						}
						else
							Toast.makeText(QuestionsScreen.this, "You must be logged in to ask questions", Toast.LENGTH_SHORT).show();
					}
				});
				
				retrieveQuestions();
				
	}
	catch(Exception e)
	{
				Toast.makeText(QuestionsScreen.this, e.toString(), Toast.LENGTH_LONG).show();
	}
	}
	
	void retrieveQuestions()
	{
		new glanceLoad(QuestionsScreen.this, new glanceLoad.postProcess() {
			
			public void onFailure(int errorState)
			{
				if(errorState == -2)
				{
					UpdateBox();
				}
				else
				{
					if(!GlobalBO.rememberMe)
					{
						GlobalBO.loginID = 0;
						GlobalBO.loginUser = "";
						GlobalBO.isMod = false;
						GlobalBO.rememberMe = false;
					}
					Intent i = new Intent(QuestionsScreen.this, MainScreen.class);
					startActivity(i);
					QuestionsScreen.this.finish();
				}
			}

			public void onSuccess() 
			{
				filtered.clear();
				for(int i = 0 ; i < q.size() ; ++i)
					filtered.add(q.get(i));
				if(GlobalBO.base == 0)
				{
					setAdapter();
				}
				else
				{
					glanceAdapter.notifyDataSetChanged();
				}			
			}
		}).execute();
	}
	
	class QuestionsAdapter extends ArrayAdapter<AllQuestions>
	{

		public QuestionsAdapter(Context context, int textViewResourceId, ArrayList<AllQuestions> x) 
		{
			super(context, textViewResourceId, x);
		}
		@Override
    	public View getView(final int position, View convertView, ViewGroup parent) 
    	{
			View row;
	    		        		
    		if (null == convertView) {
    			row = mInflater.inflate(R.layout.questionlayout, null);
    		} else {
    			row = convertView;
    		}
    		
    											        				        		
    		TextView tv1 = (TextView) row.findViewById(R.id.qtitle);
    		TextView tv2 = (TextView) row.findViewById(R.id.quser_asked);
    		TextView tv3 = (TextView) row.findViewById(R.id.qdate_asked);
    		TextView tv4 = (TextView) row.findViewById(R.id.qrating);
    		TextView tv5 = (TextView) row.findViewById(R.id.tanswer);
   	
    		tv2.setTypeface(null, Typeface.NORMAL);
    		if(filtered.get(position).is_teacherAns()==true)
    		{
    			tv2.setTypeface(null, Typeface.ITALIC);
    		}
    		
    		tv1.setText("" + filtered.get(position).get_title());
    		tv2.setText("" + filtered.get(position).get_askerid());
    		tv3.setText("" + filtered.get(position).get_askDate());
    		tv4.setText("" + filtered.get(position).get_rating());
    		if(filtered.get(position).is_solved()==true)
    		{	
    			tv5.setBackgroundResource(R.drawable.tick3);
    		}
    		else
    		{	
    			tv5.setBackgroundResource(R.drawable.cross3);
    		}   
    		
    		row.setOnClickListener(new View.OnClickListener() 
    		{
				
				public void onClick(View v) {
					Intent i = new Intent(QuestionsScreen.this, ViewQuestion.class);
					i.putExtra("position", position);
					i.putExtra("qid", filtered.get(position).get_qid());
					startActivity(i);
					QuestionsScreen.this.finish();
				}
			});
    		
    		   		row.setOnLongClickListener(new View.OnLongClickListener()
		    		{
						
						public boolean onLongClick(View v) 
						{
							if(GlobalBO.isMod==true)
				    		{
								final AlertDialog.Builder box = new AlertDialog.Builder(QuestionsScreen.this);
								box.setTitle("Delete Question")
								.setMessage("Are you sure you want to delete this question?")
								.setPositiveButton("Yes",new DialogInterface.OnClickListener()
								{
									
									class DelQuestion extends AsyncTask<Void,Void,HttpResponse>
									{
										ProgressDialog d;
										HttpPost delPost = new HttpPost("http://" + GlobalBO.IP + "/delquestion.php");
										HttpClient m4MClient = new DefaultHttpClient();
										
										@Override
										protected void onPreExecute() 
										{
											super.onPreExecute();
											d = ProgressDialog.show(QuestionsScreen.this, "Please Wait", "Deleting question...", true);
											try
											{
												List<NameValuePair> delParams = new ArrayList<NameValuePair>(3);
												delParams.add(new BasicNameValuePair("qid",Integer.toString(filtered.get(position).get_qid())));
												delParams.add(new BasicNameValuePair("idmod",Integer.toString(GlobalBO.loginID)));
												delParams.add(new BasicNameValuePair("version", GlobalBO.version));
												delPost.setEntity(new UrlEncodedFormEntity(delParams));		
											}
											catch(Exception e)
											{
												Toast.makeText(QuestionsScreen.this, "Error logging into Server", Toast.LENGTH_SHORT).show();
											}
										}
										
										@Override
										protected HttpResponse doInBackground(Void... params) 
										{
											m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
											try
											{
												return m4MClient.execute(delPost);
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
												Toast.makeText(QuestionsScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
											}
											else
											{
												
												if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
												{
													try 
													{
														int solvedResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
														if(solvedResponse == -2)
														{
															UpdateBox();
														}
														else if(solvedResponse == 0)
														{
															Toast.makeText(QuestionsScreen.this, "Unable to delete question", Toast.LENGTH_SHORT).show();
														}
										            	else if(solvedResponse == -1)
										            	{
										            		Toast.makeText(QuestionsScreen.this, "Your account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
										            		GlobalBO.loginID = 0;
										            		GlobalBO.loginUser = "";
										            		GlobalBO.isMod = false;
										            		GlobalBO.rememberMe = false;
										            		tv.setEnabled(true);
												    		tv.setText("Sign In to access all features of m4M");
										            	}
										            	else
										            	{
										            		Intent reload = new Intent(QuestionsScreen.this, QuestionsScreen.class);
															startActivity(reload);
															QuestionsScreen.this.finish();
										            	}
													}
													catch(Exception e)
													{
														Toast.makeText(QuestionsScreen.this, e.toString(), Toast.LENGTH_LONG).show();
													}
												}
												else
												{
													Toast.makeText(QuestionsScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
												}
											}
										}
											
									}
									public void onClick(DialogInterface dialog, int which) 
									{
										new DelQuestion().execute();
									}
								})
								.setNegativeButton("No",new DialogInterface.OnClickListener()
								{
									
									public void onClick(DialogInterface dialog, int which) 
									{
										
									}
								})
								.create()
								.show();
				    		}
							return true;
						}
					});
    		return row;
    	}
	}
	
	void setAdapter()
	{
		try 
		{
			glanceAdapter = new QuestionsAdapter(QuestionsScreen.this, R.layout.questionlayout, filtered);
			setListAdapter(glanceAdapter);
		}
		catch(Exception e)
		{
			Toast.makeText(QuestionsScreen.this, "Error populating list", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		if(GlobalBO.loginID != 0)
			inflater.inflate(R.menu.glancemenu,menu);
		else
			inflater.inflate(R.menu.glancemenu1,menu);
		return true;
	}
	
	private void searchPrepare()
	{
		glanceSearch.setVisibility(0);
		glanceSearch.setText("");
		glanceSearch.requestFocus();
		if(filtered.size() != q.size())
		{
			filtered.clear();
			for(int i = 0 ; i < q.size() ; ++i)
				filtered.add(q.get(i));
			glanceAdapter.notifyDataSetChanged();
		}
		list.removeFooterView(footer);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch(item.getItemId()) 
	    {
	    	case R.id.gl_search_user: 
	    	{
	    		topicSearch = false;
	    		searchPrepare();
	    	}
	    	break;
	    	case R.id.gl_search_topic:
	    	{
	    		topicSearch = true;
	    		searchPrepare();
	    	}
	    	break;
	    	case R.id.gl_logout_menu :
	    	{	
	    				
	    		final AlertDialog.Builder box = new AlertDialog.Builder(QuestionsScreen.this);
				box.setTitle("Logout")
				.setMessage("Are you sure you want to logout from Mobile Forum?")
				.setPositiveButton("Yes",new DialogInterface.OnClickListener()
				{
					
					public void onClick(DialogInterface dialog, int which) 
					{
						GlobalBO.loginID = 0;
			    		GlobalBO.loginUser = "";
			    		GlobalBO.isMod = false;
			    		GlobalBO.rememberMe = false;
			    		tv.setEnabled(true);
			    		tv.setText("Sign In to access all features of m4M");
					}
				})
				.setNegativeButton("No",new DialogInterface.OnClickListener()
				{
					
					public void onClick(DialogInterface dialog, int which) {	}
				})
				.create()
				.show();
	    	}	
	    	break;
	    	case R.id.gl_refresh_menu :
	    	{
	    		Intent i = new Intent(QuestionsScreen.this,QuestionsScreen.class);
	    		startActivity(i);
	    		QuestionsScreen.this.finish();
	    	}
	    	break;
	    }
	    return true;
	}
}
