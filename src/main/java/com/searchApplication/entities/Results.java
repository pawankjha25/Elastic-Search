package com.searchApplication.entities;

import java.util.List;

public class Results {

	private Long seriesId;

	private String country;
	private String state;
	private String county;
	private String zipcode;

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

	public Long getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId( Long seriesId )
	{
		this.seriesId = seriesId;
	}

	public String getZipcode()
	{
		return zipcode;
	}

	public void setZipcode( String zipcode )
	{
		this.zipcode = zipcode;
	}

}
