package org.fundaciotapies.ac.model.support;

import java.util.List;

public class DataMapping {

	private String name;
	private String type;
	private List<String> path;
	private List<String> value;
	private String category;
	
	private String sortBy;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getPath() {
		return path;
	}
	public void setPath(List<String> path) {
		this.path = path;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}
	public List<String> getValue() {
		return value;
	}
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	public String getSortBy() {
		return sortBy;
	}
	
}
