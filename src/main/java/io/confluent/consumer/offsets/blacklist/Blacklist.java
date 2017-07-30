package io.confluent.consumer.offsets.blacklist;

public interface Blacklist<K, V> {
  boolean shouldIgnore(K key, V value);
}
