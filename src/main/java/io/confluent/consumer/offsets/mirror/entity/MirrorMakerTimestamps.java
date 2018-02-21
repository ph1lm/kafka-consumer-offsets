package io.confluent.consumer.offsets.mirror.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorMakerTimestamps {
  private String topic;
  private Date timestamp;
}