package org.fundaciotapies.ac.model.support;

import java.util.List;

public class DataMapping {

	private String name;
	private String type;
	private List<String> path;
	private List<String> value;
	private String category;
	private String multilingual;
	private String sort;
	private String search;
	private String autocomplete;
	private List<String> filter;
	private String sortCategory;
	private List<String> sortFields;
	private Integer order;
	private String description;
	
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
	public void setMultilingual(String multilingual) {
		this.multilingual = multilingual;
	}
	public String getMultilingual() {
		return multilingual;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getSort() {
		return sort;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public String getSearch() {
		return search;
	}
	public void setAutocomplete(String autocomplete) {
		this.autocomplete = autocomplete;
	}
	public String getAutocomplete() {
		return autocomplete;
	}
	public void setFilter(List<String> filter) {
		this.filter = filter;
	}
	public List<String> getFilter() {
		return filter;
	}
	public void setSortCategory(String sortCategory) {
		this.sortCategory = sortCategory;
	}
	public String getSortCategory() {
		return sortCategory;
	}
	public void setSortFields(List<String> sortFields) {
		this.sortFields = sortFields;
	}
	public List<String> getSortFields() {
		return sortFields;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
