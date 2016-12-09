package com.searchApplication.entities;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TimeSeriesEntity
{
	String seriesId;
	String dbName;
	BigDecimal value;
	String date;

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getSeriesId()
	{
		return seriesId;
	}

	public void setSeriesId(String seriesId)
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

	public BigDecimal getValue()
	{
		return value;
	}

	public void setValue(BigDecimal value)
	{
		this.value = value;
	}

	public TimeSeriesEntity(String seriesId, String dbName, BigDecimal value, String date)
	{
		this.seriesId = seriesId;
		this.dbName = dbName;
		this.value = value;
		this.date = date;
	}
}
