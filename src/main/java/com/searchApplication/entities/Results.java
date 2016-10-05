package com.searchApplication.entities;

public class Results implements Comparable<Results> {

	private Long seriesId;
	private String locationName;
	private String locationParent;

	public Long getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId( Long seriesId )
	{
		this.seriesId = seriesId;
	}

	public String getLocationName()
	{
		return locationName;
	}

	public void setLocationName( String locationName )
	{
		this.locationName = locationName;
	}

	@Override
	public int compareTo( Results o )
	{
		return seriesId.compareTo(o.seriesId);
	}

	public String getLocationParent()
	{
		return locationParent;
	}

	public void setLocationParent( String locationParent )
	{
		this.locationParent = locationParent;
	}

}
