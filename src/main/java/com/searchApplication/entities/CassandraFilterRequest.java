package com.searchApplication.entities;

public class CassandraFilterRequest
{
	public String seriesId;
	public String dbName;
	public String fromDate;
	public String toDate;

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String period;

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

	public String getFromDate()
	{
		return fromDate;
	}

	public void setFromDate(String fromDate)
	{
		this.fromDate = fromDate;
	}

	public String getToDate()
	{
		return toDate;
	}

	public void setToDate(String toDate)
	{
		this.toDate = toDate;
	}

	@Override
	public String toString()
	{
		return " seriesId : " + seriesId + ", dbName : " + dbName + ", period : " + period + ", from : " + fromDate + " to : " + toDate;
	}

}
