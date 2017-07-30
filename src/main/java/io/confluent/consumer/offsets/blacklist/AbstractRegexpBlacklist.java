package io.confluent.consumer.offsets.blacklist;

import java.util.regex.Pattern;

public abstract class AbstractRegexpBlacklist<K, V> implements Blacklist<K, V> {

  private final Pattern pattern;

  public AbstractRegexpBlacklist(String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  public Pattern getPattern() {
    return this.pattern;
  }
}
