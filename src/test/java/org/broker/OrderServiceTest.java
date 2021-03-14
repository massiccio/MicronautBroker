package org.broker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicLong;

import org.broker.OrderService;
import org.broker.order.Order;
import org.broker.order.Price;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

	private static AtomicLong keyGenerator = new AtomicLong();

	private OrderService test;

	@BeforeEach
	void setUp() throws Exception {
		this.test = new OrderService();
	}

	@AfterEach
	void tearDown() throws Exception {
		this.test = null;
	}

	@Test
	void testCreateLongOrder() {
		assertEquals(0, this.test.values().size());
		long key = keyGenerator.incrementAndGet();
		this.test.create(key, new Order(key, 1d, 1d));
		assertEquals(1, this.test.values().size());
	}

	@Test
	void pollOrdersLargerOrEqualThan1() {
		var orders = this.test.pollOrdersLargerOrEqualThan(new Price(0d));
		assertEquals(0, orders.size());

		long key = keyGenerator.incrementAndGet();
		this.test.create(key, new Order(key, 1d, 1d));
		orders = this.test.pollOrdersLargerOrEqualThan(new Price(0.5d));
		assertEquals(1, orders.size());
	}

	
	@Test
	void pollOrdersLargerOrEqualThan2() {
		for (int i = 1; i <= 20; i++) {
			long key = keyGenerator.incrementAndGet();
			this.test.create(key, new Order(key, i, 1d));
		}
		var orders = this.test.values();
		assertEquals(20, orders.size());
		for (int i = 20; i > 0; i--) {
			orders = this.test.pollOrdersLargerOrEqualThan(new Price(i));
			assertEquals(1, orders.size());
		}
		
	}
}
