package io.confluent.consumer.offsets.mirror;

import io.confluent.consumer.offsets.concurrent.IdleStateCondition;
import io.confluent.consumer.offsets.mirror.infrastructure.EventObserver;
import io.confluent.consumer.offsets.mirror.infrastructure.Subject;
import kafka.consumer.BaseConsumerRecord;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@Setter
public class MirrorBreakerProcessor implements EventObserver<BaseConsumerRecord> {

  private static final Logger LOG = LoggerFactory.getLogger(MirrorBreakerProcessor.class);
  private IdleStateCondition idleStateCondition;
  private MirrorMakerStateStore mirrorStateStore;
  private final AtomicReference<MirrorBreakerMode> mirrorBreakerMode;

  public MirrorBreakerProcessor() {
    this.mirrorBreakerMode = new AtomicReference<>();
  }

  public void schedule() {
    LOG.info("Mirror Breaker scheduled");
    this.idleStateCondition.async(new ExitOnComplete());
    this.mirrorStateStore.switchModeTo(MirrorMakerState.WAITING);
  }

  public void switchModeTo(MirrorBreakerMode mode) {
    this.mirrorBreakerMode.getAndSet(mode);
    LOG.info("Mirror Breaker mode was switched to {}", mode);
  }

  public MirrorBreakerMode getMode() {
    return this.mirrorBreakerMode.get();
  }

  @Override
  public void onSubjectEvent(Subject<BaseConsumerRecord> context) {
    this.idleStateCondition.postpone();
  }

  class ExitOnComplete implements Runnable {
    @Override
    public void run() {
      LOG.warn("Idle state event occurred");
      if (MirrorBreakerMode.DAEMON.equals(MirrorBreakerProcessor.this.mirrorBreakerMode.get())) {
        LOG.info("Rescheduling...");
        schedule();
      } else {
        LOG.info("Mirroring was completed!");
        System.exit(0);
      }
    }
  }
}
