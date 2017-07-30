package io.confluent.consumer.offsets.blacklist;

public class IgnoreNothingBlacklist<K, V> implements Blacklist<K, V> {
  @Override
  public boolean shouldIgnore(K key, V value) {
    return false;
  }
}
