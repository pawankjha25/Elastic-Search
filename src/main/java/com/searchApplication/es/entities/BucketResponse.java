package com.searchApplication.es.entities;

public class BucketResponse implements Comparable<BucketResponse> {

	private String suggestionString;
	private String sector;
	private String subSector;
	private String superRegion;
	// to do this will have multiple names as there are multiple strings?
	private String stratumName;
	private long totalRows;

	public String getSuggestionString() {
		return suggestionString;
	}

	public void setSuggestionString(String suggestionString) {
		this.suggestionString = suggestionString;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getSubSector() {
		return subSector;
	}

	public void setSubSector(String subSector) {
		this.subSector = subSector;
	}

	public String getSuperRegion() {
		return superRegion;
	}

	public void setSuperRegion(String superRegion) {
		this.superRegion = superRegion;
	}

	public String getStratumName() {
		return stratumName;
	}

	public void setStratumName(String stratumName) {
		this.stratumName = stratumName;
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

	@Override
	public int compareTo(BucketResponse o) {
		if (suggestionString.compareTo(o.suggestionString) == 0) {
			if (subSector.compareTo(o.subSector) == 0) {
				return 0;
			} else {
				return subSector.compareTo(o.subSector);
			}
		} else {
			return suggestionString.compareTo(o.suggestionString);
		}
	}

	@Override
	public String toString() {
		return "BucketResponse [suggestionString=" + suggestionString + ", sector=" + sector + ", subSector="
				+ subSector + ", superRegion=" + superRegion + ", stratumName=" + stratumName + ", totalRows="
				+ totalRows + "]";
	}

}
