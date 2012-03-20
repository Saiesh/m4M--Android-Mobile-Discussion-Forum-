package com.m4m.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.m4m.BO.AllQuestions;
import com.m4m.BO.GlobalBO;

public class glanceLoad extends AsyncTask<Void,Void,String>
{
	ProgressDialog d;
	Context mContext;
	postProcess ppObj;
	int currentCount;
	HttpClient m4MClient = new DefaultHttpClient();
	HttpPost glancePost = new HttpPost("http://" + GlobalBO.IP + "/glance.php");
	
	public interface postProcess
	{
		public void onSuccess();
		public void onFailure(int errorState);
	}
	
	public glanceLoad(Context mContext, postProcess ppObj)
	{
		this.mContext = mContext;
		this.ppObj = ppObj;
		this.currentCount = GlobalBO.questions.size();
		m4MClient.getParams().setIntParameter("http.connection.timeout", 5000);
	}
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		d = ProgressDialog.show(mContext, "Please Wait", "Populating content...", true);
		try
		{
			List<NameValuePair> glanceParams = new ArrayList<NameValuePair>(3);
			glanceParams.add(new BasicNameValuePair("base", Integer.toString(GlobalBO.base)));
			glanceParams.add(new BasicNameValuePair("limit", Integer.toString(GlobalBO.retrievalCount)));
			glanceParams.add(new BasicNameValuePair("version", GlobalBO.version));
			glancePost.setEntity(new UrlEncodedFormEntity(glanceParams));		
		}
		catch(Exception e)
		{
			Toast.makeText(mContext, "Error while retrieving data", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected String doInBackground(Void... params) 
	{
		try 
		{
			AllQuestions obj;
			JSONObject jObject;
			String res = getPage();
			if(res.equals("-2"))
				return "mismatch";
			
			JSONArray jsonArray = new JSONArray(res);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			for(int i = 0 ; i < jsonArray.length() ; i++)
			{	
				obj = new AllQuestions();
				jObject = jsonArray.getJSONObject(i);
				obj.set_askerid(jObject.getString("username"));
				obj.set_askDate(dateFormat.parse(jObject.getString("when")));
				obj.set_qid(Integer.parseInt(jObject.getString("idquestion")));
				obj.set_rating(Integer.parseInt(jObject.getString("rating")));
				obj.set_title(jObject.getString("title"));
				if(jObject.getString("issolved").equals("0"))
					obj.set_solved(false);
				else
					obj.set_solved(true);
				if(jObject.getString("teacherans").equals("0"))
					obj.set_teacherAns(false);
				else
					obj.set_teacherAns(true);
				GlobalBO.questions.add(obj);
			}
		} 
		catch (Exception e)
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
		
		if(result.equals("mismatch"))
			ppObj.onFailure(-2);
		else if(result.equals("ok"))
		{
			if(currentCount == GlobalBO.questions.size() && GlobalBO.questions.size() != 0)
			{
				GlobalBO.base -= GlobalBO.retrievalCount;
				Toast.makeText(mContext, "No additional questions", Toast.LENGTH_SHORT).show();
			}
			else if(GlobalBO.questions.size() == 0)
			{
				Toast.makeText(mContext, "No questions in database", Toast.LENGTH_SHORT).show();
			}
			try
			{
				ppObj.onSuccess();
			}
			catch(Exception e)
			{
				Toast.makeText(mContext, e.toString() + "1", Toast.LENGTH_SHORT).show();
				ppObj.onFailure(1);
			}
		}
		else
		{
			Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
			ppObj.onFailure(1);
		}
	}	
	public String getPage() throws Exception
	{
    	StringBuilder builder = new StringBuilder();
        try
    	{
        	HttpResponse glanceResp = m4MClient.execute(glancePost);
    		if(glanceResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
    		{
    			HttpEntity entity = glanceResp.getEntity();
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