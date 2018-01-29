package io.confluent.consumer.offsets.mirror;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class ProgressValue {
  private long offset;
  private long count;

  public ProgressValue() {
    this.offset = 0L;
    this.count = 0L;
  }

  public void incrementCount() {
    this.count++;
  }
}