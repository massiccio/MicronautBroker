package org.broker;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.broker.Broker;
import org.broker.PricePackage;
import org.broker.account.Account;
import org.broker.order.Amount;
import org.broker.order.OrderStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest
@TestMethodOrder(OrderAnnotation.class)
class BrokerTest {

	private static final Logger logger = LoggerFactory.getLogger(BrokerTest.class);

	private static final double BALANCE = 10_000.d;

	private static final double PRICE_1 = 10.3;
	private static final double PRICE_2 = PRICE_1 + 1;
	private static final double AMOUNT = 1.5;

	private static long orderId1 = 0L;
	private static long orderId2 = 0L;

	@Inject
	EmbeddedServer server;

	@Inject
	@Client("/broker")
	HttpClient client;

	@Inject
	private Broker test;


	@Test
	@Order(3)
	void testCreateAccount() {
		createAccount(BALANCE);
	}

	private long createAccount(double usdBalance) {
		String uri = "/account/create/testAccount/" + usdBalance;
		Long accountId = client.toBlocking().retrieve(HttpRequest.GET(uri), Long.class);
		assertTrue(accountId > 0);
		return accountId;
	}

	@Test
	@Order(1)
	void testGetAccountDetails1() {
		assertThrows(HttpClientResponseException.class, () -> {
			getAccount(1L);
		});
	}

	@Test
	@Order(4)
	void testGetAccountDetails2() {
		// create account
		long accountId = createAccount(BALANCE);
		// get account balance
		var account = getAccount(accountId);
		assertEquals(new Amount(BALANCE), account.getUsdBalance());
		assertEquals(0, account.getBtcBalance().getAmount());
	}

	private Account getAccount(long accountId) {
		String uri = "/account/get/" + accountId;
		Account account = client.toBlocking().retrieve(HttpRequest.GET(uri), Account.class);
		assertNotNull(account);
		return account;
	}

	@Test
	@Order(2)
	void testCreateLimitOrder1() {
		// "/order/create/{accountId}/{priceLimit}/{amount}"
		String uri = "/account/create/1/10.0/1.5"; // invalid account
		assertThrows(HttpClientResponseException.class, () -> {
			client.toBlocking().retrieve(HttpRequest.GET(uri), Long.class);
		});
	}

	@Test
	@Order(5)
	void testCreateLimitOrder2() {
		long accountId = createAccount(BALANCE);
		orderId1 = createOrder(accountId, PRICE_1, AMOUNT);
		assertTrue(orderId1 > 0);
	}

	private long createOrder(long accountId, double price, double amount) {
		// "/order/create/{accountId}/{priceLimit}/{amount}"
		String uri = "/order/create/" + accountId + "/" + price + "/" + amount; // valid account
		long orderId = client.toBlocking().retrieve(HttpRequest.GET(uri), Long.class);
		assertTrue(orderId > 0);
		return orderId;
	}

	@Test
	@Order(6)
	void testGetOrderDetails() {
		long accountId = createAccount(BALANCE);
		orderId2 = createOrder(accountId, PRICE_2, AMOUNT);
		org.broker.order.Order order = getOrder(orderId2);
		assertEquals(OrderStatus.PENDING, order.getStatus());
		assertEquals(accountId, order.getAccountId());
		assertEquals(orderId2, order.getId());
		assertEquals(PRICE_2, order.getPriceLimit().getAmount());
	}

	private org.broker.order.Order getOrder(long orderId) {
		String uri = "/order/get/" + orderId; // valid order
		org.broker.order.Order order = client.toBlocking().retrieve(HttpRequest.GET(uri), org.broker.order.Order.class);
		assertNotNull(order);
		return order;
	}

	@Test
	@Order(7)
	void testGetOrders() {
		String uri = "/order/all/";
		@SuppressWarnings("unchecked")
		List<org.broker.order.Order> list = client.toBlocking().retrieve(HttpRequest.GET(uri), List.class);
		assertNotNull(list);
	}

	@Test
	@Order(8)
	void testNewPrice() {
		PricePackage newPrice = new PricePackage();
		newPrice.setPrice(PRICE_1 + 0.1); // order 1 does not get filled
		newPrice.setTimestamp(Date.from(Instant.now()));

		// get info about account 2 before the order gets filled
		Account account2 = getAccount(getOrder(orderId2).getAccountId());
		Amount usdBalance2BeforeOrderExecution = account2.getUsdBalance();
		assertEquals(0d, account2.getBtcBalance().getAmount());

		long accountId3 = createAccount(10d);
		long orderId3 = createOrder(accountId3, PRICE_2, 10000);
		Amount usdBalance3BeforeOrderExecution = getAccount(accountId3).getUsdBalance();
		this.test.newPrice(newPrice); // order 1 pending, fills order 2, cancels order 3

		var order1 = getOrder(orderId1);
		var order2 = getOrder(orderId2);
		var order3 = getOrder(orderId3);
		logger.info("New price: " + order1.toString());
		logger.info("New price: " + order2.toString());
		assertEquals(OrderStatus.PENDING, order1.getStatus());
		assertEquals(OrderStatus.FILLED, order2.getStatus());
		assertEquals(OrderStatus.CANCELLED, order3.getStatus());

		// check that account2 after the order has been filled
		account2 = getAccount(getOrder(orderId2).getAccountId());
		Amount usdBalance2AfterOrderExecution = account2.getUsdBalance();
		// USD balance
		assertEquals(usdBalance2AfterOrderExecution,
				usdBalance2BeforeOrderExecution.subtract(Amount.multiply(order2.getAmount(), order2.getFilledAt())));
		// BTC balance
		assertEquals(order2.getAmount(), account2.getBtcBalance());

		var account3 = getAccount(accountId3);
		Amount usdBalance3AfterOrderExecution = account3.getUsdBalance();
		assertEquals(usdBalance3BeforeOrderExecution, usdBalance3AfterOrderExecution);
		assertEquals(0d, account3.getBtcBalance().getAmount());
	}

}
