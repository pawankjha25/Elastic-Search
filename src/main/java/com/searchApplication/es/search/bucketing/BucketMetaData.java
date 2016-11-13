package com.searchApplication.es.search.bucketing;

import com.google.gson.Gson;

public class BucketMetaData {

	private String superRegion;
	private String sector;
	private String subSector;
	private long total;

	public BucketMetaData( String superRegion, String sector, String subSector )
	{
		super();
		this.superRegion = superRegion;
		this.sector = sector;
		this.subSector = subSector;
		this.total = 1;
	}

	public void incrementCount()
	{
		this.total += 1;
	}

	public String getSuperRegion()
	{
		return superRegion;
	}

	public void setSuperRegion( String superRegion )
	{
		this.superRegion = superRegion;
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

	public long getTotal()
	{
		return total;
	}

	public void setTotal( long total )
	{
		this.total = total;
	}

	@Override
	public int hashCode()
	{
		return (sector + subSector + superRegion).hashCode();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof BucketMetaData )
		{
			BucketMetaData b = (BucketMetaData) obj;
			return sector.equals(b.getSector()) && subSector.equals(b.getSubSector())
					&& superRegion.equals(b.getSuperRegion());
		}
		else
		{
			return false;
		}

	}

	@Override
	public String toString()
	{
		return "BucketMetaData [superRegion=" + superRegion + ", sector=" + sector + ", subSector=" + subSector
				+ ", total=" + total + "]";
	}

}
