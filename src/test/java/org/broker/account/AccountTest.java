package org.broker.account;

import static org.junit.jupiter.api.Assertions.*;

import org.broker.account.Account;
import org.broker.order.Amount;
import org.broker.order.Price;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTest {

	private static final String TEST_ACCOUNT = "test account";

	private static final double INITIAL_BALANCE = 100D;

	private Account test;

	@BeforeEach
	void setUp() throws Exception {
		this.test = new Account(TEST_ACCOUNT, INITIAL_BALANCE);
	}

	@AfterEach
	void tearDown() throws Exception {
		this.test = null;
	}

	@Test
	void testGetId() {
		assertTrue(this.test.getId() > 0L);
	}

	@Test
	void testGetName() {
		assertEquals(TEST_ACCOUNT, this.test.getName());
	}

	@Test
	void testGetUsdBalance() {
		assertEquals(INITIAL_BALANCE, this.test.getUsdBalance().getAmount());
	}

	@Test
	void testGetBtcBalance() {
		assertEquals(0d, this.test.getBtcBalance().getAmount());
	}

	@Test
	void testNewTransaction() {
		// the order is too large
		assertFalse(this.test.newTransaction(new Amount(20d), new Price(20d)));
		assertEquals(0d, this.test.getBtcBalance().getAmount());
		assertEquals(INITIAL_BALANCE, this.test.getUsdBalance().getAmount());
		assertEquals(0L, this.test.getOrdersFilled());
		
		// valid orders
		for (int i = 1; i <= 100; i++) {
			assertTrue(this.test.newTransaction(new Amount(0.1d), new Price(10d)));
			assertTrue(Math.abs(i / 10d - this.test.getBtcBalance().getAmount()) < 0.001d); // avoid problem due to rounding errors	
			assertEquals(INITIAL_BALANCE - i, this.test.getUsdBalance().getAmount());
			assertEquals(i, this.test.getOrdersFilled());
		}
		
		var usdBalance = this.test.getUsdBalance();
		var btcBalance = this.test.getBtcBalance();
		long ordersFilled = this.test.getOrdersFilled();
		
		assertFalse(this.test.newTransaction(new Amount(0.1d), new Price(10d)));
		assertEquals(usdBalance, this.test.getUsdBalance());
		assertEquals(btcBalance, this.test.getBtcBalance());
		assertEquals(ordersFilled, this.test.getOrdersFilled());
	}

	@Test
	void testGetOrdersFilled() {
		assertEquals(0L, this.test.getOrdersFilled());
	}

	@Test
	void testEqualsObject() {
		Account other = new Account(TEST_ACCOUNT, INITIAL_BALANCE);
		// use account id
		assertNotEquals(this.test, other);
	}

}
