package com.searchApplication.es.entities;

public class RowAttributes {

	private String attribute_name;
	private String attribute_value;
	private String attribute_type;
	private String attribute_parent;
	private int attribute_level;
	
	
	public RowAttributes(String attribute_value, String attribute_name, String attribute_type, String attribute_parent,
			int attribute_level) {
		super();
		this.attribute_name = attribute_name;
		this.attribute_value = attribute_value;
		this.attribute_type = attribute_type;
		this.attribute_parent = attribute_parent;
		this.attribute_level = attribute_level;
	}
	public String getAttribute_name() {
		return attribute_name;
	}
	public void setAttribute_name(String attribute_name) {
		this.attribute_name = attribute_name;
	}
	public String getAttribute_value() {
		return attribute_value;
	}
	public void setAttribute_value(String attribute_value) {
		this.attribute_value = attribute_value;
	}
	public String getAttribute_type() {
		return attribute_type;
	}
	public void setAttribute_type(String attribute_type) {
		this.attribute_type = attribute_type;
	}
	public String getAttribute_parent() {
		return attribute_parent;
	}
	public void setAttribute_parent(String attribute_parent) {
		this.attribute_parent = attribute_parent;
	}
	public int getAttribute_level() {
		return attribute_level;
	}
	public void setAttribute_level(int attribute_level) {
		this.attribute_level = attribute_level;
	}
	
	
}
