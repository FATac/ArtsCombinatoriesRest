package org.fundaciotapies.ac.rest.client;

import java.util.List;

class ExtraOptions {
	private String img;
	private String position;
	
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
}

class Profile {
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

public class TranscoEntity {
	private String id;
	private String priority;
	private String src_path;
	private List<Profile> profiles;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getSrc_path() {
		return src_path;
	}
	public void setSrc_path(String src_path) {
		this.src_path = src_path;
	}
	public List<Profile> getProfiles() {
		return profiles;
	}
	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}
}
