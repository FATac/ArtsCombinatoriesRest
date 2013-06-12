package org.fundaciotapies.ac.model.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestOrderedName {

	/**
	 * Checks if two OrderedName are equal
	 * @throws Exception
	 */
	@Test
	public void testTwoOrderedNamesAreEqual() throws Exception{
		OrderedName name = new OrderedName("name", 3);
		
		OrderedName otherName = new OrderedName("name", 3);
		
		assertThat(name, is(equalTo(otherName)));
		assertThat(name.hashCode(), is(equalTo(otherName.hashCode())));
	}
	
	/**
	 * Two OrderedName should be different if the name differs
	 * @throws Exception
	 */
	@Test
	public void testTwoOrderedNamesAreDifferentByName() throws Exception{
		OrderedName name = new OrderedName("name", 3);
		
		OrderedName otherName = new OrderedName("differentName", 3);
		
		assertThat(name, is(not(equalTo(otherName))));
		assertThat(name.hashCode(), is(not(equalTo(otherName.hashCode()))));
	}
	
	/**
	 * Two OrderedName should be different if the order differs
	 * @throws Exception
	 */
	@Test
	public void testTwoOrderedNamesAreDifferentByOrder() throws Exception{
		OrderedName name = new OrderedName("name", 3);
		
		OrderedName otherName = new OrderedName("name", 5);
		
		assertThat(name, is(not(equalTo(otherName))));
		assertThat(name.hashCode(), is(not(equalTo(otherName.hashCode()))));
	}
}
