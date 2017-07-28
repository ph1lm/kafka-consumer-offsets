package io.confluent.consumer.offsets.processor;

public interface ConsumerOffsetsProcessor<K, V> {
  void process(K key, V value);
  void close();
}
