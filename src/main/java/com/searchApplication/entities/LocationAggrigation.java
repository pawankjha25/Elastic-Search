package com.searchApplication.entities;

import java.util.Set;

public class LocationAggrigation implements Comparable<LocationAggrigation>

{

    private String locationParent;
    
    private long seriesIds;
    
    private int level;

    private Set<String> locations;

    public String getLocationParent()
    {
        return locationParent;
    }

    public void setLocationParent( String locationParent )
    {
        this.locationParent = locationParent;
    }

    public Set<String> getLocations()
    {
        return locations;
    }

    public void setLocations( Set<String> locations )
    {
        this.locations = locations;
    }

    @Override
    public int compareTo( LocationAggrigation o )
    {
        return 1;
    }

	public long getSeriesIds()
	{
		return seriesIds;
	}

	public void setSeriesIds( long seriesIds )
	{
		this.seriesIds = seriesIds;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel( int level )
	{
		this.level = level;
	}

}
