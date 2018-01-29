package io.confluent.consumer.offsets.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.confluent.consumer.offsets.mirror.MirrorMakerState;
import io.confluent.consumer.offsets.mirror.entity.MirrorMakerStats;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorMakerStatsDto {
  private Map<String, Long> stats;
  @JsonProperty("state")
  private MirrorMakerState mirrorMakerState;

  public MirrorMakerStatsDto(MirrorMakerStats mirrorMaker) {
    this.stats = mirrorMaker.getStats();
    this.mirrorMakerState = mirrorMaker.getMirrorMakerState();
  }
}