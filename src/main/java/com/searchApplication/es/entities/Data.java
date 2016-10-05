package com.searchApplication.es.entities;

import java.util.Map;
import java.util.Set;

import com.searchApplication.entities.Results;

public class Data {

	private Map<String, Set<Results>> seriesId;
	private String details;

	public Map<String, Set<Results>> getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId( Map<String, Set<Results>> seriesId )
	{
		this.seriesId = seriesId;
	}

	public String getDetails()
	{
		return details;
	}

	public void setDetails( String details )
	{
		this.details = details;
	}
}
