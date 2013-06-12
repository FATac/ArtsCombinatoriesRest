package org.fundaciotapies.ac.model.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class TestOrderedNameComparator {

	
	/**
	 * Checks a lower integer on "order" comes first
	 * @throws Exception
	 */
	@Theory
	public void testOrdersByOrder(
			@TestedOn(ints = {1, 2, 3, 4, 5, 6})Integer firstOrder,
			@TestedOn(ints = {1, 2, 3, 4, 5, 6}) Integer secondOrder) throws Exception{
		
		assumeTrue(firstOrder < secondOrder);
		
		OrderedName first = new OrderedName("name", firstOrder);
		OrderedName second = new OrderedName("name shouldn't matter", secondOrder);
		
		OrderedNameComparator comparator = new OrderedNameComparator();
		
		assertThat(comparator.compare(first, second), is(lessThan(0))); 
	}
	
	/**
	 * Checks a higher integer on "order" comes first
	 * @throws Exception
	 */
	@Theory
	public void testOrdersByOrderReverse(
			@TestedOn(ints = {1, 2, 3, 4, 5, 6})Integer firstOrder,
			@TestedOn(ints = {1, 2, 3, 4, 5, 6}) Integer secondOrder) throws Exception{
		
		assumeTrue(firstOrder > secondOrder);
		
		OrderedName first = new OrderedName("name", firstOrder);
		OrderedName second = new OrderedName("name shouldn't matter", secondOrder);
		
		OrderedNameComparator comparator = new OrderedNameComparator();
		
		assertThat(comparator.compare(first, second), is(greaterThan(0))); 
	}
	
	/**
	 * Checks a the same order maintains order
	 * @throws Exception
	 */
	@Theory
	public void testOrdersByOrderEqual(
			@TestedOn(ints = {1, 2, 3, 4, 5, 6})Integer firstOrder,
			@TestedOn(ints = {1, 2, 3, 4, 5, 6}) Integer secondOrder) throws Exception{
		
		assumeTrue(firstOrder == secondOrder);
		
		OrderedName first = new OrderedName("name", firstOrder);
		OrderedName second = new OrderedName("name shouldn't matter", secondOrder);
		
		OrderedNameComparator comparator = new OrderedNameComparator();
		
		assertThat(comparator.compare(first, second), is(equalTo(0))); 
	}
	
	/**
	 * Check the list is built with the expected order
	 */
	@Test
	public void testListBuildsAsExpected(){
		OrderedName first = new OrderedName("1", 1);
		OrderedName second = new OrderedName("2", 2);
		OrderedName third = new OrderedName("3", 3);
		
		List<OrderedName> list = new ArrayList<OrderedName>();
		list.add(second);
		list.add(third);
		list.add(first);
		
		OrderedNameComparator comparator = new OrderedNameComparator();
		
		Collections.sort(list, comparator);
				
		assertThat(list, contains(first, second, third));
	}
}
