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

import com.m4m.BO.AllAnswers;
import com.m4m.BO.GlobalBO;

public class questionLoad extends AsyncTask<Void,Void,String>
{
	ProgressDialog d;
	Context mContext;
	postProcess ppObj;
	int listPos;
	HttpClient m4MClient = new DefaultHttpClient();
	HttpPost answerPost = new HttpPost("http://" + GlobalBO.IP + "/answers.php");
	
	public interface postProcess
	{
		public void onFailure(int errorState);
		public void onSuccess();
	}
	
	questionLoad(Context mContext, postProcess ppObj, int listPos)
	{
		this.mContext = mContext;
		this.ppObj = ppObj;
		this.listPos = listPos;
		m4MClient.getParams().setIntParameter("http.connection.timeout", 5000);
	}
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		d = ProgressDialog.show(mContext, "Please Wait", "Populating content...", true);
    	try
		{
			List<NameValuePair> answerParams = new ArrayList<NameValuePair>(2);
			answerParams.add(new BasicNameValuePair("qid", Integer.toString(GlobalBO.questions.get(listPos).get_qid())));
			answerParams.add(new BasicNameValuePair("version", GlobalBO.version));
			answerPost.setEntity(new UrlEncodedFormEntity(answerParams));		
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
			GlobalBO.answers.clear();
			AllAnswers obj;
			JSONObject jObject;
			String res = getPage();
			if(res.equals("-2"))
				return "mismatch";
			JSONArray jsonArray = new JSONArray(res);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			jObject = jsonArray.getJSONObject(0);
			GlobalBO.questions.get(listPos).set_question(jObject.getString("question"));
			if(jObject.getString("issolved").compareTo("0") == 0)
				GlobalBO.questions.get(listPos).set_solved(false);
			else
				GlobalBO.questions.get(listPos).set_solved(true);
			GlobalBO.questions.get(listPos).set_rating(Integer.parseInt(jObject.getString("rating")));
			for(int i = 1 ; i < jsonArray.length(); i++)
			{	
				obj = new AllAnswers();
				jObject = jsonArray.getJSONObject(i);
				obj.set_answererid(jObject.getString("username"));
				obj.set_answerDate(dateFormat.parse(jObject.getString("when")));
				obj.set_aid(Integer.parseInt(jObject.getString("idanswer")));
				obj.set_ansrating(Integer.parseInt(jObject.getString("rating")));
				obj.set_answer(jObject.getString("answer"));
				if(jObject.getString("issoln").compareTo("1") == 0)
					obj.set_isAnswer(true);
				else
					obj.set_isAnswer(false);
				if(jObject.getString("isteacher").compareTo("1") == 0)
					obj.set_teacherAnswer(true);
				else
					obj.set_teacherAnswer(false);
				GlobalBO.answers.add(obj);
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
		
		if(result.equals("-2"))
			ppObj.onFailure(-2);
		else if(result.equals("ok"))
		{
			try
			{
				ppObj.onSuccess();
			}
			catch(Exception e)
			{
				Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
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
			HttpResponse answerResp = m4MClient.execute(answerPost);	
			if(answerResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				HttpEntity entity = answerResp.getEntity();
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