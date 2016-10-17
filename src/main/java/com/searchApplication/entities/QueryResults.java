package com.searchApplication.entities;

import java.util.Map;

public class QueryResults {

	private Results data;
	private String dbName;
	private Long propertyId;
	private Map<String, String> stratums;

	public Map<String, String> getStratums()
	{
		return stratums;
	}

	public void setStratums( Map<String, String> stratums )
	{
		this.stratums = stratums;
	}

	public String getDbName()
	{
		return dbName;
	}

	public void setDbName( String dbName )
	{
		this.dbName = dbName;
	}

	public Long getPropertyId()
	{
		return propertyId;
	}

	public void setPropertyId( Long propertyId )
	{
		this.propertyId = propertyId;
	}

	public Results getData()
	{
		return data;
	}

	public void setData( Results data )
	{
		this.data = data;
	}
}
