package com.searchApplication.entities;

import java.util.List;

public class Results {

	private List<Long> seriesId;

	private String superRegion;
	private String country;
	private String state;
	private String county;

	public String getCountry()
	{
		return country;
	}

	public void setCountry( String country )
	{
		this.country = country;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String state )
	{
		this.state = state;
	}

	public String getCounty()
	{
		return county;
	}

	public void setCounty( String county )
	{
		this.county = county;
	}

	public List<Long> getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId( List<Long> seriesId )
	{
		this.seriesId = seriesId;
	}

	public String getSuperRegion()
	{
		return superRegion;
	}

	public void setSuperRegion( String superRegion )
	{
		this.superRegion = superRegion;
	}

}
