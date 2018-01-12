package io.confluent.consumer.offsets.mirror;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@AllArgsConstructor
@ToString
@Getter
public class ProgressKey implements Comparable<ProgressKey> {
  private String topic;
  private int partition;


  @Override
  public int compareTo(ProgressKey that) {
    if (this.topic.compareTo(that.topic) < 0) {
      return -1;
    } else if (this.topic.compareTo(that.topic) > 0) {
      return 1;
    }

    if (this.partition < that.partition) {
      return -1;
    } else if (this.partition > that.partition) {
      return 1;
    }
    return 0;
  }
}
