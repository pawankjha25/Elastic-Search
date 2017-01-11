package com.searchApplication.es.entities;

public class LocationData {

	private String series_id;
	private String location_name;
	private String location_type;
	private String location_meta;

	public LocationData( String series_id, String location_name, String location_type, String location_meta )
	{
		super();
		this.series_id = series_id;
		this.location_name = location_name;
		this.location_type = location_type;
		this.location_meta = location_meta;
	}

	public LocationData()
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

}
