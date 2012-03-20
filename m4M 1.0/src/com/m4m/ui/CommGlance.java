package com.m4m.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.json.JSONArray;
import org.json.JSONObject;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.Community;
import com.m4m.BO.GlobalBO;

public class CommGlance extends ListActivity 
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
	private EditText searchText;
	private Spinner sortSpinner;
	LayoutInflater mInflater;
	private TextView noComm;
	private String myMobile, myEmail, myName;
	ArrayAdapter<Community> commAdapter;
	ArrayList<Community> c = GlobalBO.community;
	BeanComparator bcname, bcuser;
	ArrayList<Community> filtered = new ArrayList<Community>();
	private int searchLength = 0;
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.commglance);
	        bcname = new BeanComparator(Community.class, "getName");
	        bcuser = new BeanComparator(Community.class, "getUsername");
	        noComm = (TextView)findViewById(R.id.nocomm);
	        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        searchText = (EditText)findViewById(R.id.searchtext);
	        searchText.setHint("Type to search");
	        searchText.requestFocus();
	        filtered = (ArrayList<Community>) c.clone();
	        
	        sortSpinner = (Spinner)findViewById(R.id.sortspinner);
	        commAdapter = new CommAdapter(CommGlance.this, R.layout.contact, filtered);
			setListAdapter(commAdapter);
	        
	        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
	                this, R.array.sort, android.R.layout.simple_spinner_item);
	        
	        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        sortSpinner.setAdapter(sortAdapter);
	        sortSpinner.setSelection(0);
	        
	        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
				{
					if(arg2 == 0)
					{	
						Collections.sort(filtered, bcname);
						commAdapter.notifyDataSetChanged();
					}
					else
					{
						Collections.sort(filtered, bcuser);
						commAdapter.notifyDataSetChanged();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {}
			});
	        
	        searchText.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) 
				{
					searchLength = searchText.getText().length();
					filtered.clear();
					if(sortSpinner.getSelectedItemPosition() == 0)
					{
						for(int i = 0 ; i < c.size() ; ++i)
						{
							String x = c.get(i).getName();
							if(searchLength <= x.length())
							{
								if(x.toLowerCase().contains(searchText.getText().toString().toLowerCase()))
								{
									filtered.add(c.get(i));
								}
							}
							if(filtered.size() > 0)
							{
								Collections.sort(filtered, bcname);
								noComm.setVisibility(8);
							}
							else
							{
								noComm.setVisibility(0);
							}
						}
					}
					else
					{
						for(int i = 0 ; i < c.size() ; ++i)
						{
							String x = c.get(i).getUsername();
							if(searchLength <= x.length())
							{
								if(x.toLowerCase().contains(searchText.getText().toString().toLowerCase()))
								{
									filtered.add(c.get(i));
								}
							}
							if(filtered.size() > 0)
							{
								Collections.sort(filtered, bcuser);
								noComm.setVisibility(8);
							}
							else
							{
								noComm.setVisibility(0);
							}
						}
					}
					commAdapter.notifyDataSetChanged();
				}
	        });
		}
		catch(Exception e)
		{
			Toast.makeText(CommGlance.this, e.toString(), Toast.LENGTH_LONG).show();
			quitActivity();
		}
    }
	
	class CommAdapter extends ArrayAdapter<Community>
	{
		public CommAdapter(Context context, int textViewResourceId, ArrayList<Community> x) 
		{
			super(context, textViewResourceId, x);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) 
    	{
    		View row;
    		if (null == convertView) 
    		{
    			row = mInflater.inflate(R.layout.contact, null);
    		} 
    		else 
    		{
    			row = convertView;
    		}
    		
    		try
    		{
    			final TextView name = (TextView)row.findViewById(R.id.commname);
        		final TextView username = (TextView)row.findViewById(R.id.commuser);
        		
        		name.setText(filtered.get(position).getName());
        		username.setText(filtered.get(position).getUsername());
        		
        		row.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						final Dialog commDialog = new Dialog(CommGlance.this);
						commDialog.setContentView(R.layout.commdetails);
						commDialog.setTitle("User Details");
											
						final TextView commName = (TextView)commDialog.findViewById(R.id.comm_name);
						final TextView commUsername = (TextView)commDialog.findViewById(R.id.comm_username);
						final TextView commJoined = (TextView)commDialog.findViewById(R.id.comm_joined);
						final TextView commDesignation = (TextView)commDialog.findViewById(R.id.comm_designation);
						final TextView commQuestions = (TextView)commDialog.findViewById(R.id.comm_questions);
						final TextView commAnswers = (TextView)commDialog.findViewById(R.id.comm_answers);
						final TextView commSolutions = (TextView)commDialog.findViewById(R.id.comm_solutions);
						final TextView commQAggregate = (TextView)commDialog.findViewById(R.id.comm_qaggregate);
						final TextView commAnAggregate = (TextView)commDialog.findViewById(R.id.comm_anaggregate);
						final TextView commContact = (TextView)commDialog.findViewById(R.id.comm_contact);
						Button commClose = (Button)commDialog.findViewById(R.id.commclose);
						
						commClose.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								commDialog.dismiss();	
							}
						});
						
						class CommTask extends AsyncTask<Void, Void, String>
						{
							ProgressDialog d;
							HttpPost commPost = new HttpPost("http://" + GlobalBO.IP + "/commdetails.php");
							HttpClient m4MClient = new DefaultHttpClient();
							private String contactEmail;
							@Override
							protected void onPreExecute()
							{
								super.onPreExecute();
								d = ProgressDialog.show(CommGlance.this, "Please Wait", "Retrieving details...", true);
								try
								{
									List<NameValuePair> commParams = new ArrayList<NameValuePair>(3);
									commParams.add(new BasicNameValuePair("username", filtered.get(position).getUsername()));
									commParams.add(new BasicNameValuePair("version", GlobalBO.version));
									commParams.add(new BasicNameValuePair("iduser", Integer.toString(GlobalBO.loginID)));
									commPost.setEntity(new UrlEncodedFormEntity(commParams));
								}
								catch(Exception e)
								{
									Toast.makeText(CommGlance.this, "Unable to retrieve user details", Toast.LENGTH_SHORT).show();
								}
							}
							
							@Override
							protected String doInBackground(Void... params) {
								try 
								{
									String res = getDetails();
									SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									SimpleDateFormat format = new SimpleDateFormat("d MMM ''yy HH:mm");
									
									if(res.equals("-2"))
									{
										return "mismatch";
									}
									else if(res.equals("-1"))
									{
										GlobalBO.loginID = 0;
										GlobalBO.loginUser = "";
										GlobalBO.rememberMe = false;
										GlobalBO.isMod = false;
										return "Account was terminated by a Moderator, you have been logged out";
									}
									
									JSONArray jArray = new JSONArray(res);
									JSONObject jObject;
									
									jObject = jArray.getJSONObject(0);
									myMobile = jObject.getString("phone");
									myEmail = jObject.getString("email");
									myName = jObject.getString("name");
									
									jObject = jArray.getJSONObject(1);
									contactEmail= jObject.getString("email");
									commName.setText(filtered.get(position).getName());
									commUsername.setText(filtered.get(position).getUsername());
									commJoined.setText(format.format(dateFormat.parse(jObject.getString("joined"))));
									if(jObject.getString("ismod").equals("1"))
									{
										commDesignation.setText("Moderator");
									}
									else if(jObject.getString("isteacher").equals("1"))
									{
										commDesignation.setText("Teacher");
									}
									else
									{
										commDesignation.setText("Member");
									}
									commQuestions.setText(jObject.getString("qposted"));
									commAnswers.setText(jObject.getString("anposted"));
									commSolutions.setText(jObject.getString("sposted"));
									if(jObject.getString("qaggregate").equals("null"))
									{
										commQAggregate.setText("0");
									}
									else
										commQAggregate.setText(jObject.getString("qaggregate"));
									if(jObject.getString("anaggregate").equals("null"))
									{
										commAnAggregate.setText("0");
									}
									else
										commAnAggregate.setText(jObject.getString("anaggregate"));
								} 
								catch (Exception e)
								{
									return e.toString();
								}
								return "ok";
							}
							
							@Override
							protected void onPostExecute(String result) 
							{
								super.onPostExecute(result);
								d.dismiss();
								if(result.equals("mismatch"))
								{
									if(GlobalBO.loginID != 0)
				            		{
										Toast.makeText(CommGlance.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
										GlobalBO.loginID = 0;
										GlobalBO.loginUser = "";
										GlobalBO.isMod = false;
										GlobalBO.rememberMe = false;
				            		}
									final AlertDialog.Builder box = new AlertDialog.Builder(CommGlance.this);
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
									        	CommGlance.this.finish();
									        }
									        catch(Exception e)
									        {
									        	Toast.makeText(CommGlance.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
									        	CommGlance.this.finish();
									        }
										}
									})
									.setNegativeButton("Exit",new DialogInterface.OnClickListener()
									{
										
										public void onClick(DialogInterface dialog, int which) 
										{
											CommGlance.this.finish();
										}
									})
									.setCancelable(false)
									.create()
									.show();
								}
								else if(!result.equals("ok"))
								{
									Toast.makeText(CommGlance.this, result, Toast.LENGTH_LONG).show();
									quitActivity();
								}
								else
								{
									try
									{
										commDialog.show();
										commContact.setOnClickListener(new View.OnClickListener() {
											
											@Override
											public void onClick(View v) 
											{
												final Dialog detailsDialog = new Dialog(CommGlance.this);
												
												detailsDialog.setContentView(R.layout.requestcontact);
												detailsDialog.setTitle("Request Details");
																	
												final TextView detailsName = (TextView)detailsDialog.findViewById(R.id.details_name);
												Button sendDetails = (Button)detailsDialog.findViewById(R.id.send_details);
												detailsName.setText("An E-Mail will be sent to " + filtered.get(position).getName() + " with your following details:");
												
												sendDetails.setOnClickListener(new View.OnClickListener() {
													
													@Override
													public void onClick(View v) {
														class SendMail extends AsyncTask<Void,Void,Boolean>
														{
															ProgressDialog d;
															GMailSender sender;
															String body;
															@Override
															protected void onPreExecute()
															{
																d = ProgressDialog.show(CommGlance.this,"Please Wait","Sending Details...",true);
																sender = new GMailSender("no.reply.m4m", "m4mhabibul");
																body =    filtered.get(position).getName() + ",\n\n"
																		+ myName + ", a member of m4M Community with username " 
																		+ GlobalBO.loginUser 
																		+ " has requested your contact details.\n"
																		+ "The user's contact details are:\n"
																		+ "E-Mail Address: " + myEmail + "\n"
																		+ "Mobile Number: " + myMobile + "\n"
																		+ "You may reply to any of the contacts if you please.\n\n"
																		+ "To report abuse, please reply to this mail with the message body \"Abuse\".\n\n"
																		+ "Regards\n\n"
																		+ "m4M Community\n";
															}
															
															@Override
															protected Boolean doInBackground(Void... params) {
																try {
																	sender.sendMail("m4M Community Contact Request",   
												                    		body,   
												                            "no.reply.m4m@gmail.com",   
												                            contactEmail); 
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
																	Toast.makeText(CommGlance.this, "Error sending Details", Toast.LENGTH_SHORT).show(); 
																}
																else
																{
																	Toast.makeText(CommGlance.this, "An E-Mail has been sent to " + filtered.get(position).getName(), Toast.LENGTH_SHORT).show();
																	detailsDialog.dismiss();
																}
															}
														}
														new SendMail().execute();
													}
												});
												
												detailsDialog.show();
											}
										});
									}
									catch(Exception e)
									{
										Toast.makeText(CommGlance.this, e.toString(), Toast.LENGTH_SHORT).show();
										quitActivity();
									}
								}
							}	
							
							public String getDetails() throws Exception
							{
						    	StringBuilder builder = new StringBuilder();
						        try
						    	{
						        	HttpResponse commResp = m4MClient.execute(commPost);
						    		if(commResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
						    		{
						    			HttpEntity entity = commResp.getEntity();
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
						new CommTask().execute();
					}
				});	
    		}
    		catch(Exception e)
    		{
    			Toast.makeText(CommGlance.this, e.toString(), Toast.LENGTH_LONG).show();
    			quitActivity();
    		}
    		return row;
    	}		    		
	}
	
	@Override
	public void onBackPressed()
	{
		try 
		{
			quitActivity();
		}
		catch(Exception e)
		{
			Toast.makeText(CommGlance.this, e.toString(), Toast.LENGTH_LONG).show();
		}
	}
	public void quitActivity()
	{
		Intent i = new Intent(CommGlance.this, MainScreen.class);
		startActivity(i);
		CommGlance.this.finish();
	}
}