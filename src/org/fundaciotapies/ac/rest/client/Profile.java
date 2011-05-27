package org.fundaciotapies.ac.rest.client;

public class Profile {
	private String type;
	private String dst_path;
	private ExtraOptions extra_options;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDst_path() {
		return dst_path;
	}
	public void setDst_path(String dst_path) {
		this.dst_path = dst_path;
	}
	public ExtraOptions getExtra_options() {
		return extra_options;
	}
	public void setExtra_options(ExtraOptions extra_options) {
		this.extra_options = extra_options;
	}
}
