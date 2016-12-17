package com.searchApplication.entities;

import java.util.Date;

public class SeriesIdStatistics {

	private String seriesID;
	private String tableName;
	private Date startDate;
	private Date endDate;
	private int rowCount;

	public SeriesIdStatistics(String seriesID, String tableName, Date startDate, Date endDate, int rowCount) {
		this.seriesID = seriesID;
		this.tableName = tableName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.rowCount = rowCount;
	}

	@Override
	public String toString() {
		return "SeriesIdStatistics [seriesID=" + seriesID + ", tableName=" + tableName + ", startDate=" + startDate + ", endDate=" + endDate + ", rowCount=" + rowCount + "]";
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getSeriesID() {
		return seriesID;
	}

	public void setSeriesID(String seriesID) {
		this.seriesID = seriesID;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void incrementRowCount() {
		this.rowCount++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((seriesID == null) ? 0 : seriesID.hashCode());
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeriesIdStatistics other = (SeriesIdStatistics) obj;
		if (seriesID == null) {
			if (other.seriesID != null)
				return false;
		} else if (!seriesID.equals(other.seriesID))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

}
