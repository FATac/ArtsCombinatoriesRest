package org.fundaciotapies.ac.logic.support;

import java.util.List;

public class LegalBlock {
	private String name;
	private String description;
	private List<LegalBlockData> data;
	private List<LegalBlockRules> rules;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setData(List<LegalBlockData> data) {
		this.data = data;
	}
	public List<LegalBlockData> getData() {
		return data;
	}
	public void setRules(List<LegalBlockRules> rules) {
		this.rules = rules;
	}
	public List<LegalBlockRules> getRules() {
		return rules;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LegalBlock) {
			if (name == null) return false;
			return name.equals(((LegalBlock)obj).getName());
		} else return false;
	}
}
