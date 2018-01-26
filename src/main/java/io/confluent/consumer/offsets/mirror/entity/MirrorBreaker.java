package io.confluent.consumer.offsets.mirror.entity;

import io.confluent.consumer.offsets.mirror.MirrorBreakerMode;
import io.confluent.consumer.offsets.web.model.MirrorBreakerDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MirrorBreaker {
  private MirrorBreakerMode mirrorBreakerMode;

  public MirrorBreaker(MirrorBreakerDto mirrorBreakerDto) {
    this.mirrorBreakerMode = mirrorBreakerDto.getMirrorBreakerMode();
  }
}
