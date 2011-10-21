package org.fundaciotapies.ac.model.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomMap extends HashMap<String, Object>{
	private static final long serialVersionUID = -1812690206134151827L;
	
	public String put(String key, String value) {
		if (key==null) return null;
		key = key.trim();
		Object prev = super.get(key);
		if (prev!=null && key.equals("type")) return null;
		if (prev!=null && value!=null) {
			if (prev instanceof String) {
				super.put(key, new String[]{(String)prev, value});
			} else {
				String[] arr = (String[])prev;
				String[] curr = new String[arr.length+1];
				int i = 0;
				for (String s : arr) curr[i++] = s;
				curr[i] = value;
				
				super.put(key, curr);
			}
		} else {
			super.put(key, value);
		}
		
		return value;
	}
	
	public CustomMap(Map<String, String> map) {
		Set<Map.Entry<String, String>> es = map.entrySet();
		for (Map.Entry<String, String> e : es) {
			this.put(e.getKey(), e.getValue());
		}
	}
	
	public CustomMap() {
		super();
	}
	
}
