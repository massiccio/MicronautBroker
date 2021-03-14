package org.broker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.broker.order.Order;
import org.broker.order.Price;

public class OrderService extends Service<Order> {

	/** Pending orders, sorted in descending order. */
	private PriorityQueue<Order> orderBook;

	public OrderService() {
		super();
		orderBook = new PriorityQueue<>(Collections.reverseOrder());
	}

	@Override
	public boolean create(long key, Order value) {
		if (super.create(key, value)) {
			this.orderBook.add(value);
			return true;
		}
		return false;
	}

	/**
	 * Get the list of orders whose limit price is >= price
	 */
	public List<Order> pollOrdersLargerOrEqualThan(Price price) {
		if (orderBook.isEmpty()) {
			return Collections.emptyList();
		}

		List<Order> list = new ArrayList<>();
		while (orderBook.size() > 0 && orderBook.peek().getPriceLimit().compareTo(price) >= 0) {
			list.add(orderBook.poll());
		}
		return list;
	}
}
