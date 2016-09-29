package com.searchApplication.es.entities;

public class WildCardSearchResponse implements Comparable<WildCardSearchResponse> {

    private String suggestionString;
    private String sector;
    private String subSector;
    private String superRegion;
    private String stratumName;

    public String getSuggestionString()
    {
        return suggestionString;
    }

    public void setSuggestionString( String suggestionString )
    {
        this.suggestionString = suggestionString;
    }

    public String getSector()
    {
        return sector;
    }

    public void setSector( String sector )
    {
        this.sector = sector;
    }

    public String getSubSector()
    {
        return subSector;
    }

    public void setSubSector( String subSector )
    {
        this.subSector = subSector;
    }

    public String getSuperRegion()
    {
        return superRegion;
    }

    public void setSuperRegion( String superRegion )
    {
        this.superRegion = superRegion;
    }

    public String getStratumName()
    {
        return stratumName;
    }

    public void setStratumName( String stratumName )
    {
        this.stratumName = stratumName;
    }

    @Override
    public int compareTo( WildCardSearchResponse o )
    {
        if( suggestionString.compareTo(o.suggestionString) == 0 )
        {
            if( subSector.compareTo(o.subSector) == 0 )
            {
                return 0;
            }
            else
            {
                return subSector.compareTo(o.subSector);
            }
        }
        else
        {
            return suggestionString.compareTo(o.suggestionString);
        }
    }

}
