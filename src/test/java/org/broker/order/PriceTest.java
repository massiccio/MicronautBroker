package org.broker.order;

import static org.junit.jupiter.api.Assertions.*;

import org.broker.PricePackage;
import org.broker.order.Price;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriceTest {
	
	private Price test;

	@BeforeEach
	void setUp() throws Exception {
		this.test = new Price(3d);
	}

	@AfterEach
	void tearDown() throws Exception {
		this.test = null;
	}

	@Test
	void testPrice() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Price(-1d);
		});
	}

	@Test
	void testCreate() {
		PricePackage price = new PricePackage();
		price.setPrice(5d);
		this.test = Price.create(price);
		assertEquals(price.getPrice(), this.test.getAmount());
	}

}
