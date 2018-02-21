package io.confluent.consumer.offsets.mirror.entity;

import io.confluent.consumer.offsets.mirror.MirrorMakerState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorMaker {
  private MirrorMakerState mirrorMakerState;
}