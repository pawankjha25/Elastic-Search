package com.searchApplication.entities;

import java.util.Set;

public class LocationAggrigation implements Comparable<LocationAggrigation>

{

    private String locationParent;

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

}
