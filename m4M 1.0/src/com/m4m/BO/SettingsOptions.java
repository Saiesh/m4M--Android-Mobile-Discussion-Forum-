package com.m4m.BO;

public class SettingsOptions {
	private String caption;
	private String description;

	public String getCaption() {
		return caption;
	}
	public String getDescription() {
		return description;
	}
	
	public SettingsOptions(String caption, String description)
	{
		this.caption = caption;
		this.description = description;
	}
}
