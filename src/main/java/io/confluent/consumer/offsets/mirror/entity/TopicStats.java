package io.confluent.consumer.offsets.mirror.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@lombok.Builder
public class TopicStats implements Comparable<TopicStats> {
  private String name;
  private int partition;
  private long offset;
  private long count;
  private Date date;

  @Override
  public int compareTo(TopicStats that) {
    return this.date.compareTo(that.getDate());
  }
}
