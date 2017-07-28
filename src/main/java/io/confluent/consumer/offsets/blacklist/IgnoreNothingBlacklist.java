package io.confluent.consumer.offsets.blacklist;

public class IgnoreNothingBlacklist<K, V> implements ConsumerOffsetsBlacklist<K, V> {
  @Override
  public boolean shouldIgnore(K key, V value) {
    return false;
  }
}
