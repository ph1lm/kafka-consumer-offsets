package io.confluent.consumer.offsets.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.confluent.consumer.offsets.mirror.entity.Metrics;
import io.confluent.consumer.offsets.web.common.CustomSerializers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDto {
  private long count;
  @JsonProperty("mean_rate")
  @JsonSerialize(using = CustomSerializers.DoubleSerializer.class)
  private double meanRate;
  @JsonProperty("one_minute_rate")
  @JsonSerialize(using = CustomSerializers.DoubleSerializer.class)
  private double oneMinuteRate;
  @JsonProperty("five_minute_rate")
  @JsonSerialize(using = CustomSerializers.DoubleSerializer.class)
  private double fiveMinuteRate;
  @JsonProperty("fifteen_minute_rate")
  @JsonSerialize(using = CustomSerializers.DoubleSerializer.class)
  private double fifteenMinuteRate;

  public MetricsDto(Metrics meterStats) {
    this.count = meterStats.getCount();
    this.meanRate = meterStats.getMeanRate();
    this.oneMinuteRate = meterStats.getOneMinuteRate();
    this.fiveMinuteRate = meterStats.getFiveMinuteRate();
    this.fifteenMinuteRate = meterStats.getFifteenMinuteRate();
  }
}
