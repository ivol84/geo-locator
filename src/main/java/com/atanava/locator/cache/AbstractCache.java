package com.atanava.locator.cache;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public abstract class AbstractCache<K, V> implements Cache<K, V> {
	protected final Map<K, CompositeValue> innerMap;
	protected final Deque<CompositeKey> keysByAddingOrder;
	protected final long entryLifeTime;
	protected final AtomicReference<Date> lastEvicted;
	private final Runtime runtime;
	@Getter
	@Setter
	protected volatile int minRating = 10;
	@Getter
	@Setter
	protected volatile int batchSize = 1000;
	@Getter
	@Setter
	private volatile boolean useGC = true;

	@Override
	public void put(K key, V value) {
		Date now = new Date();
		CompositeKey compositeKey = new CompositeKey(key, now);

		CompositeValue compositeValue = innerMap.get(key);
		if (compositeValue != null) {
			compositeValue.value = value;
			compositeValue.inserted = now;
		} else {
			compositeValue = new CompositeValue(value, now, new AtomicInteger());
		}

		innerMap.put(key, compositeValue);
		keysByAddingOrder.addLast(compositeKey);

		evictIfNeeded();
	}

	@Override
	public V get(K key) {
		evictIfNeeded();

		CompositeValue compositeValue = innerMap.get(key);
		if (compositeValue != null && (new Date().getTime() - compositeValue.inserted.getTime()) < entryLifeTime) {
			compositeValue.rating.incrementAndGet();
			return compositeValue.value;
		}
		return null;
	}

	protected void evictIfNeeded() {}

	protected void evictCache() {
		Date now = new Date();
		if ((now.getTime() - lastEvicted.get().getTime()) >= entryLifeTime) {
			Iterator<CompositeKey> iterator = keysByAddingOrder.iterator();
			while (iterator.hasNext()) {
				CompositeKey next = iterator.next();
				if ((now.getTime() - next.inserted.getTime()) >= entryLifeTime) {
					iterator.remove();

					K key = next.key;
					CompositeValue compositeValue = innerMap.get(key);
					if ((now.getTime() - compositeValue.inserted.getTime()) >= entryLifeTime
							&& compositeValue.rating.decrementAndGet() < minRating) {
						innerMap.remove(key);
					}
				} else break;
			}
			if (useGC) {
				runtime.gc();
			}
			lastEvicted.set(now);
		}
	}

	@AllArgsConstructor
	protected class CompositeValue {
		private V value;
		private Date inserted;
		private final AtomicInteger rating;
	}

	@AllArgsConstructor
	protected class CompositeKey {
		final K key;
		final Date inserted;
	}
}

