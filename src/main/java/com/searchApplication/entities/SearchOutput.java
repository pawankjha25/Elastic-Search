package com.searchApplication.entities;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchOutput {

	private String searchKey;
	private Map<String, List<String>> stratum;
	private Map<String, Set<LocationAggrigation>> locations;
	private Set<Stratum> stratumList;
	private Long totalRows;

	public Set<Stratum> getStratumList()
	{
		return stratumList;
	}

	public void setStratumList( Set<Stratum> stratumList )
	{
		this.stratumList = stratumList;
	}

	public Map<String, List<String>> getStratum()
	{
		return stratum;
	}

	public void setStratum( Map<String, List<String>> stratum )
	{
		this.stratum = stratum;
	}

	public String getSearchKey()
	{
		return searchKey;
	}

	public void setSearchKey( String searchKey )
	{
		this.searchKey = searchKey;
	}

	public Map<String, Set<LocationAggrigation>> getLocations()
	{
		return locations;
	}

	public void setLocations( Map<String, Set<LocationAggrigation>> map )
	{
		this.locations = map;
	}

	public Long getTotalRows()
	{
		return totalRows;
	}

	public void setTotalRows( Long totalRows )
	{
		this.totalRows = totalRows;
	}

}
