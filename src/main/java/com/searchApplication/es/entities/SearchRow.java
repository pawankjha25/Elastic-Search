package com.searchApplication.es.entities;

import java.util.List;

public class SearchRow implements Comparable<SearchRow> {

    private List<String> description;
    private String sector;
    private String subSector;
    private String superRegion;
    private String bucket;

    public List<String> getDescription()
    {
        return description;
    }

    public void setDescription( List<String> description )
    {
        this.description = description;
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

    public String getBucket()
    {
        return bucket;
    }

    public void setBucket( String bucket )
    {
        this.bucket = bucket;
    }

    @Override
    public int compareTo( SearchRow o )
    {
        if( bucket.compareTo(o.bucket) == 0 )
        {
            if( subSector.compareTo(o.bucket) == 0 )
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
            return bucket.compareTo(o.bucket);
        }
    }
}
