package com.searchApplication.entities;

import java.util.List;

public class TimeSeriesDataRequest {
	private List<String> seriesId;
	private String fromDate;
	private String toDate;
	private String period;
	private String dbName;
	private String tableName;

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

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

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public List<String> getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(List<String> seriesId) {
		this.seriesId = seriesId;
	}

}