/**
 * 
 */
package org.broker.account;

import java.util.concurrent.atomic.AtomicLong;

import org.broker.order.Amount;
import org.broker.order.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client account
 */
public class Account {

	private static final Logger logger = LoggerFactory.getLogger(Account.class);

	private static AtomicLong idGenerator = new AtomicLong(0L);

	private long id;

	private String name;

	private Amount usdBalance;

	private Amount btcBalance;

	private long ordersFilled;

	
	public Account() {
		// default constructor used to deserialize object
	}

	public Account(String name, double usdBalance) {
		if (usdBalance < 0d) {
			throw new IllegalArgumentException();
		}
		this.id = idGenerator.incrementAndGet();
		this.name = name;
		this.usdBalance = new Amount(usdBalance);
		this.btcBalance = new Amount(0d);
		this.ordersFilled = 0;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public synchronized Amount getUsdBalance() {
		return usdBalance;
	}

	public synchronized Amount getBtcBalance() {
		return btcBalance;
	}

	public synchronized boolean newTransaction(Amount btc, Price price) {
		Amount amount = Amount.multiply(price, btc);
		if (usdBalance.compareTo(amount) >= 0) {
			this.usdBalance.subtract(amount);
			this.btcBalance.add(btc);
			this.ordersFilled++;
			return true;
		}
		logger.info("Insufficient funds for account " + this.id);
		return false;
	}

	public synchronized long getOrdersFilled() {
		return ordersFilled;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		Account other = (Account) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", name=" + name + ", usdBalance=" + usdBalance + ", btcBalance=" + btcBalance
				+ ", ordersFilled=" + ordersFilled + "]";
	}

}
