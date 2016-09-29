package com.searchApplication.es.entities;

import java.util.List;

public class Data {

	private List<Long> seriesId;
	private String details;

	public List<Long> getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId( List<Long> seriesId )
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
