package io.confluent.consumer.offsets.mirror.service;

import com.google.common.base.Preconditions;
import io.confluent.consumer.offsets.mirror.MirrorBreakerProcessor;
import io.confluent.consumer.offsets.mirror.MirrorMakerHandlerContext;
import io.confluent.consumer.offsets.mirror.entity.MirrorBreaker;
import lombok.Setter;


@Setter
public class MirrorBreakerService {

  private static final MirrorBreakerService INSTANCE = new MirrorBreakerService();
  private final MirrorBreakerProcessor mirrorBreakerProcessor;

  private MirrorBreakerService() {
    this.mirrorBreakerProcessor = MirrorMakerHandlerContext.getInstance().getMirrorBreakerProcessor();
  }

  public static MirrorBreakerService getInstance() {
    return INSTANCE;
  }

  public MirrorBreaker getCurrentMode() {
    return new MirrorBreaker(this.mirrorBreakerProcessor.getMode());
  }

  public void update(MirrorBreaker mirrorBreaker) {
    Preconditions.checkArgument(mirrorBreaker.getMirrorBreakerMode() != null, "Mode can not be null");
    this.mirrorBreakerProcessor.switchModeTo(mirrorBreaker.getMirrorBreakerMode());
  }
}
