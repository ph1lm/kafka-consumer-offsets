package io.confluent.consumer.offsets.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.confluent.consumer.offsets.mirror.MirrorBreakerMode;
import io.confluent.consumer.offsets.mirror.entity.MirrorBreaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorBreakerDto {
  @JsonProperty("mode")
  private MirrorBreakerMode mirrorBreakerMode;

  public MirrorBreakerDto(MirrorBreaker mirrorBreaker) {
    this.mirrorBreakerMode = mirrorBreaker.getMirrorBreakerMode();
  }
}
