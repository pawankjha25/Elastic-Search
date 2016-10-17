package com.searchApplication.entities;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryResultsList {

	private List<QueryResults> results;
	private Map<String, Set<LocationAggrigation>> locations;

	public List<QueryResults> getResults()
	{
		return results;
	}

	public void setResults( List<QueryResults> results )
	{
		this.results = results;
	}

	public Map<String, Set<LocationAggrigation>> getLocations()
	{
		return locations;
	}

	public void setLocations( Map<String, Set<LocationAggrigation>> map )
	{
		this.locations = map;
	}
}
