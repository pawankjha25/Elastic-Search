package com.searchApplication.es.entities;

import java.util.List;

public class Row {

	private String sector;
	private String sub_sector;
	private String super_region;
	private List<String> description;
	private List<RowAttributes> attributes;
    private DBData db;
    private List<LocationData> locations;
	public String getSector() {
		return sector;
	}
	public void setSector(String sector) {
		this.sector = sector;
	}
	public String getSub_sector() {
		return sub_sector;
	}
	public void setSub_sector(String sub_sector) {
		this.sub_sector = sub_sector;
	}
	public String getSuper_region() {
		return super_region;
	}
	public void setSuper_region(String super_region) {
		this.super_region = super_region;
	}
	public List<String> getDescription() {
		return description;
	}
	public void setDescription(List<String> description) {
		this.description = description;
	}
	public List<RowAttributes> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<RowAttributes> attributes) {
		this.attributes = attributes;
	}
	public DBData getDb() {
		return db;
	}
	public void setDb(DBData db) {
		this.db = db;
	}
	public List<LocationData> getLocations() {
		return locations;
	}
	public void setLocations(List<LocationData> locations) {
		this.locations = locations;
	}
    
    

}
