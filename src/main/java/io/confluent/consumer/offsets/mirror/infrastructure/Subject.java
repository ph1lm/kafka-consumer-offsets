package io.confluent.consumer.offsets.mirror.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Subject<T> {
  private final T entry;
}
