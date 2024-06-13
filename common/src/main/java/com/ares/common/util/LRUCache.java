package com.ares.common.util;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LRUCache<K, V> {
    private final ConcurrentLinkedHashMap<K, V> cache;
    private EvictionListener<K, V> removeListener = new EvictionListener<K, V>() {
        @Override
        public void onEviction(K k, V v) {
            log.info("key = {} lru removed", k);
        }
    };


    public LRUCache(int maxSize) {
        this.cache = new ConcurrentLinkedHashMap.Builder<K, V>()
                .maximumWeightedCapacity(maxSize)
                .initialCapacity(maxSize)
                .listener(removeListener)
                .weigher(Weighers.singleton()).build();
    }

    public V get(K key) {
        return cache.get(key);
    }

    public V put(K key, V value) {
        return cache.put(key, value);
    }

    public V remove(K key) {
        return cache.remove(key);
    }

    public int size(){
        return cache.size();
    }
}