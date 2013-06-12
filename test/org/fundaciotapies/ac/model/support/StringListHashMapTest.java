package org.fundaciotapies.ac.model.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.fundaciotapies.ac.model.support.StringListHashMap.convertToArrayList;

@RunWith(JUnit4.class)
public class StringListHashMapTest {

	/**
	 * If a new item is put (the key didn't exist beforehand),
	 * adds the result as a new List of String
	 */
	@Test
	public void testPutAnInexistingKey(){
		StringListHashMap<String> map = new StringListHashMap<String>();
		map.put("key", convertToArrayList("value"));
		
		assertThat(map.get("key"), contains("value"));
	}
	
	/**
	 * If an existing key is put, the passed list is added to the existing 
	 * values (does not overwrite)
	 */
	@Test
	public void testPutAnExistingKey(){
		StringListHashMap<String> map = new StringListHashMap<String>();
		map.put("key", convertToArrayList("1"));
		map.put("key", convertToArrayList("2", "3"));
		
		assertThat(map.get("key"), contains("1", "2", "3"));
	}
}
