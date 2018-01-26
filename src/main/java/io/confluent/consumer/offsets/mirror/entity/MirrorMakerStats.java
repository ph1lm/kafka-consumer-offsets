package io.confluent.consumer.offsets.mirror.entity;

import io.confluent.consumer.offsets.mirror.MirrorMakerState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorMakerStats {
  private Map<String, Long> stats;
  private MirrorMakerState mirrorMakerState;
}