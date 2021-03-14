package org.broker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.broker.Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ServiceTest {
	
	private static final AtomicLong key = new AtomicLong();
	
	private Service<Integer> test;

	@BeforeEach
	void setUp() throws Exception {
		this.test = new Service<>();
	}

	@AfterEach
	void tearDown() throws Exception {
		this.test = null;
	}

	@Test
	void testCreate() {
		for (int i = 0; i < 10; i++) {
			this.test.create(key.incrementAndGet(), i);
		}
		assertEquals(10, this.test.values().size());
	}

	@Test
	void testGet() {
		assertNull(this.test.get(key.incrementAndGet()));
		this.test.create(1L, 10);
		assertEquals(10, this.test.get(1L));
	}

	@Test
	void testExist() {
		assertFalse(this.test.exist(key.incrementAndGet()));
		this.test.create(1L, 1);
		assertTrue(this.test.exist(1L));
	}

	@Test
	void testValues() {
		for (int i = 0; i < 3; i++) {
			this.test.create(key.incrementAndGet(), i);
		}
		Set<Integer> values = new HashSet<>(test.values());
		assertEquals(3, values.size());
		for (int i = 0; i < 3; i++) {
			assertTrue(values.contains(i));
		}
	}
	
	
	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void testConcurrency() throws InterruptedException, ExecutionException {
		final int iterations = 100_000;
		final int size = 10;
		ExecutorService es = Executors.newFixedThreadPool(size);
		Collection<Callable<Boolean>> tasks = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			tasks.add(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					for (int i = 0; i < iterations; i++) {
						long k = key.incrementAndGet();
						test.create(k, (int) k);
					}
					return true;
				}
			});
		}
		List<Future<Boolean>> res = es.invokeAll(tasks);
		es.shutdown();
		for (var x: res) {
			x.get();
		}
		assertEquals(size * iterations, this.test.values().size());
	}

}
