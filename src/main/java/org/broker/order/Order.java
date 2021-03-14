/**
 * 
 */
package org.broker.order;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Buy order
 */
public class Order implements Comparable<Order> {

	private static AtomicLong idGenerator = new AtomicLong(0L);

	private long id;

	private long accountId;

	private Price priceLimit;

	private Amount amount;

	private Amount filledAt;

	private OrderStatus orderStatus = OrderStatus.PENDING;
	
	public Order() {
		// default constructor used to deserialize objects
	}

	public Order(long accountId, double priceLimit, double amount) {
		this(accountId, new Price(priceLimit), new Amount(amount));
	}

	public Order(long accountId, Price priceLimit, Amount amount) {
		id = idGenerator.incrementAndGet();
		this.accountId = accountId;
		this.priceLimit = priceLimit;
		this.amount = amount;
		this.orderStatus = OrderStatus.PENDING;
		filledAt = null;
	}

	public long getId() {
		return id;
	}

	public long getAccountId() {
		return accountId;
	}

	public Price getPriceLimit() {
		return priceLimit;
	}

	public Amount getAmount() {
		return amount;
	}

	@Override
	public int compareTo(Order o) {
		return this.priceLimit.compareTo(o.priceLimit);
	}

	public synchronized OrderStatus getStatus() {
		return this.orderStatus;
	}

	public synchronized Amount getFilledAt() {
		return filledAt;
	}

	public synchronized void setFilledAt(Amount filledAt) {
		this.filledAt = filledAt;
		setStatus(OrderStatus.FILLED);
	}

	public synchronized void setStatus(OrderStatus orderStatus) {
		if (this.orderStatus.equals(OrderStatus.PENDING)) {
			this.orderStatus = orderStatus;
		}
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
		Order other = (Order) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public synchronized String toString() {
		return "Order [orderId=" + id + ", accountId=" + accountId + ", priceLimit=" + priceLimit + ", amount="
				+ amount + ", filledAt=" + filledAt + ", status=" + orderStatus + "]";
	}

}
