package org.broker.order;

import org.broker.PricePackage;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Price extends Amount {
	
	public Price(double price) throws IllegalArgumentException {
		super(price);
	}
	
	public static Price create(PricePackage price) {
		return new Price(price.getPrice());
	}

}
