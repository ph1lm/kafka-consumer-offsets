package io.confluent.consumer.offsets.function;

public class IdentityFunction<T> implements Function<T, T> {
  @Override
  public T apply(T input) {
    return input;
  }
}
