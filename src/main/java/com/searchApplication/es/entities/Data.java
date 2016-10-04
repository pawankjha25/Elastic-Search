package com.searchApplication.es.entities;

import java.util.Set;

public class Data {

	private Set<Long> seriesId;
	private String details;

	public Set<Long> getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId( Set<Long> seriesId )
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
