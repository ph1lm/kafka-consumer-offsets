package io.confluent.consumer.offsets.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorMakerCountsDto {
  private String topic;
  private long counts;
}