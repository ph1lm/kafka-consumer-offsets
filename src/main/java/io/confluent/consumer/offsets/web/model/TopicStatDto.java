package io.confluent.consumer.offsets.web.model;

import io.confluent.consumer.offsets.mirror.entity.TopicStats;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class TopicStatDto {
  private String name;
  private int partition;
  private long offset;
  private long count;
  private Date date;

  public TopicStatDto(TopicStats topicProgress) {
    this.name = topicProgress.getName();
    this.partition = topicProgress.getPartition();
    this.offset = topicProgress.getOffset();
    this.count = topicProgress.getCount();
    this.date = topicProgress.getDate();
  }
}

