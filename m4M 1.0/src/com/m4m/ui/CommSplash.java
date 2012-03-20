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
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.Community;
import com.m4m.BO.GlobalBO;

public class CommSplash extends Activity 
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
	//private ImageView commImage;
	//private Animation rotate;
	CommunityLoad myTask;
	TextView commText;
	ProgressBar commBar;
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.commsplash);
	        commBar = (ProgressBar)findViewById(R.id.commbar);
	        commText = (TextView)findViewById(R.id.commtxt);
			commText.setText("Loading Data, Please Wait");
	        //commImage = (ImageView)findViewById(R.id.comm_image);
	        //rotate = AnimationUtils.loadAnimation(CommSplash.this, R.anim.rotate_indefinitely);
	        //rotate.setInterpolator(new LinearInterpolator());
	        myTask = new CommunityLoad();
	        myTask.execute();
        }
		catch(Exception e)
		{
			Toast.makeText(CommSplash.this, e.toString(), Toast.LENGTH_LONG).show();
			quitActivity();
		}
    }
	class CommunityLoad extends AsyncTask<Void, Void, String>
	{
		HttpClient m4MClient = new DefaultHttpClient();
		HttpPost commPost = new HttpPost("http://" + GlobalBO.IP + "/commglance.php");
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			//commImage.startAnimation(rotate);
			try
			{
				List<NameValuePair> commParams = new ArrayList<NameValuePair>(2);
				commParams.add(new BasicNameValuePair("version", GlobalBO.version));
				commParams.add(new BasicNameValuePair("iduser", Integer.toString(GlobalBO.loginID)));
				commPost.setEntity(new UrlEncodedFormEntity(commParams));
			}
			catch(Exception e)
			{
				Toast.makeText(CommSplash.this, "Error loading Community data", Toast.LENGTH_SHORT).show();
				quitActivity();
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			try 
			{
				Community obj;
				JSONObject jObject;
				String res = getDetails();
				if(res.equals("-2"))
				{
					return "mismatch";
				}
				else if(res.equals("-1"))
				{
					return "terminated";
				}
				GlobalBO.community.clear();
				JSONArray jsonArray = new JSONArray(res);
				for(int i = 0 ; i < jsonArray.length() ; i++)
				{	
					obj = new Community();
					jObject = jsonArray.getJSONObject(i);
					obj.setUsername(jObject.getString("username"));
					obj.setName(jObject.getString("name"));
					GlobalBO.community.add(obj);
				}
			} 
			catch (Exception e)
			{
				return "Error loading Community data";
			}
			return "ok";
		}
		@Override
		protected void onPostExecute(String result) 
		{
			super.onPostExecute(result);
			//commImage.setAnimation(null);
			
			if(result.equals("mismatch"))
			{
				commBar.setVisibility(8);
	    		commText.setText("Aborted: Version Error");
				if(GlobalBO.loginID != 0)
        		{
					Toast.makeText(CommSplash.this, "You have been logged out from m4M", Toast.LENGTH_SHORT).show();
					GlobalBO.loginID = 0;
					GlobalBO.loginUser = "";
					GlobalBO.isMod = false;
					GlobalBO.rememberMe = false;
        		}
				final AlertDialog.Builder box = new AlertDialog.Builder(CommSplash.this);
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
				        	CommSplash.this.finish();
				        }
				        catch(Exception e)
				        {
				        	Toast.makeText(CommSplash.this,"Unable to upgrade m4M",Toast.LENGTH_LONG).show();
				        	CommSplash.this.finish();
				        }
					}
				})
				.setNegativeButton("Exit",new DialogInterface.OnClickListener()
				{
					
					public void onClick(DialogInterface dialog, int which) 
					{
						CommSplash.this.finish();
					}
				})
				.setCancelable(false)
				.create()
				.show();
			}
			else if(result.equals("terminated"))
			{
				GlobalBO.loginID = 0;
				GlobalBO.loginUser = "";
				GlobalBO.isMod = false;
				GlobalBO.rememberMe = false;
				Toast.makeText(CommSplash.this, "Account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
			}
			else if(!result.equals("ok"))
			{
				Toast.makeText(CommSplash.this, result, Toast.LENGTH_LONG).show();
				quitActivity();
			}
			else
			{
				try
				{
					Intent i = new Intent(CommSplash.this, CommGlance.class);
					startActivity(i);
					CommSplash.this.finish();
				}
				catch(Exception e)
				{
					Toast.makeText(CommSplash.this, e.toString(), Toast.LENGTH_SHORT).show();
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
	
	private void quitActivity()
	{
		Intent i = new Intent(CommSplash.this, MainScreen.class);
		startActivity(i);
		CommSplash.this.finish();
	}
	@Override
	public void onBackPressed()
	{
		try 
		{
			myTask.cancel(true);
			quitActivity();
		}
		catch(Exception e)
		{
			Toast.makeText(CommSplash.this, e.toString(), Toast.LENGTH_SHORT).show();
		}
	}
}