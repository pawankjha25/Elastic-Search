package com.searchApplication.entities;

import java.util.Map;
import java.util.Set;

public class QueryResultsList {

	private Set<QueryResults> results;
	private Map<String, Set<LocationAggrigation>> locations;

	public Set<QueryResults> getResults() {
		return results;
	}

	public void setResults(Set<QueryResults> results) {
		this.results = results;
	}

	public Map<String, Set<LocationAggrigation>> getLocations() {
		return locations;
	}

	public void setLocations(Map<String, Set<LocationAggrigation>> map) {
		this.locations = map;
	}
}
