package org.broker;

import java.time.Instant;
import java.util.Date;

import javax.inject.Singleton;

import org.broker.PricePackage;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Singleton
@Controller("/")
public class PriceController {

	@Get("btc-price")
	public PricePackage getFixedPrice() {
		PricePackage res = new PricePackage();
		res.setPrice(2d);
		res.setTimestamp(Date.from(Instant.now()));
		return res;
	}
}
