package io.confluent.consumer.offsets.processor;

public interface ProcessorBuilder<K, V> {
  Processor<K, V> build();
}
