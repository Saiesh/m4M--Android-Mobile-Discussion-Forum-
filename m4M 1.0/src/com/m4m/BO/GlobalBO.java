package com.m4m.BO;

import java.util.ArrayList;

import android.content.Context;

public class GlobalBO 
{
	public static ArrayList<AllQuestions> questions = new ArrayList<AllQuestions>();
	public static ArrayList<AllAnswers> answers = new ArrayList<AllAnswers>();
	public static ArrayList<SettingsOptions> settings = new ArrayList<SettingsOptions>();
	public static ArrayList<Community> community = new ArrayList<Community>();
	public static String loginUser;
	public static int loginID;
	public static boolean rememberMe;
	public static boolean isMod;
	public static String version = "";
	public static String releasedate = "01.01.2012";
	public static String IP;
	public static int retrievalCount;
	public static int base = 0;
	public static String seed;
	public static Context mContext;
}
