package com.searchApplication.entities;

public class Stratum implements Comparable<Stratum> {

	private String stratumName;
	private String parent;
	private String level;

	public String getStratumName()
	{
		return stratumName;
	}

	public void setStratumName( String stratumName )
	{
		this.stratumName = stratumName;
	}

	public String getParent()
	{
		return parent;
	}

	public void setParent( String parent )
	{
		this.parent = parent;
	}

	public String getLevel()
	{
		return level;
	}

	public void setLevel( String level )
	{
		this.level = level;
	}

	@Override
	public int compareTo( Stratum o )
	{
		return stratumName.compareTo(o.stratumName);
	}

}
