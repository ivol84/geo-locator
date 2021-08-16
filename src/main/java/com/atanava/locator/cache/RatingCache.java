package com.atanava.locator.cache;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
public abstract class RatingCache<K, V> {
	@NonNull
	protected Map<K, CompositeValue> innerMap;
	@NonNull
	protected Deque<CompositeKey> keysByAddingOrder;
	@NonNull
	protected AtomicReference<Date> lastEvicted;
	@Setter
	protected Runtime runtime;
	@Getter	@Setter
	protected volatile long entryLifeTime;
	@Getter	@Setter
	protected volatile int minRating;
	@Getter	@Setter
	protected volatile int batchSize;
	@Getter
	protected volatile boolean useGC;
	@Getter @Setter
	private volatile boolean isMemorySaving;

	public RatingCache() {
	}

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
		log.debug("Put to cache key: {} value: {}", key, value);

		evictIfNeeded();
	}

	public V get(K key) {
		evictIfNeeded();

		V value = null;
		CompositeValue compositeValue = innerMap.get(key);
		if (compositeValue != null) {
			compositeValue.rating.incrementAndGet();
			value = compositeValue.value;
		}
		log.debug("Get from cache key: {} value: {}", key, value);

		return value;
	}

	protected abstract void evictIfNeeded();

	protected void evictCache() {
		log.debug("Trying to evict cache");
		Date now = new Date();
		if ((now.getTime() - lastEvicted.get().getTime()) >= entryLifeTime) {
			Iterator<CompositeKey> iterator = keysByAddingOrder.iterator();
			while (iterator.hasNext()) {
				CompositeKey next = iterator.next();
				if ((now.getTime() - next.inserted.getTime()) >= entryLifeTime) {
					iterator.remove();
					log.debug("REMOVE CompositeKey: {} from keysByAddingOrder", next.key);

					K key = next.key;
					CompositeValue compositeValue = innerMap.get(key);
					if (compositeValue != null
							&& (now.getTime() - compositeValue.inserted.getTime()) >= entryLifeTime
							&& compositeValue.rating.decrementAndGet() < minRating) {
						CompositeValue removed = innerMap.remove(key);
						log.debug("REMOVE key: {} value: {} from inner map", key, removed);
					} else if (compositeValue == null) {
						CompositeValue removed = innerMap.remove(key);
						log.debug("REMOVE key: {} value: {} from inner map", key, removed);
					} else {
						keysByAddingOrder.addLast(new CompositeKey(key, now));
						log.debug("MOVED to end of deque key: {} ", key);
					}
				} else break;
			}
			if (useGC) {
				runtime.gc();
			}
			lastEvicted.set(now);
		}
	}

	public void setUseGC(boolean useGC) {
		this.useGC = useGC;
		if (useGC)
			this.runtime = Runtime.getRuntime();
	}

	@AllArgsConstructor
	protected class CompositeValue {
		private V value;
		private Date inserted;
		private final AtomicInteger rating;

		@Override
		public String toString() {
			return value.toString();
		}
	}

	@AllArgsConstructor
	protected class CompositeKey {
		private final K key;
		private final Date inserted;
	}
}

