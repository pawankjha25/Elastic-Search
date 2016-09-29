package com.searchApplication.es.entities;

public class Locations implements Comparable<Locations>

{

    private Long series_id;
    private String location_name;
    private String location_type;
    private String location_meta;

    public Long getSeries_id()
    {
        return series_id;
    }

    public void setSeries_id( Long series_id )
    {
        this.series_id = series_id;
    }

    public String getLocation_name()
    {
        return location_name;
    }

    public void setLocation_name( String location_name )
    {
        this.location_name = location_name;
    }

    public String getLocation_type()
    {
        return location_type;
    }

    public void setLocation_type( String location_type )
    {
        this.location_type = location_type;
    }

    public String getLocation_meta()
    {
        return location_meta;
    }

    public void setLocation_meta( String location_meta )
    {
        this.location_meta = location_meta;
    }

    @Override
    public int compareTo( Locations o )
    {
        if( series_id.compareTo(o.series_id) == 0 )
        {
            if( location_type.compareTo(o.location_type) == 0 )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            return 1;
        }
    }

}
