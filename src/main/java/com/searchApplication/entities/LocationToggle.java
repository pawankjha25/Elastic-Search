package com.searchApplication.entities;

import java.util.List;

public class LocationToggle {

	private String dbName;
	private String tableName;
	private List<Long> rowIds;

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public List<Long> getRowIds() {
		return rowIds;
	}

	public void setRowIds(List<Long> rowIds) {
		this.rowIds = rowIds;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
