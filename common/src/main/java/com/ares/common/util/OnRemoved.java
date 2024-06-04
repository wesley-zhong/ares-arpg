package com.ares.common.util;

public interface OnRemoved<K, V> {
    void apply(K key, V value);
}
