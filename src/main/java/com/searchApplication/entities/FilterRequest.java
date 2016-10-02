package com.searchApplication.entities;

import java.util.List;
import java.util.Map;

public class FilterRequest {

    private String searchText;
    private Map<String, List<String>> filters;
    private Map<String, String> locations;

    public String getSearchText()
    {
        return searchText;
    }

    public void setSearchText( String searchText )
    {
        this.searchText = searchText;
    }

    public Map<String, List<String>> getFilters()
    {
        return filters;
    }

    public void setFilters( Map<String, List<String>> filters )
    {
        this.filters = filters;
    }

    public Map<String, String> getLocations()
    {
        return locations;
    }

    public void setLocations( Map<String, String> locations )
    {
        this.locations = locations;
    }

}
