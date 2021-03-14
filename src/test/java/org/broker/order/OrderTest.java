package org.broker.order;

import static org.junit.jupiter.api.Assertions.*;

import org.broker.order.Amount;
import org.broker.order.Order;
import org.broker.order.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderTest {
	private static final long ACCOUNT_ID = 1L;
	private static final double PRICE_LIMIT = 5D;
	private static final double AMOUNT = 2d;
	
	private Order test;

	@BeforeEach
	void setUp() throws Exception {
		this.test = new Order(ACCOUNT_ID, PRICE_LIMIT, AMOUNT);
	}

	@AfterEach
	void tearDown() throws Exception {
		this.test = null;
	}

	@Test
	void testHashCode() {
		int val = this.test.hashCode();
		this.test.setStatus(OrderStatus.FILLED);
		assertEquals(val, this.test.hashCode());
		this.test.setFilledAt(new Amount(10d));
		assertEquals(val, this.test.hashCode());
	}

	@Test
	void testGetId() {
		assertTrue(this.test.getId() > 0L);
	}

	@Test
	void testGetAccountId() {
		assertEquals(ACCOUNT_ID, this.test.getAccountId());
	}

	@Test
	void testGetPriceLimit() {
		assertEquals(PRICE_LIMIT, this.test.getPriceLimit().getAmount());
	}

	@Test
	void testGetAmount() {
		assertEquals(AMOUNT, this.test.getAmount().getAmount());
	}

	@Test
	void testCompareTo() {
		Order other = new Order(ACCOUNT_ID, PRICE_LIMIT - 1d, AMOUNT);
		assertTrue(this.test.compareTo(other) > 0);
	}

	@Test
	void testGetStatus1() {
		assertTrue(this.test.getStatus().equals(OrderStatus.PENDING));
		this.test.setStatus(OrderStatus.CANCELLED);
		assertTrue(this.test.getStatus().equals(OrderStatus.CANCELLED));
		// cannot change status anymore
		this.test.setStatus(OrderStatus.PENDING);
		assertTrue(this.test.getStatus().equals(OrderStatus.CANCELLED));
	}
	
	@Test
	void testGetStatus2() {
		assertTrue(this.test.getStatus().equals(OrderStatus.PENDING));
		this.test.setStatus(OrderStatus.FILLED);
		assertTrue(this.test.getStatus().equals(OrderStatus.FILLED));
		// cannot change status anymore
		this.test.setStatus(OrderStatus.CANCELLED);
		assertTrue(this.test.getStatus().equals(OrderStatus.FILLED));
	}

	@Test
	void testGetFilledAt() {
		assertNull(this.test.getFilledAt());
	}

	@Test
	void testSetFilledAt() {
		this.test.setFilledAt(new Amount(10d));
		assertEquals(10d, this.test.getFilledAt().getAmount());
	}

	

	@Test
	void testEqualsObject() {
		Order other = new Order(ACCOUNT_ID, PRICE_LIMIT, AMOUNT);
		// the order id is used
		assertNotEquals(this.test, other);
	}

}
