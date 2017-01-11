package com.searchApplication.entities;

import java.util.Map;

public class Results {

	private String seriesId;
	private Map<String,String> locations;
	
	public String getSeriesId()
	{
		return seriesId;
	}
	
	public void setSeriesId( String seriesId )
	{
		this.seriesId = seriesId;
	}
	
	public Map<String, String> getLocations()
	{
		return locations;
	}
	
	public void setLocations( Map<String, String> locations )
	{
		this.locations = locations;
	}
}
