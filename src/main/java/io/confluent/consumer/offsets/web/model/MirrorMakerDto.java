package io.confluent.consumer.offsets.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.confluent.consumer.offsets.mirror.MirrorMakerState;
import io.confluent.consumer.offsets.mirror.entity.MirrorMaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorMakerDto {
  @JsonProperty("state")
  private MirrorMakerState mirrorMakerState;

  public MirrorMakerDto(MirrorMaker mirrorMaker) {
    this.mirrorMakerState = mirrorMaker.getMirrorMakerState();
  }
}