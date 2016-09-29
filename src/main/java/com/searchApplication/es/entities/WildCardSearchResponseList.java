package com.searchApplication.es.entities;

import java.util.Set;

public class WildCardSearchResponseList {

    private String searchString;
    private Set<WildCardSearchResponse> searchResponse;

    public String getSearchString()
    {
        return searchString;
    }

    public void setSearchString( String searchString )
    {
        this.searchString = searchString;
    }

    public Set<WildCardSearchResponse> getSearchResponse()
    {
        return searchResponse;
    }

    public void setSearchResponse( Set<WildCardSearchResponse> searchResponse )
    {
        this.searchResponse = searchResponse;
    }
}
