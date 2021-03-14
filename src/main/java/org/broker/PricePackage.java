package org.broker;

import java.util.Date;

import io.micronaut.core.annotation.Introspected;

/**
 * Utility class used to store data retrieved from the quote service.
 */
@Introspected
public class PricePackage {

	private double price;
	private Date timestamp;

	public PricePackage() {
		//
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "PricePackage [price=" + price + ", timestamp=" + timestamp.toString() + "]";
	}

}
