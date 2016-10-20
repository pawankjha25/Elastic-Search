package com.searchApplication.entities;

import java.util.Map;

public class Results {

	private Long seriesId;
	private Map<String,String> locations;
	
	public Long getSeriesId()
	{
		return seriesId;
	}
	
	public void setSeriesId( Long seriesId )
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
