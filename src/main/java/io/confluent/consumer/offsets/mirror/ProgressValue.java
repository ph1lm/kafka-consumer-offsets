package io.confluent.consumer.offsets.mirror;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class ProgressValue {
  private long offset;
  private long count;
  private Date date;

  public ProgressValue() {
    this.date = new Date();
  }

  public void incrementCount() {
    this.count++;
  }
}