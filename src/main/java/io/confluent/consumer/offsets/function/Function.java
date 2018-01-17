package io.confluent.consumer.offsets.function;

@FunctionalInterface
public interface Function<I, O> {
  O apply(I input);
}
