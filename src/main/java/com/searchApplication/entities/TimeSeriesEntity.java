package com.searchApplication.entities;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TimeSeriesEntity
{
	String seriesId;
	String tableName;
	BigDecimal value;
	String date;
	String period;
    String extended;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getExtended() {
		return extended;
	}

	public void setExtended(String extended) {
		this.extended = extended;
	}

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

	public BigDecimal getValue()
	{
		return value;
	}

	public void setValue(BigDecimal value)
	{
		this.value = value;
	}

	public TimeSeriesEntity(String seriesId, String tableName, BigDecimal value, String date, String period, String extended)
	{
		this.seriesId = seriesId;
		this.tableName = tableName;
		this.value = value;
		this.date = date;
		this.period=period;
		this.extended=extended;
	}
}
