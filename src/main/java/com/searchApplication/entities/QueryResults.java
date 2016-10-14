package com.searchApplication.entities;

import java.util.List;
import com.searchApplication.es.entities.Data;

public class QueryResults implements Comparable<QueryResults> {

	private List<Data> data;
	private String dbName;
	private Long propertyId;

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

	@Override
	public int compareTo( QueryResults o )
	{
		return 1;
	}

	public List<Data> getData()
	{
		return data;
	}

	public void setData( List<Data> data )
	{
		this.data = data;
	}

}
