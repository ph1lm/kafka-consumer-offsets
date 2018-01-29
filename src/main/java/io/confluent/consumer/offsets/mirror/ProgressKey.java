package io.confluent.consumer.offsets.mirror;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@AllArgsConstructor
@ToString
@Getter
public class ProgressKey {
  private String topic;
  private int partition;
}
