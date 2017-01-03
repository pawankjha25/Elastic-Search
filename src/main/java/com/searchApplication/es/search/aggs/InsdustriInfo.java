package com.searchApplication.es.search.aggs;

import java.util.List;

public class InsdustriInfo {

	private String sector;
	private List<RegionInfo> regions;
	public String getSector() {
		return sector;
	}
	public void setSector(String sector) {
		this.sector = sector;
	}
	public List<RegionInfo> getRegions() {
		return regions;
	}
	public void setRegions(List<RegionInfo> regions) {
		this.regions = regions;
	}
	@Override
	public String toString() {
		return "InsdustriInfo [sector=" + sector + ", regions=" + regions + "]";
	}
	
	
	
	
	
}
