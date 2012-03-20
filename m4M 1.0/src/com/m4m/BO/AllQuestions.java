package com.m4m.BO;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AllQuestions 
{
	private int _rating;
	private String _askerid;
	private String _title;
	private String _question;
	private boolean _teacherAns;
	private boolean _solved;
	private Date _askDate;
	private int _qid;
	
	public int get_qid() {
		return _qid;
	}
	public void set_qid(int _qid) {
		this._qid = _qid;
	}
	public String get_askerid() {
		return _askerid;
	}
	public void set_askerid(String _askerid) {
		this._askerid = _askerid;
	}
	public int get_rating() {
		return _rating;
	}
	public void set_rating(int _rating) {
		this._rating = _rating;
	}
	public String get_title() {
		return _title;
	}
	public void set_title(String _title) {
		this._title = _title;
	}
	public boolean is_teacherAns() {
		return _teacherAns;
	}
	public void set_teacherAns(boolean _teacherAns) {
		this._teacherAns = _teacherAns;
	}
	public boolean is_solved() {
		return _solved;
	}
	public void set_solved(boolean _solved) {
		this._solved = _solved;
	}
	public String get_askDate() 
	{
		SimpleDateFormat format=new SimpleDateFormat("d MMM ''yy HH:mm");
		return format.format(_askDate);
	}
	public void set_askDate(Date _askDate) {
		this._askDate = _askDate;
	}
	public void set_question(String _question) {
		this._question = _question;
	}
	public String get_question() {
		return _question;
	}	
}
