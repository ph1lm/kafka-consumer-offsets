package io.confluent.consumer.offsets.processor;

public interface Processor<K, V> {
  void process(K key, V value);
  void close();
}
