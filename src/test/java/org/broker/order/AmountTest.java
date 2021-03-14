package org.broker.order;

import static org.junit.jupiter.api.Assertions.*;

import org.broker.order.Amount;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AmountTest {
	
	private Amount test;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		this.test = new Amount(2d);
	}

	@AfterEach
	void tearDown() throws Exception {
		this.test = null;
	}

	@Test()
	void testAmount() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Amount(-1d);			
		});
	}

	@Test
	void testGetValue() {
		assertEquals(2d, this.test.getAmount());
	}

	@Test
	void testAdd() {
		Amount other = new Amount(4d);
		assertEquals(6d, this.test.add(other).getAmount());
	}

	@Test
	void testSubtract1() {
		Amount other = new Amount(5d);
		assertEquals(3d, other.subtract(this.test).getAmount());
	}
	
	@Test
	void testSubtract2() {
		Amount other = new Amount(2d);
		assertEquals(0d, other.subtract(this.test).getAmount());
	}
	
	@Test
	void testSubtract3() {
		Amount other = new Amount(1d);
		assertThrows(IllegalArgumentException.class, () -> {
			other.subtract(this.test);
		});
	}
	

	@Test
	void testMultiply() {
		Amount other = new Amount(4d);
		assertEquals(8d, Amount.multiply(this.test, other).getAmount());
	}

	@Test
	void testCompareTo() {
		assertTrue(this.test.compareTo(new Amount(5d)) < 0);
		assertTrue(this.test.compareTo(new Amount(1d)) > 0);
		assertTrue(this.test.compareTo(new Amount(2d)) == 0);
	}

}
