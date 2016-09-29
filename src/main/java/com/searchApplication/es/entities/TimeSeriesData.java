package com.searchApplication.es.entities;

import java.util.List;
import java.util.Set;

public class TimeSeriesData {

	private List<Attributes> attributes;
	private DatabaseInfo db;
	private Set<Locations> locations;
	private String sector;
	private String sub_sector;
	private String super_region;
	private List<String> description;

	public String getSector()
	{
		return sector;
	}

	public void setSector( String sector )
	{
		this.sector = sector;
	}

	public String getSub_sector()
	{
		return sub_sector;
	}

	public void setSub_sector( String sub_sector )
	{
		this.sub_sector = sub_sector;
	}

	public String getSuper_region()
	{
		return super_region;
	}

	public void setSuper_region( String super_region )
	{
		this.super_region = super_region;
	}

	public List<Attributes> getAttributes()
	{
		return attributes;
	}

	public void setAttributes( List<Attributes> attributes )
	{
		this.attributes = attributes;
	}

	public DatabaseInfo getDb()
	{
		return db;
	}

	public void setDb( DatabaseInfo db )
	{
		this.db = db;
	}

	public Set<Locations> getLocations()
	{
		return locations;
	}

	public void setLocations( Set<Locations> locations )
	{
		this.locations = locations;
	}

	public List<String> getDescription()
	{
		return description;
	}

	public void setDescription( List<String> description )
	{
		this.description = description;
	}

}
