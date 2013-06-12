package org.fundaciotapies.ac.model.support;

import java.util.Comparator;

public class OrderedNameComparator implements Comparator<OrderedName> {

	@Override
	public int compare(OrderedName firstName, OrderedName secondName) {
		if (firstName != null && firstName.getOrder() != null
				&& secondName != null && secondName.getOrder() != null){
			return firstName.getOrder().compareTo(secondName.getOrder());
		}
		else{
			return 0;
		}
	}
}