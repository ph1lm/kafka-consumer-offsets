package io.confluent.consumer.offsets.blacklist;

public interface ConsumerOffsetsBlacklist<K, V> {
  boolean shouldIgnore(K key, V value);
}
