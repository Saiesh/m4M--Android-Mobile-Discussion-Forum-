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

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.m4m.BO.AllAnswers;
import com.m4m.BO.AllQuestions;
import com.m4m.BO.GlobalBO;

public class rateElement implements View.OnClickListener
{
	int id;
	boolean isQuestion;
	ImageButton ratingButton;
	TextView ratingText, tv;
	Context mContext;
	postProcess ppObj;
	
	public interface postProcess
	{
		public void onMismatch();
	}
	
	rateElement(int id, boolean isQuestion, ImageButton ratingButton, TextView ratingText, TextView tv, Context mContext, postProcess ppObj)
	{
		this.id = id;
		this.isQuestion = isQuestion;
		this.ratingButton = ratingButton;
		this.ratingText = ratingText;
		this.mContext = mContext;
		this.tv = tv;
		this.ppObj = ppObj;
	}
	
	class postRating extends AsyncTask<Void, Void, HttpResponse>
	{
		HttpClient m4MClient = new DefaultHttpClient();
		HttpPost ratingPost;
		
		@Override
		protected void onPreExecute()
		{
			ratingButton.setBackgroundResource(R.drawable.uparrowpressed);
			if(isQuestion)
				ratingPost = new HttpPost("http://" + GlobalBO.IP + "/ratequestion.php");
			else
				ratingPost = new HttpPost("http://" + GlobalBO.IP + "/rateanswer.php");
			try
			{
				List<NameValuePair> ratingParams = new ArrayList<NameValuePair>(3);
				if(isQuestion)
					ratingParams.add(new BasicNameValuePair("idquestion", Integer.toString(id)));
				else
					ratingParams.add(new BasicNameValuePair("idanswer", Integer.toString(id)));
				ratingParams.add(new BasicNameValuePair("iduser", Integer.toString(GlobalBO.loginID)));
				ratingParams.add(new BasicNameValuePair("version", GlobalBO.version));
				ratingPost.setEntity(new UrlEncodedFormEntity(ratingParams));
			}
			catch(Exception e)
			{
				Toast.makeText(mContext, "Unable to increase rating", Toast.LENGTH_SHORT).show();
			}
		}
		@Override
		protected HttpResponse doInBackground(Void... params) 
		{
			m4MClient.getParams().setIntParameter("http.connection.timeout",5000);
			try
			{
				return m4MClient.execute(ratingPost);
			}
			catch(Exception e)
			{
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(HttpResponse result)
		{
			ratingButton.setBackgroundResource(R.drawable.uparrow);			
			if(result == null)
				Toast.makeText(mContext, "Unable to increase rating", Toast.LENGTH_SHORT).show();
			else
			{
				if(result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
	    		{
					try
					{
						int ratingResponse = Integer.parseInt(EntityUtils.toString(result.getEntity()));
		            	if(ratingResponse == 0)
						{
							Toast.makeText(mContext,"Unable to increase rating",Toast.LENGTH_SHORT).show();
						}
		            	else if(ratingResponse == -2)
		            	{
		            		ppObj.onMismatch();
		            	}
		            	else if(ratingResponse == -1)
		            	{
		            		Toast.makeText(mContext, "Your account was terminated by a Moderator, you have been logged out", Toast.LENGTH_SHORT).show();
		            		GlobalBO.loginID = 0;
		            		GlobalBO.loginUser = "";
		            		GlobalBO.isMod = false;
		            		GlobalBO.rememberMe = false;
		            		tv.setEnabled(true);
				    		tv.setText("Sign In to access all features of m4M");
		            	}
		            	else
		            	{
		            		int rating = Integer.parseInt(ratingText.getText().toString());
							rating += 1;
							ratingText.setText("" + rating);
						}
					}
					catch(Exception e)
					{
						Toast.makeText(mContext, "Unable to increase rating", Toast.LENGTH_SHORT).show();
					}
	    		}
			}
		}
	}
	
	public void onClick(View arg0) 
	{
		String currUser = null;
		if(isQuestion)
		{
			for(AllQuestions q: GlobalBO.questions)
			{
				if(q.get_qid() == id)
					currUser = q.get_askerid();
			}
		}
		else
		{
			for(AllAnswers a: GlobalBO.answers)
			{
				if(a.get_aid() == id)
					currUser = a.get_answererid();
			}	
		}
		if(GlobalBO.loginID != 0)
		{
			if(GlobalBO.loginUser.compareTo(currUser) == 0)
				Toast.makeText(mContext, "You cannot rate your own questions/answers", Toast.LENGTH_SHORT).show();
			else
				new postRating().execute();
		}	
		else
			Toast.makeText(mContext, "You must be logged in to rate questions or answers", Toast.LENGTH_SHORT).show();
	}
	
}
