package com.atanava.locator.service.cache;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class MultiThreadRatingCache<K, V> extends RatingCache<K, V> {

	@Setter
	private long sleep;

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
			while (!Thread.currentThread().isInterrupted()) {
				if (innerMap.size() >= batchSize) {
					try {
						log.debug("Evict Cache from daemon----------------------------------");
						evictCache();
						Thread.sleep(sleep);// это решение мне не нравится
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

