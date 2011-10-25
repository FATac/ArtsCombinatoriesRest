package org.fundaciotapies.ac.logic.categories.support;

import java.util.List;

public class DataCategory {

	private String name;
	private List<DataCategoryMatch> matches;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setMatches(List<DataCategoryMatch> matches) {
		this.matches = matches;
	}
	public List<DataCategoryMatch> getMatches() {
		return matches;
	}
}
