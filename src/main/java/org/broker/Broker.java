package org.broker;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Singleton;

import org.broker.account.Account;
import org.broker.order.Order;
import org.broker.order.OrderStatus;
import org.broker.order.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.Micronaut;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Singleton
@Controller("/broker")
public class Broker {

	private static final Logger logger = LoggerFactory.getLogger(Broker.class);

	private Service<Account> accounts;

	private OrderService orders;

	private final ReadWriteLock ordersLock;

	public Broker() {
		ordersLock = new ReentrantReadWriteLock();
		accounts = new Service<>();
		orders = new OrderService();
	}

	// Account

	@Get("account/create/{name}/{usdBalance}")
	public HttpResponse<Long> createAccount(String name, double usdBalance) {
		Account account = new Account(name, usdBalance);
		if (this.accounts.create(account.getId(), account)) {
			logger.info("Created " + account.toString());
			return HttpResponse.ok(account.getId());
		}
		return HttpResponse.badRequest(-1L);
	}

	@Get("/account/get/{accountId}")
	public Account getAccountDetails(long accountId) {
		Account account = this.accounts.get(accountId);
		if (account != null) {
			logger.info(account.toString());
		}
		return account;
	}

	// Order

	@Get("/order/create/{accountId}/{priceLimit}/{amount}")
	public HttpResponse<Long> createLimitOrder(long accountId, double priceLimit, double amount) {

		if (!this.accounts.exist(accountId)) {
			logger.warn("Invalid account id, order ignored: " + accountId);
			return HttpResponse.badRequest(-1L);
		}

		Order order = new Order(accountId, priceLimit, amount);
		this.ordersLock.writeLock().lock();
		try {
			this.orders.create(order.getId(), order);
		} finally {
			this.ordersLock.writeLock().unlock();
		}
		logger.info("Created " + order.toString());
		return HttpResponse.ok(order.getId());
	}

	@Get("/order/get/{orderId}")
	public Order getOrderDetails(long orderId) {
		this.ordersLock.readLock().lock();
		try {
			return this.orders.get(orderId);
		} finally {
			this.ordersLock.readLock().unlock();
		}
	}
	
	@Get("/order/all")
	public List<Order> getOrders() {
		this.ordersLock.readLock().lock();
		try {
			return this.orders.values();
		} finally {
			this.ordersLock.readLock().unlock();
		}
	}
	
	public boolean newPrice(PricePackage pricePackage) {
		final Price curPrice = Price.create(pricePackage);		
		return Single.just(curPrice).subscribeOn(Schedulers.computation()).map(x -> fillOrders(x)).blockingGet();
	}

	private boolean fillOrders(Price curPrice) {
		// get list of orders that can be executed
		List<Order> orders = null;
		this.ordersLock.writeLock().lock();
		try {
			orders = this.orders.pollOrdersLargerOrEqualThan(curPrice);
		} finally {
			this.ordersLock.writeLock().unlock();
		}


		orders.forEach(x -> {
			Account account = this.accounts.get(x.getAccountId());
			if (account == null) {
				x.setStatus(OrderStatus.CANCELLED);
				logger.warn("Account id invalid: " + x.getAccountId());
			} else {
				if (account.newTransaction(x.getAmount(), curPrice)) {
					x.setFilledAt(curPrice); // mark the status as filled
					logger.info("Order filled " + x.toString());
				} else {
					x.setStatus(OrderStatus.CANCELLED);
					StringBuilder sb = new StringBuilder();
					sb.append("Order cancelled: ").append(x.toString());
					sb.append(" account id: ").append(x.getAccountId());
					sb.append(", account balance: ").append(account.getUsdBalance().getAmount());
					sb.append(", current price: ").append(curPrice.getAmount());
					logger.info(sb.toString());

				}
			}
		});
		return true;
	}
	
	
	public static void main(String[] args) {
		Micronaut.run(Broker.class, args);
	}

}
