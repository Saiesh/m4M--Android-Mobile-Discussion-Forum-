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
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.AllAnswers;
import com.m4m.BO.GlobalBO;

public class ViewQuestion extends ListActivity 
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
	
	ArrayList<AllAnswers> a;
	ImageButton bttn_incQRating, minimize, imgExpandQ;
	public TextView tv;
	TextView txt_qtitle,txt_question,txt_quser,txt_qdate,txt_qrating, txt_noanswer;
	ImageView img_isanswer;
	LayoutInflater mInflater;
	Button bttn_postAnswer;
	Bundle b;
	int listPos, QuestionId;
	RelativeLayout layout;
	LinearLayout qExpand;
	boolean isCompressed=false;
	int lines=0,TitleLines=0;
			
	private void UpdateBox()
	{
		if(GlobalBO.loginID != 0)
		{
			Toast.makeText(ViewQuestion.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
			GlobalBO.loginID = 0;
			GlobalBO.loginUser = "";
			GlobalBO.isMod = false;
			GlobalBO.rememberMe = false;
		}
		final AlertDialog.Builder box = new AlertDialog.Builder(ViewQuestion.this);
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
		        	ViewQuestion.this.finish();
		        }
		        catch(Exception e)
		        {
		        	Toast.makeText(ViewQuestion.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
		        	ViewQuestion.this.finish();
		        }
			}
		})
		.setNegativeButton("Exit",new DialogInterface.OnClickListener()
		{
			
			public void onClick(DialogInterface dialog, int which) 
			{
				ViewQuestion.this.finish();
			}
		})
		.setCancelable(false)
		.create()
		.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewquestion);
		try
		{
				mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				Bundle b = getIntent().getExtras();
				listPos = b.getInt("position");
				QuestionId = b.getInt("qid");
				
				layout = (RelativeLayout)findViewById(R.id.CompLayout);
				qExpand = (LinearLayout)findViewById(R.id.expandq);
				imgExpandQ=(ImageButton)findViewById(R.id.imgexpandq);	
				minimize=(ImageButton)findViewById(R.id.tminimize);				
				bttn_incQRating=(ImageButton)findViewById(R.id.qup);
				txt_qtitle=(TextView)findViewById(R.id.qtitle);
				txt_question=(TextView)findViewById(R.id.question);
				txt_quser=(TextView)findViewById(R.id.quser_asked);
				txt_qdate=(TextView)findViewById(R.id.qdate_asked);
				txt_qrating=(TextView)findViewById(R.id.qrating);
				txt_noanswer=(TextView)findViewById(R.id.noanswer);
				img_isanswer=(ImageView)findViewById(R.id.tanswer);
				bttn_postAnswer=(Button)findViewById(R.id.post_answer_bttn);
				tv = (TextView)findViewById(R.id.userdetails);
					
				qExpand.setVisibility(8);
				minimize.setVisibility(8);
				
				minimize.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						layout.setVisibility(8);
						qExpand.setVisibility(0);
					}
				});
				
				qExpand.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						layout.setVisibility(0);
						qExpand.setVisibility(8);
					}
				});
				imgExpandQ.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						layout.setVisibility(0);
						qExpand.setVisibility(8);
					}
				});
				
				if(GlobalBO.loginID != 0)
				{	
					new loginTv(tv).execute();
					tv.setEnabled(false);
				}
								
				tv.setOnClickListener(new loginDialog(ViewQuestion.this,  new loginDialog.postProcess() {
					
					public void postExecute(int errorState) 
					{
						if(errorState == -2)
							UpdateBox();
						else
						{
							new loginTv(tv).execute();
							tv.setEnabled(false);
						}
					}
					
					public void userRegistration()
					{
						GlobalBO.mContext = ViewQuestion.this;
						Intent i = new Intent(ViewQuestion.this, RegistrationScreen.class);
						i.putExtra("qid",QuestionId);
						i.putExtra("position",listPos);
						startActivity(i);
						ViewQuestion.this.finish();
					}
					
					public void forgotPassword()
					{
						GlobalBO.mContext = ViewQuestion.this;
						Intent i = new Intent(ViewQuestion.this, ForgotPassword.class);
						i.putExtra("qid",QuestionId);
						i.putExtra("position",listPos);
						startActivity(i);
						ViewQuestion.this.finish();
					}
				}));
				
				bttn_postAnswer.setOnClickListener(new View.OnClickListener()
				{
					
					public void onClick(View arg0) 
					{
						if(GlobalBO.loginID != 0)
						{
							Intent i = new Intent(ViewQuestion.this, AnswerQuestion.class);
							i.putExtra("qid",QuestionId);
							i.putExtra("position",listPos);
							startActivity(i);
							ViewQuestion.this.finish();
						}
						else
							Toast.makeText(ViewQuestion.this, "You must be logged in to post answers", Toast.LENGTH_SHORT).show();
					}
				});

				
				new questionLoad(ViewQuestion.this, new questionLoad.postProcess() {
					
					public void onFailure(int errorState)
					{
						if(errorState == -2)
							UpdateBox();
						else
						{
							GlobalBO.loginID = 0;
							GlobalBO.loginUser = "";
							GlobalBO.isMod = false;
							GlobalBO.rememberMe = false;
							Intent i = new Intent(ViewQuestion.this, MainScreen.class);
							startActivity(i);
							ViewQuestion.this.finish();
						}
					}
					
					public void onSuccess() {
						txt_question.setText(GlobalBO.questions.get(listPos).get_question());
						if(txt_question.getLineCount() > 4)
							minimize.setVisibility(0);
						txt_qrating.setText("" + GlobalBO.questions.get(listPos).get_rating());
						if(GlobalBO.questions.get(listPos).is_solved()==true)
						{	
							img_isanswer.setBackgroundResource(R.drawable.tick3);
						}
						else
						{	
							img_isanswer.setBackgroundResource(R.drawable.cross3);
						}
						a = GlobalBO.answers;
						if(!a.isEmpty()) {
							txt_noanswer.setVisibility(8);
						setListAdapter(new ArrayAdapter<AllAnswers>(ViewQuestion.this, R.layout.answerlayout, a) 
								{
										int currId = 0;
										ImageView currImg = null;
							        	@Override
							        	public View getView(final int position, View convertView, ViewGroup parent) 
							        	{
							        		View row;
							        		if (null == convertView) 
							        		{
							        			row = mInflater.inflate(R.layout.answerlayout, null);
							        		} 
							        		else 
							        		{
							        			row = convertView;
							        		}
							        		
							        		try
							        		{
							        		   		ImageButton bttn_incARating = (ImageButton)row.findViewById(R.id.ansup);
									        		final TextView txt_ansrating = (TextView)row.findViewById(R.id.ansrating);
									        		final ImageView img_setanswer = (ImageView)row.findViewById(R.id.isanswer);
									        		final TextView txt_answer = (TextView)row.findViewById(R.id.answer);
									        		final TextView txt_answerer = (TextView)row.findViewById(R.id.answerer);
									        		final TextView txt_answerdate = (TextView)row.findViewById(R.id.answerdate);
									        		
									        		txt_answerer.setTypeface(null, Typeface.NORMAL);
									        		if(a.get(position).is_teacherAnswer()==true)
									        		{
									        			txt_answerer.setTypeface(null, Typeface.ITALIC);
									        		}
									        		
									        		txt_ansrating.setText(""+a.get(position).get_ansrating());
									        		txt_answer.setText(a.get(position).get_answer());
									        		txt_answerer.setText(a.get(position).get_answererid());
									        		txt_answerdate.setText(a.get(position).get_answerDate());
									        		
													bttn_incARating.setOnClickListener(new rateElement(a.get(position).get_aid(), false, bttn_incARating, txt_ansrating, tv, ViewQuestion.this, new rateElement.postProcess() {
														
														@Override
														public void onMismatch() {
															UpdateBox();
														}
													}));
													
													if(a.get(position).is_isAnswer()==true)
													{	
														img_setanswer.setBackgroundResource(R.drawable.tick3);
														currImg = img_setanswer;
														currId = a.get(position).get_aid();
													}
													else
													{	
														img_setanswer.setBackgroundResource(R.drawable.notanswer);
													}
													img_setanswer.setOnClickListener(new View.OnClickListener() {
														
														class postSolved extends AsyncTask<Void, Void, HttpResponse>
														{
															HttpClient m4MClient = new DefaultHttpClient();
															HttpPost solvedPost;
															
															@Override
															protected void onPreExecute()
															{
																if(a.get(position).is_isAnswer())
																{
																	img_setanswer.setBackgroundResource(R.drawable.tick3pressed);
																}
																else
																{
																	img_setanswer.setBackgroundResource(R.drawable.notanswerpressed);
																}
																solvedPost = new HttpPost("http://" + GlobalBO.IP + "/solved.php");
																try
																{
																	List<NameValuePair> solvedParams = new ArrayList<NameValuePair>(4);
																	solvedParams.add(new BasicNameValuePair("curr_idanswer", Integer.toString(a.get(position).get_aid())));
																	solvedParams.add(new BasicNameValuePair("prev_idanswer", Integer.toString(currId)));
																	solvedParams.add(new BasicNameValuePair("iduser", Integer.toString(GlobalBO.loginID)));
																	solvedParams.add(new BasicNameValuePair("version", GlobalBO.version));
																	solvedPost.setEntity(new UrlEncodedFormEntity(solvedParams));
																}
																catch(Exception e)
																{
																	Toast.makeText(ViewQuestion.this, "Unable to mark as solution", Toast.LENGTH_SHORT).show();
																}
															}
															@Override
															protected HttpResponse doInBackground(Void... params) 
															{
																m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
																try
																{
																	return m4MClient.execute(solvedPost);
																}
																catch(Exception e)
																{
																	return null;
																}
															}
															
															@Override
															protected void onPostExecute(HttpResponse result)
															{
																if(a.get(position).is_isAnswer())
																{
																	img_setanswer.setBackgroundResource(R.drawable.tick3);
																}
																else
																{
																	img_setanswer.setBackgroundResource(R.drawable.notanswer);
																}
																if(result == null)
																	Toast.makeText(ViewQuestion.this, "Unable to mark as solution", Toast.LENGTH_SHORT).show();
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
																				Toast.makeText(ViewQuestion.this,"Unable to mark as solution",Toast.LENGTH_SHORT).show();
																			}
															            	else if(solvedResponse == -1)
															            	{
															            		Toast.makeText(ViewQuestion.this, "Your account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
															            		GlobalBO.loginID = 0;
															            		GlobalBO.loginUser = "";
															            		GlobalBO.isMod = false;
															            		GlobalBO.rememberMe = false;
															            		tv.setEnabled(true);
																	    		tv.setText("Sign In to access all features of m4M");
															            	}
															            	else
															            	{
															            		if(a.get(position).is_isAnswer())
																				{
															            			img_isanswer.setBackgroundResource(R.drawable.cross3);
																					img_setanswer.setBackgroundResource(R.drawable.notanswer);
																					a.get(position).set_isAnswer(false);
																					GlobalBO.questions.get(listPos).set_solved(false);
																					currId = 0;
																					currImg = null;
																				}
																				else
																				{
																					img_setanswer.setBackgroundResource(R.drawable.tick3);
																					img_isanswer.setBackgroundResource(R.drawable.tick3);
																					if(currId != 0)
																					{
																						currImg.setBackgroundResource(R.drawable.notanswer);
																						for(AllAnswers ans : a)
																						{
																							if(ans.get_aid() == currId)
																								ans.set_isAnswer(false);
																						}
																					}
																					currId = a.get(position).get_aid();
																					currImg = img_setanswer;
																					a.get(position).set_isAnswer(true);
																				}
															            	}
																		}
																		catch(Exception e)
																		{
																			Toast.makeText(ViewQuestion.this, "Unable to mark as solution", Toast.LENGTH_SHORT).show();
																		}
														    		}
																}
															}
														}
														
														public void onClick(View arg0) 
														{
															if(GlobalBO.loginUser.compareTo(GlobalBO.questions.get(listPos).get_askerid()) == 0)
															{
																new postSolved().execute();
															}	
															else
															{
																Toast.makeText(ViewQuestion.this, "You must be the question asker to mark a solution", Toast.LENGTH_SHORT).show();
															}
														}
													});													
							        		}
							        		catch(Exception e)
							        		{
							        			Toast.makeText(ViewQuestion.this, e.toString(), Toast.LENGTH_LONG).show();
							        		}
							        		finally
							        		{
						        			   		row.setOnLongClickListener(new View.OnLongClickListener()
						        		    		{
						        						
						        						public boolean onLongClick(View v) 
						        						{
						        							if(GlobalBO.isMod==true)
										            		{	
								        							final AlertDialog.Builder box = new AlertDialog.Builder(ViewQuestion.this);
								        							box.setTitle("Delete Answer")
								        							.setMessage("Are you sure you want to delete this answer?")
								        							.setPositiveButton("Yes",new DialogInterface.OnClickListener()
								        							{
								        								
								        								class DelQuestion extends AsyncTask<Void,Void,HttpResponse>
								        								{
								        									ProgressDialog d;
								        									HttpPost delPost = new HttpPost("http://" + GlobalBO.IP + "/delanswer.php");
								        									HttpClient m4MClient = new DefaultHttpClient();
								        									
								        									@Override
								        									protected void onPreExecute() 
								        									{
								        										super.onPreExecute();
								        										d = ProgressDialog.show(ViewQuestion.this, "Please Wait", "Deleting answer...", true);
								        										try
								        										{
								        											List<NameValuePair> delParams = new ArrayList<NameValuePair>(3);
								        											delParams.add(new BasicNameValuePair("aid", Integer.toString(a.get(position).get_aid())));
								        											delParams.add(new BasicNameValuePair("idmod", Integer.toString(GlobalBO.loginID)));
								        											delParams.add(new BasicNameValuePair("version", GlobalBO.version));
								        											delPost.setEntity(new UrlEncodedFormEntity(delParams));		
								        										}
								        										catch(Exception e)
								        										{
								        											Toast.makeText(ViewQuestion.this, "Error logging into Server", Toast.LENGTH_SHORT).show();
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
								        											Toast.makeText(ViewQuestion.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
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
								        														Toast.makeText(ViewQuestion.this, "Unable to delete answer",Toast.LENGTH_SHORT).show();
								        													}
								        									            	else if(solvedResponse == -1)
								    										            	{
								    										            		Toast.makeText(ViewQuestion.this, "Your account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
								    										            		GlobalBO.loginID = 0;
								    										            		GlobalBO.loginUser = "";
								    										            		GlobalBO.isMod = false;
								    										            		GlobalBO.rememberMe = false;
								    										            		tv.setEnabled(true);
								    												    		tv.setText("Sign In to access all features of m4M");
								    										            	}
								        									            	else
								        									            	{
								        									            		Intent reload = new Intent(ViewQuestion.this, ViewQuestion.class);
								        									            		reload.putExtra("position", listPos);
								        									            		reload.putExtra("qid",QuestionId);
								        														startActivity(reload);
								        														ViewQuestion.this.finish();
								        									            	}
								        												}
								        												catch(Exception e)
								        												{
								        													Toast.makeText(ViewQuestion.this, "Unable to delete answer",Toast.LENGTH_SHORT).show();
								        												}
								        											}
								        											else
								        											{
								        												Toast.makeText(ViewQuestion.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
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
								        								public void onClick(DialogInterface dialog, int which) 	{	}
								        							})
								        							.create()
								        							.show();
										            		}
						        							return true;
						        						}
						        		    		});
							            		}
							        			return row;
							        		}
								}); }
						
					}
					
				}, listPos).execute();
				
				txt_quser.setTypeface(null, Typeface.NORMAL);
				if(GlobalBO.questions.get(listPos).is_teacherAns()==true)
				{
        			txt_quser.setTypeface(null, Typeface.ITALIC);
        		}
				txt_qtitle.setText(GlobalBO.questions.get(listPos).get_title());
				txt_quser.setText(GlobalBO.questions.get(listPos).get_askerid());
				txt_qdate.setText(GlobalBO.questions.get(listPos).get_askDate());
				
				
				bttn_incQRating.setOnClickListener(new rateElement(GlobalBO.questions.get(listPos).get_qid(), true, bttn_incQRating, txt_qrating, tv, ViewQuestion.this, new rateElement.postProcess() {
					
					@Override
					public void onMismatch() {
						UpdateBox();
					}
				}));
				
				layout.setOnLongClickListener(new View.OnLongClickListener() 
				{
					
					public boolean onLongClick(View v) 
					{
							if(isCompressed==false)
							{
								lines=txt_question.getLineCount();
								TitleLines=txt_qtitle.getLineCount();
								txt_question.setMaxLines(2);
								txt_qtitle.setMaxLines(1);
								txt_qtitle.setEllipsize(TruncateAt.END);
								txt_qtitle.setSingleLine(true);
								txt_question.setEllipsize(TruncateAt.END);
								isCompressed=true;
							}
							else
							{
								isCompressed=false;
								txt_question.setMaxLines(lines);
								txt_question.setEllipsize(null);
								txt_qtitle.setMaxLines(TitleLines);
								txt_qtitle.setEllipsize(null);
								txt_qtitle.setSingleLine(false);
							}
					
							return true;
					}
				});
				
				
	
		}
		catch(Exception e)
		{
			Toast.makeText(ViewQuestion.this, e.toString(), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		if(GlobalBO.loginID != 0)
			inflater.inflate(R.menu.qtmenu,menu);
		else
			inflater.inflate(R.menu.qtmenu1,menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch(item.getItemId()) 
	    {
	    	case R.id.qt_logout_menu :
	    	{	
	    		
	    		final AlertDialog.Builder box = new AlertDialog.Builder(ViewQuestion.this);
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
					
					public void onClick(DialogInterface dialog, int which) 
					{
						
					}
				})
				.create()
				.show();
	    	}	
	    	break;
	    	case R.id.qt_refresh_menu :
	    	{
	    		Intent i = new Intent(ViewQuestion.this,ViewQuestion.class);
	    		i.putExtra("qid",QuestionId);
	    		i.putExtra("position",listPos);
	    		startActivity(i);
	    		ViewQuestion.this.finish();
	    	}
	    	break;
	    }
	    return true;
	}
	
	@Override
	public void onBackPressed()
	{
		Intent back = new Intent(ViewQuestion.this, QuestionsScreen.class);
		startActivity(back);
		ViewQuestion.this.finish();
	}
}	
