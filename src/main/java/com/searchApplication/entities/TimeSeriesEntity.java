package com.searchApplication.entities;

import java.math.BigInteger;

public class TimeSeriesEntity
{
	BigInteger seriesId;
	String dbName;
	double value;
	String date;

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public BigInteger getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId(BigInteger seriesId)
	{
		this.seriesId = seriesId;
	}

	public String getDbName()
	{
		return dbName;
	}

	public void setDbName(String dbName)
	{
		this.dbName = dbName;
	}

	public double getValue()
	{
		return value;
	}

	public void setValue(double value)
	{
		this.value = value;
	}

	public TimeSeriesEntity(BigInteger j, String dbName, double d, String date)
	{
		this.seriesId = j;
		this.dbName = dbName;
		this.value = d;
		this.date = date;
	}
}
