package com.atanava.locator.cache;

import java.util.Date;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MultiThreadRatingCache<K, V> extends RatingCache<K, V> {

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

	private class Cleaner implements Runnable {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				if (keysByAddingOrder.size() >= batchSize) {
					evictCache();
				}
			}
		}
	}
}

