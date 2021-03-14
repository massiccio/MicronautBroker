package org.broker;

import static io.micronaut.http.HttpRequest.GET;

import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.core.type.Argument;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.Flowable;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Task used to retrieve price quotes.
 */
@Singleton
public class PriceCheckerTask {
	

	private static final Logger logger = LoggerFactory.getLogger(PriceCheckerTask.class);

	private static final int MAX_ERRORS = 3;

	/**
	 * Pauses price checks for this number of seconds if more than MAX_ERRORS
	 * consecutive errors occur.
	 */
	private static final int CIRCUIT_BREAKER_RESET = 30;

	@Client("http://127.0.0.1:5000")
	@Inject
	private RxHttpClient client;


	@Inject
	private Broker controller;

	private AtomicInteger errors = new AtomicInteger(0);

	private Calendar nextCheckAt = Calendar.getInstance();

	@Scheduled(fixedDelay = "2s")
	void pollPrice() {
		if (Instant.now().isAfter(nextCheckAt.toInstant())) {
			Flowable<PricePackage> res = client.retrieve(GET("btc-price"), Argument.of(PricePackage.class));
			res.subscribe(getPriceSubscriber());
		} else {
			logger.debug("Skipping price check.");
		}
	}

	void checkCompleted(boolean success) {
		if (success) {
			this.errors.set(0);
		} else if (this.errors.incrementAndGet() >= MAX_ERRORS) {
			errors.set(0);
			nextCheckAt.add(Calendar.SECOND, CIRCUIT_BREAKER_RESET);
			logger.warn("Disabling price check. Next check at " + nextCheckAt.getTime());
		}
	}
	
	
	private DisposableSubscriber<PricePackage> getPriceSubscriber() {
		return new DisposableSubscriber<PricePackage>() {

			@Override
			public void onNext(PricePackage t) {
				logger.info(t.toString());
				checkCompleted(true);
				controller.newPrice(t);
			}

			@Override
			public void onError(Throwable t) {
				checkCompleted(false);
				logger.info(t.getClass().toString());
			}

			@Override
			public void onComplete() {
				//
			}
		};
	}

}
