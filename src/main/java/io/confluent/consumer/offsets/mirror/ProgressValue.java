package io.confluent.consumer.offsets.mirror;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class ProgressValue {
  private long offset;
  private final AtomicInteger count;

  public ProgressValue() {
    this.offset = 0L;
    this.count = new AtomicInteger(0);
  }

  public void incrementCount() {
    this.count.incrementAndGet();
  }
}