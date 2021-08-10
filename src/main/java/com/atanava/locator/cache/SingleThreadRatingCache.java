package com.atanava.locator.cache;

import java.util.Date;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SingleThreadRatingCache<K, V> extends RatingCache<K, V> {

	public SingleThreadRatingCache(Map<K, CompositeValue> innerMap,
								   Deque<CompositeKey> keyByAddingOrder,
								   AtomicReference<Date> lastEvicted) {
		super(innerMap, keyByAddingOrder, lastEvicted);
	}

	@Override
	protected void evictIfNeeded() {
		if (keysByAddingOrder.size() >= batchSize) {
			evictCache();
		}
	}
}
