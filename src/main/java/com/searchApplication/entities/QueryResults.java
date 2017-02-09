package com.searchApplication.entities;

import java.util.Map;

public class QueryResults implements Comparable<QueryResults> {

	private Results data;
	private String dbName;
	private String encodedDbName;
	private String encodedTableName;
	private Long propertyId;
	private Map<String, String> stratums;

	public Map<String, String> getStratums() {
		return stratums;
	}

	public void setStratums(Map<String, String> stratums) {
		this.stratums = stratums;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public Long getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	public Results getData() {
		return data;
	}

	public void setData(Results data) {
		this.data = data;
	}

	@Override
	public int compareTo(QueryResults o) {
		if (this.data.getSeriesId() == o.data.getSeriesId()) {
			return 0;
		}
		return 1;
	}

	public String getEncodedDbName() {
		return encodedDbName;
	}

	public void setEncodedDbName(String encodedDbName) {
		this.encodedDbName = encodedDbName;
	}

	public String getEncodedTableName() {
		return encodedTableName;
	}

	public void setEncodedTableName(String encodedTableName) {
		this.encodedTableName = encodedTableName;
	}
}
