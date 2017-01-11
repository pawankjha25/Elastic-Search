package com.searchApplication.es.entities;

public class Locations implements Comparable<Locations>

{

	private String series_id;
	private String location_name;
	private String location_type;
	private String location_meta;
	private String location_parent;

	public Locations( String series_id, String location_name, String location_type, String location_meta,
			String location_parent )
	{
		super();
		this.series_id = series_id;
		this.location_name = location_name;
		this.location_type = location_type;
		this.location_meta = location_meta;
		this.location_parent = location_parent;
	}

	public Locations()
	{

	}

	public String getSeries_id()
	{
		return series_id;
	}

	public void setSeries_id( String series_id )
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

	public String getLocation_parent()
	{
		return location_parent;
	}

	public void setLocation_parent( String location_parent )
	{
		this.location_parent = location_parent;
	}

}
