package com.atanava.locator.cache;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class MultiThreadRatingCache<K, V> extends RatingCache<K, V> {

	@Setter
	private long sleepTime;

	public MultiThreadRatingCache(Map<K, CompositeValue> innerMap,
								  Deque<CompositeKey> keyByAddingOrder,
								  AtomicReference<Date> lastEvicted) {
		super(innerMap, keyByAddingOrder, lastEvicted);
	}

	private void initCleaner() {
		Thread cleaner = new Thread(new Cleaner());
		cleaner.setDaemon(true);
		cleaner.start();
	}

	@Override
	protected void evictIfNeeded() {}

	private class Cleaner implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (innerMap.size() >= batchSize) {
					try {
						log.debug("Woke up cache cleaner daemon ----------------------------------");
						evictCache();
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						log.warn("Thread was interrupted = {}", Thread.interrupted());
					}
				}
			}
		}
	}
}

