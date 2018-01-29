package io.confluent.consumer.offsets.mirror.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class TopicStats implements Comparable<TopicStats> {
  private String name;
  private int partition;
  private long offset;
  private long count;

  @Override
  public int compareTo(TopicStats that) {
    if (this.name.compareTo(that.getName()) < 0) {
      return -1;
    } else if (this.name.compareTo(that.getName()) > 0) {
      return 1;
    }

    if (this.partition < that.getPartition()) {
      return -1;
    } else if (this.partition > that.getPartition()) {
      return 1;
    }
    return 0;
  }
}
