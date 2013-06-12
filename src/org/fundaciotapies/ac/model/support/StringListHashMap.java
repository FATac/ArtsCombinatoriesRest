package org.fundaciotapies.ac.model.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StringListHashMap<K> extends HashMap<K, ArrayList<String>>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 131234123512L;

	@Override
	public ArrayList<String> put(K key, ArrayList<String> value) {
		ArrayList<String> lastValue = this.get(key);
		if (lastValue != null){
			lastValue.addAll(value);
			return super.put(key, lastValue);
		}
		else{
			return super.put(key, value);
		}
	}
	
	public static 	ArrayList<String> convertToArrayList(String... values) {
		return new ArrayList<String>(Arrays.asList(values));
	}
}
