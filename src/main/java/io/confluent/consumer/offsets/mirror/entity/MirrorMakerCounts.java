package io.confluent.consumer.offsets.mirror.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorMakerCounts {
  private String topic;
  private long counts;
}