package io.confluent.consumer.offsets.blacklist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompositeBlacklist<K, V> implements ConsumerOffsetsBlacklist<K, V> {

  private final List<ConsumerOffsetsBlacklist<K, V>> blacklists;

  private CompositeBlacklist(List<ConsumerOffsetsBlacklist<K, V>> blacklists) {
    this.blacklists = blacklists;
  }

  @Override
  public boolean shouldIgnore(K key, V value) {
    for (ConsumerOffsetsBlacklist<K, V> blacklist : this.blacklists) {
      if (blacklist.shouldIgnore(key, value)) {
        return true;
      }
    }
    return false;
  }

  public static class Builder<K, V> {

    private final List<ConsumerOffsetsBlacklist<K, V>> blacklists = new LinkedList<>();

    public Builder<K, V> ignore(ConsumerOffsetsBlacklist<K, V> blacklist) {
      this.blacklists.add(blacklist);
      return this;
    }

    public ConsumerOffsetsBlacklist<K, V> build() {
      return new CompositeBlacklist<>(Collections.unmodifiableList(this.blacklists));
    }
  }
}
