package com.searchApplication.es.search.aggs;

import java.util.List;

public class RegionInfo {

	private String region;
	private List<String> subsectors;
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public List<String> getSubsectors() {
		return subsectors;
	}
	public void setSubsectors(List<String> subsectors) {
		this.subsectors = subsectors;
	}
	@Override
	public String toString() {
		return "RegionInfo [region=" + region + ", subsectors=" + subsectors + "]";
	}
	
	
	
	
}
