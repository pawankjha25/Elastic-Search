package com.searchApplication.entities;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterRequest {

	private String searchText;
	private Map<String, List<String>> filters;
	private Map<String, Set<String>> locations;
	private String stratumName;
	private List<String> reqAttList;
	private Boolean location;

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public Map<String, List<String>> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, List<String>> filters) {
		this.filters = filters;
	}

	public Map<String, Set<String>> getLocations() {
		return locations;
	}

	public void setLocations(Map<String, Set<String>> locations) {
		this.locations = locations;
	}

	public String getStratumName() {
		return stratumName;
	}

	public void setStratumName(String stratumName) {
		this.stratumName = stratumName;
	}

	public Boolean getLocation() {
		return location;
	}

	public void setLocation(Boolean location) {
		this.location = location;
	}

	public List<String> getReqAttList() {
		return reqAttList;
	}

	public void setReqAttList(List<String> reqAttList) {
		this.reqAttList = reqAttList;
	}

}
