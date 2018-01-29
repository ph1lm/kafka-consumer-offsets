package io.confluent.consumer.offsets.mirror.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Metrics {
  private long count;
  private double meanRate;
  private double oneMinuteRate;
  private double fiveMinuteRate;
  private double fifteenMinuteRate;
}
