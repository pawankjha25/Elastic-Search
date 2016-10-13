package com.searchApplication.es.entities;

public class PerformanceBucket implements Comparable<PerformanceBucket> {

	private String queryString;
	private Integer fetchSize;
	private long timeTakenToHitES;
	private long timeTakenToGetEsResults;
	private long timeTakenToProcessBuckets;
	private long timeTakenToSortBuckets;
	private double totalTimeTaken;

	public String getQueryString()
	{
		return queryString;
	}

	public void setQueryString( String queryString )
	{
		this.queryString = queryString;
	}

	public long getTimeTakenToHitES()
	{
		return timeTakenToHitES;
	}

	public void setTimeTakenToHitES( long timeTakenToHitES )
	{
		this.timeTakenToHitES = timeTakenToHitES;
		this.totalTimeTaken = this.totalTimeTaken + timeTakenToHitES;
	}

	public long getTimeTakenToGetEsResults()
	{
		return timeTakenToGetEsResults;
	}

	public void setTimeTakenToGetEsResults( long timeTakenToGetEsResults )
	{
		this.timeTakenToGetEsResults = timeTakenToGetEsResults;
		this.totalTimeTaken = this.totalTimeTaken + timeTakenToGetEsResults;
	}

	public long getTimeTakenToProcessBuckets()
	{
		return timeTakenToProcessBuckets;
	}

	public void setTimeTakenToProcessBuckets( long timeTakenToProcessBuckets )
	{
		this.timeTakenToProcessBuckets = timeTakenToProcessBuckets;
		this.totalTimeTaken = this.totalTimeTaken + timeTakenToProcessBuckets;
	}

	public Integer getFetchSize()
	{
		return fetchSize;
	}

	public void setFetchSize( Integer fetchSize )
	{
		this.fetchSize = fetchSize;
	}

	public long getTimeTakenToSortBuckets()
	{
		return timeTakenToSortBuckets;
	}

	public void setTimeTakenToSortBuckets( long timeTakenToSortBuckets )
	{
		this.timeTakenToSortBuckets = timeTakenToSortBuckets;
		this.totalTimeTaken = this.totalTimeTaken + timeTakenToSortBuckets;
	}

	public double getTotalTimeTaken()
	{
		return totalTimeTaken;
	}

	public void setTotalTimeTaken( double totalTimeTaken )
	{
		this.totalTimeTaken = totalTimeTaken;
	}

	@Override
	public int compareTo( PerformanceBucket o )
	{
		return 0;
	}

}
