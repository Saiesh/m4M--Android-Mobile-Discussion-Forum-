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
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.GlobalBO;
import com.m4m.BO.SettingsOptions;

public class SettingsScreen extends ListActivity 
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
	private ArrayList<SettingsOptions> s = GlobalBO.settings;
	
	void UpdateBox()
	{
		if(GlobalBO.loginID != 0)
		{
			Toast.makeText(SettingsScreen.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
			GlobalBO.loginID = 0;
			GlobalBO.loginUser = "";
			GlobalBO.isMod = false;
			GlobalBO.rememberMe = false;
		}
		final AlertDialog.Builder box = new AlertDialog.Builder(SettingsScreen.this);
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
		        	SettingsScreen.this.finish();
		        }
		        catch(Exception e)
		        {
		        	Toast.makeText(SettingsScreen.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
		        	SettingsScreen.this.finish();
		        }
			}
		})
		.setNegativeButton("Exit",new DialogInterface.OnClickListener()
		{
			
			public void onClick(DialogInterface dialog, int which) 
			{
				SettingsScreen.this.finish();
			}
		})
		.setCancelable(false)
		.create()
		.show();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		try {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setListAdapter(new ArrayAdapter<SettingsOptions>(SettingsScreen.this, R.layout.settingoption, s) {
			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				
				View row;
		 
				if (null == convertView) {
					row = mInflater.inflate(R.layout.settingoption, null);
				} 
				else {
					row = convertView;
				}
				
				TextView caption = (TextView)row.findViewById(R.id.caption);
				TextView description = (TextView)row.findViewById(R.id.description);
				
				caption.setText("" + s.get(position).getCaption());
				description.setText("" + s.get(position).getDescription());
				row.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						switch(position)
						{
						case 0: final Dialog addressDialog = new Dialog(SettingsScreen.this);
						
								addressDialog.setContentView(R.layout.settingdialog);
								addressDialog.setTitle("Change Settings");
								addressDialog.show();
									
								final EditText address = (EditText)addressDialog.findViewById(R.id.setting_value);				
								Button setAddress = (Button)addressDialog.findViewById(R.id.setting_set);
								TextView settingKey = (TextView)addressDialog.findViewById(R.id.setting_key);
								TextView settingRestore = (TextView)addressDialog.findViewById(R.id.setting_restore);
								
								address.setHint("Current: " + GlobalBO.IP);
								setAddress.setText("Set Address");
								settingKey.setText("Remote Server Address");
								
								settingRestore.setOnClickListener(new View.OnClickListener() {
									
									public void onClick(View v) {
										address.setText("m4m.x10.mx");
									}
								});
								
								setAddress.setOnClickListener(new View.OnClickListener() {
									
									public void onClick(View v) {
										if(address.getText().toString().compareTo("") == 0)
										{
											Toast.makeText(SettingsScreen.this, "Address field cannot be blank", Toast.LENGTH_SHORT).show();
										}
										else
										{
											GlobalBO.IP = address.getText().toString();
											Toast.makeText(SettingsScreen.this, "Server address changed", Toast.LENGTH_SHORT).show();
											addressDialog.dismiss();
										}
									}
								});
								break;
						case 1: final Dialog retrievalDialog = new Dialog(SettingsScreen.this);
						
								retrievalDialog.setContentView(R.layout.settingdialog);
								retrievalDialog.setTitle("Change Settings");
								retrievalDialog.show();
									
								final EditText limit = (EditText)retrievalDialog.findViewById(R.id.setting_value);				
								Button setLimit = (Button)retrievalDialog.findViewById(R.id.setting_set);
								TextView limitKey = (TextView)retrievalDialog.findViewById(R.id.setting_key);
								TextView limitRestore = (TextView)retrievalDialog.findViewById(R.id.setting_restore);
								
								limit.setHint("Current: " + GlobalBO.retrievalCount);
								setLimit.setText("Set Limit");
								limitKey.setText("Question Retrieval Limit");
								
								limitRestore.setOnClickListener(new View.OnClickListener() {
									
									public void onClick(View v) {
										limit.setText("25");
									}
								});
								
								setLimit.setOnClickListener(new View.OnClickListener() {
									
									public void onClick(View v) {
										if(limit.getText().toString().compareTo("") == 0)
										{
											Toast.makeText(SettingsScreen.this, "Limit field cannot be blank", Toast.LENGTH_SHORT).show();
										}
										else
										{
											try {
												int count = Integer.parseInt(limit.getText().toString());
												if(count <= 0)
													Toast.makeText(SettingsScreen.this, "Value must be greater than 0", Toast.LENGTH_SHORT).show();
												else
												{
													GlobalBO.retrievalCount = count;
													Toast.makeText(SettingsScreen.this, "Retrieval limit changed", Toast.LENGTH_SHORT).show();
													retrievalDialog.dismiss();
												}
											}
											catch(Exception e) {
												Toast.makeText(SettingsScreen.this, "Integer value must be specified", Toast.LENGTH_SHORT).show();
											}	
										}
									}
								});
								break;
						case 2: final Dialog cpwdDialog = new Dialog(SettingsScreen.this);
						
								cpwdDialog.setContentView(R.layout.changepwd);
								cpwdDialog.setTitle("Change Password");
								cpwdDialog.show();
									
								final EditText username = (EditText)cpwdDialog.findViewById(R.id.cpwd_username);
								final EditText curpwd = (EditText)cpwdDialog.findViewById(R.id.cpwd_curpwd);
								final EditText newpwd = (EditText)cpwdDialog.findViewById(R.id.cpwd_newpwd);
								final EditText cnewpwd = (EditText)cpwdDialog.findViewById(R.id.cpwd_cnewpwd);
								Button changepwd = (Button)cpwdDialog.findViewById(R.id.changepwd);
								
								if(!GlobalBO.loginUser.equals(""))
									username.setText(GlobalBO.loginUser);
								
								changepwd.setOnClickListener(new View.OnClickListener() 
								{
									@Override
									public void onClick(View v) 
									{
											if(username.getText().toString().equals("") ||
											   curpwd.getText().toString().equals("") ||
											   newpwd.getText().toString().equals("") ||
											   cnewpwd.getText().toString().equals(""))	
											{
												Toast.makeText(SettingsScreen.this, "All fields are required", Toast.LENGTH_SHORT).show();
											}
											else if(newpwd.getText().toString().length() < 8)
						            		{
						            			Toast.makeText(SettingsScreen.this, "Password must be minimum 8 characters", Toast.LENGTH_SHORT).show();
						            		}
											else if(newpwd.getText().toString().compareTo(cnewpwd.getText().toString()) != 0)
											{
												Toast.makeText(SettingsScreen.this, "Password confirmation mismatch", Toast.LENGTH_SHORT).show();
											}
											else
											{
												class changeTask extends AsyncTask<Void, Void, HttpResponse>
												{
													ProgressDialog d;
													HttpPost changePost = new HttpPost("http://" + GlobalBO.IP + "/changepwd.php");
													HttpClient m4MClient = new DefaultHttpClient();
													int changeResponse;
													@Override
													protected void onPreExecute()
													{
														super.onPreExecute();
														d = ProgressDialog.show(SettingsScreen.this, "Please Wait", "Changing Password...", true);
														try
														{
															List<NameValuePair> changeParams = new ArrayList<NameValuePair>(5);
															changeParams.add(new BasicNameValuePair("username", username.getText().toString().toLowerCase()));
															changeParams.add(new BasicNameValuePair("curpwd", m4MCrypt.encrypt(GlobalBO.seed, curpwd.getText().toString()).toString()));
															changeParams.add(new BasicNameValuePair("newpwd", m4MCrypt.encrypt(GlobalBO.seed, newpwd.getText().toString()).toString()));
															changeParams.add(new BasicNameValuePair("modcheck", "0"));
															changeParams.add(new BasicNameValuePair("version", GlobalBO.version));
															changePost.setEntity(new UrlEncodedFormEntity(changeParams));
														}
														catch(Exception e)
														{
															Toast.makeText(SettingsScreen.this, "Unable to change password", Toast.LENGTH_SHORT).show();
														}
													}
													
													@Override
													protected HttpResponse doInBackground(Void... params) 
													{
														m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
														try
														{
															return m4MClient.execute(changePost);
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
														
														if(result == null)
															Toast.makeText(SettingsScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
														else
														{
															if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
												    		{
																try
																{
																	changeResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
													            	
																	if(changeResponse == -1)
																	{
																		if(GlobalBO.loginID == 0)
																			Toast.makeText(SettingsScreen.this, "Your account was terminated by a Moderator", Toast.LENGTH_SHORT).show();
																		else
																		{
																			GlobalBO.loginID = 0;
																			GlobalBO.loginUser = "";
																			GlobalBO.rememberMe = false;
																			GlobalBO.isMod = false;
																			Toast.makeText(SettingsScreen.this, "Account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
																		}
																		cpwdDialog.dismiss();
																	}
																	else if(changeResponse == -2)
																	{
																		UpdateBox();
																	}
																	else if(changeResponse == 0)
																	{
																		Toast.makeText(SettingsScreen.this,"Invalid Username or Password",Toast.LENGTH_SHORT).show();
																		curpwd.setText("");
																		newpwd.setText("");
																		cnewpwd.setText("");
																	}
																	else if(changeResponse == -3)
																	{
																		cpwdDialog.dismiss();
																		Toast.makeText(SettingsScreen.this, "Your password has been changed", Toast.LENGTH_SHORT).show();
																	}
																	else 
																	{
																		final Dialog modDialog = new Dialog(SettingsScreen.this);
																		
																		modDialog.setContentView(R.layout.moderator);
																		modDialog.setTitle("Moderator Verification");
																		modDialog.show();
																			
																		final EditText modKey = (EditText)modDialog.findViewById(R.id.mod_key);				
																		final Button unlock = (Button)modDialog.findViewById(R.id.mod_unlock);
																		
																		try
																		{
																			unlock.setOnClickListener(new View.OnClickListener() {
																				
																				@Override
																				public void onClick(View v) 
																				{
																					if(modKey.getText().toString().equals(""))
																						Toast.makeText(SettingsScreen.this, "Key field cannot be blank", Toast.LENGTH_SHORT).show();
																					else if(Integer.parseInt(modKey.getText().toString()) == changeResponse)
																					{
																						class verifyTask extends AsyncTask<Void, Void, HttpResponse>
																						{
																							HttpPost verifyPost = new HttpPost("http://" + GlobalBO.IP + "/changepwd.php");
																						    ProgressDialog d;	
																							@Override
																							protected void onPreExecute()
																							{
																								super.onPreExecute();
																								d = ProgressDialog.show(SettingsScreen.this, "Please Wait", "Verifying Moderator Key...", true);
																								try
																								{
																									List<NameValuePair> verifyParams = new ArrayList<NameValuePair>(5);
																									verifyParams.add(new BasicNameValuePair("username", username.getText().toString().toLowerCase()));
																									verifyParams.add(new BasicNameValuePair("curpwd", m4MCrypt.encrypt(GlobalBO.seed, curpwd.getText().toString()).toString()));
																									verifyParams.add(new BasicNameValuePair("newpwd", m4MCrypt.encrypt(GlobalBO.seed, newpwd.getText().toString()).toString()));
																									verifyParams.add(new BasicNameValuePair("modcheck", "1"));
																									verifyParams.add(new BasicNameValuePair("version", GlobalBO.version));
																									verifyPost.setEntity(new UrlEncodedFormEntity(verifyParams));
																								}
																								catch(Exception e)
																								{
																									Toast.makeText(SettingsScreen.this, "Unable to change password", Toast.LENGTH_SHORT).show();
																								}
																							}
																							
																							@Override
																							protected HttpResponse doInBackground(Void... params) 
																							{
																								m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
																								try
																								{
																									return m4MClient.execute(verifyPost);
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
																								if(result == null)
																									Toast.makeText(SettingsScreen.this, "Could not connect to Server", Toast.LENGTH_SHORT).show();
																								else
																								{
																									if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
																						    		{
																										try
																										{
																											int verifyResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
																											if(verifyResponse == -2)
																											{
																												UpdateBox();
																											}
																											else if(verifyResponse == -3)
																											{
																												modDialog.dismiss();
																												cpwdDialog.dismiss();
																												Toast.makeText(SettingsScreen.this, "Your password has been changed", Toast.LENGTH_SHORT).show();
																											}
																											else
																											{
																												Toast.makeText(SettingsScreen.this, "Unable to change password", Toast.LENGTH_SHORT).show();
																											}
																										}
																										catch(Exception e)
																										{
																											Toast.makeText(SettingsScreen.this, "Unable to change password", Toast.LENGTH_SHORT).show();
																										}
																						    		}
																								}
																							}
																						}
																						new verifyTask().execute();	            	
																					}
																					else
																					{
																						Toast.makeText(SettingsScreen.this, "Moderator Key Mismatch", Toast.LENGTH_SHORT).show();
																					}
																				}
																			});
																		}
																		catch(Exception e)
																		{
																			Toast.makeText(SettingsScreen.this, "Unable to change password", Toast.LENGTH_SHORT).show();
																		}
																		
																	}
																}
																catch(Exception e)
																{
																	Toast.makeText(SettingsScreen.this, "Unable to change password", Toast.LENGTH_SHORT).show();
																}
												    		}
														}
													}		
												}
												new changeTask().execute();
											}
									}
								});
								
								break;
								
						default: break;
						}
						
					}
				});
				return row;
			}
		});
		}
		catch(Exception e)
		{
			Toast.makeText(SettingsScreen.this, e.toString(), Toast.LENGTH_LONG).show();
		}	
	}
	
	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
		Intent i= new Intent(SettingsScreen.this, MainScreen.class);
		startActivity(i);
		SettingsScreen.this.finish();
	}
}

