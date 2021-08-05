package com.atanava.locator.service.cache;

import lombok.*;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public abstract class RatingCache<K, V> {
	@NonNull
	protected Map<K, CompositeValue> innerMap;
	@NonNull
	protected Deque<CompositeKey> keysByAddingOrder;
	@NonNull
	protected AtomicReference<Date> lastEvicted;
	protected Runtime runtime;
	@Getter	@Setter
	protected volatile long entryLifeTime;
	@Getter	@Setter
	protected volatile int minRating;
	@Getter	@Setter
	protected volatile int batchSize;
	@Getter	@Setter
	protected volatile boolean useGC;

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

		evictIfNeeded();
	}

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

	protected class CompositeValue {
		private V value;
		private Date inserted;
		private final AtomicInteger rating;

		public CompositeValue(V value, Date inserted, AtomicInteger rating) {
			this.value = value;
			this.inserted = inserted;
			this.rating = rating;
		}
	}

	protected class CompositeKey {
		final K key;
		final Date inserted;

		protected CompositeKey(K key, Date inserted) {
			this.key = key;
			this.inserted = inserted;
		}
	}
}

