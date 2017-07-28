package io.confluent.consumer.offsets.function;

public interface Function<I, O> {
  O apply(I input);
}
