package com.searchApplication.es.search.bucketing;

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
		StringBuilder builder = new StringBuilder();
		if(sector != null) {
			builder.append(sector);
		}
		if(subSector != null) {
			builder.append(subSector);
		}
		if(superRegion != null) {
			builder.append(superRegion);
		}
		return builder.toString().hashCode();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof BucketMetaData )
		{
			BucketMetaData b = (BucketMetaData) obj;
			boolean equals = true;
			if(equals == true) {
				if (sector != null) {
					if(sector.equals(b.getSector())) {
						equals = true;
					} else {
						equals = false;
					}
				} else {
					equals = b.getSector() == null;
				}
			}
			if(equals == true) {
				if (subSector != null) {
					if(subSector.equals(b.getSubSector())) {
						equals = true;
					} else {
						equals = false;
					}
				} else {
					equals = b.getSubSector() == null;
				}
			}
			if(equals == true) {
				if (superRegion != null) {
					if(superRegion.equals(b.getSuperRegion())) {
						equals = true;
					} else {
						equals = false;
					}
				} else {
					equals = b.getSuperRegion() == null;
				}
			}
			
			return equals;
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
