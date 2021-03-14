package org.broker.order;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Amount implements Comparable<Amount> {

	private double amount;
	
	
	public Amount(double value) {
		if (value < 0d) {
			throw new IllegalArgumentException("Negative value: " + value);
		}
		this.amount = value;
	}

	public double getAmount() {
		return amount;
	}

	public Amount add(Amount other) {
		this.amount += other.amount;
		return this;
	}

	public Amount subtract(Amount other) {
		if (compareTo(other) >= 0) {
			this.amount -= other.amount;
			return this;
		} else {
			throw new IllegalArgumentException("Negative value detected.");
		}
	}

	public static Amount multiply(Amount a, Amount b) {
		return new Amount(a.amount * b.amount);
	}

	@Override
	public int compareTo(Amount o) {
		return Double.compare(this.amount, o.amount);
	}

	@Override
	public String toString() {
		return "Amount [amount=" + amount + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Amount other = (Amount) obj;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
			return false;
		return true;
	}
	
	

}
