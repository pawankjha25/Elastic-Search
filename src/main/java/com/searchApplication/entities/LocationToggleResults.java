package com.searchApplication.entities;

import java.util.Map;

public class LocationToggleResults {

	private String id;
	private Map<String, String> locations;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, String> getLocations() {
		return locations;
	}

	public void setLocations(Map<String, String> locations) {
		this.locations = locations;
	}

}
