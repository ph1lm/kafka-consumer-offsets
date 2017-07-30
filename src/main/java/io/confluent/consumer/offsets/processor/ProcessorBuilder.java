package io.confluent.consumer.offsets.processor;

public interface ProcessorBuilder<T> {
  T build();
}
