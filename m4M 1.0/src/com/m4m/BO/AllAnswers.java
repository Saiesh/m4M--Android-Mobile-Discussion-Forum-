package com.m4m.BO;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AllAnswers 
{
	private int _ansrating;
	private String _answererid;
	private String _answer;
	private boolean _teacherAnswer;
	private boolean _isAnswer;
	private Date _answerDate;
	private int _aid;
		
	public int get_ansrating() 
	{
		return _ansrating;
	}
	public void set_ansrating(int _ansrating) 
	{
		this._ansrating = _ansrating;
	}
	public String get_answererid()
	{
		return _answererid;
	}
	public void set_answererid(String _answererid)
	{
		this._answererid = _answererid;
	}
	public String get_answer() 
	{
		return _answer;
	}
	public void set_answer(String _answer)
	{
		this._answer = _answer;
	}
	public boolean is_teacherAnswer()
	{
		return _teacherAnswer;
	}
	public void set_teacherAnswer(boolean _teacherAnswer) 
	{
		this._teacherAnswer = _teacherAnswer;
	}
	public boolean is_isAnswer() 
	{
		return _isAnswer;
	}
	public void set_isAnswer(boolean _isAnswer) 
	{
		this._isAnswer = _isAnswer;
	}
	public String get_answerDate() 
	{
		SimpleDateFormat format=new SimpleDateFormat("d MMM ''yy HH:mm");
		return format.format(_answerDate);
	}
	public void set_answerDate(Date _answerDate) 
	{
		this._answerDate = _answerDate;
	}
	public int get_aid() 
	{
		return _aid;
	}
	public void set_aid(int _aid) {
		this._aid = _aid;
	}
}
