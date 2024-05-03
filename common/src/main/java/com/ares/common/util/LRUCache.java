package com.ares.common.util;

import com.google.common.cache.*;

public class LRUCache<K, V> {

    private final LoadingCache<K, V> cache;
    private OnRemoved<K, V> onRemoved;
    //  private V EMPTY =  new ();

    public LRUCache(CacheLoader<K, V> loader, int maxSize, OnRemoved<K, V> onRemovedListener) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .removalListener(new RemovalListener<Object, Object>() {
                    @Override
                    public void onRemoval(RemovalNotification<Object, Object> notification) {
                        onRemoved.apply((K) notification.getKey(), (V) notification.getValue());
                    }
                })
                .build(loader);
    }

    public LRUCache(int maxSize, OnRemoved<K, V> onRemovedListener) {
        this.onRemoved = onRemovedListener;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .removalListener(new RemovalListener<Object, Object>() {
                    @Override
                    public void onRemoval(RemovalNotification<Object, Object> notification) {
                        onRemoved.apply((K) notification.getKey(), (V) notification.getValue());
                    }
                })
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        return null;
                    }
                });
    }

    public LRUCache(int maxSize) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        return null;
                    }
                });
    }

    public synchronized V get(K key) {
        try {
            return cache.get(key);
        } catch (Exception e) {

        }
        return null;
    }

    public synchronized void put(K key, V value) {
        cache.put(key, value);
    }
}