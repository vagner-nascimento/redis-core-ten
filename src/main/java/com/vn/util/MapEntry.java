package com.vn.util;

import java.util.Map;

/**
 * A class that implemnts Map.Entry<K, V>
 *
 * @param <K> key
 * @param <V> value
 */
public final class MapEntry<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;

    public MapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K setKey(K key) {
        this.key = key;
        return this.key;
    }

    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public V setValue(V value) {
        this.value = value;
        return this.value;
    }
}
