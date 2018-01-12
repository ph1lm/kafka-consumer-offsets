package io.confluent.consumer.offsets.mirror;

import io.confluent.consumer.offsets.concurrent.IdleStateCondition;
import io.confluent.consumer.offsets.handler.HandlerMode;
import io.confluent.consumer.offsets.handler.HandlerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MirrorBreaker {

  private static final Logger LOG = LoggerFactory.getLogger(MirrorBreaker.class);
  private final IdleStateCondition idleStateCondition;
  private final MirrorStateStore mirrorStateStore;

  public MirrorBreaker() {
    String idleStateTimeoutSecs = System.getProperty("idle-state-timeout-secs", Long.toString(5 * 60)); //5min by default
    this.idleStateCondition = new IdleStateCondition(Long.parseLong(idleStateTimeoutSecs));
    this.mirrorStateStore = MirrorStateStore.getInstance();
  }

  public void schedule() {
    this.idleStateCondition.async(new ExitOnComplete());
    this.mirrorStateStore.switchModeTo(HandlerState.WAITING);
  }

  public void postpone() {
    this.idleStateCondition.postpone();
    this.mirrorStateStore.switchModeTo(HandlerState.RUNNING);
  }

  private void exit() {
    if (HandlerMode.DAEMON.equals(this.mirrorStateStore.getMode())) {
      LOG.warn("Rescheduling...");
      schedule();
    } else {
      LOG.warn("Mirroring was completed!");
      System.exit(0);
    }
  }

  class ExitOnComplete implements Runnable {
    @Override
    public void run() {
      LOG.warn("Idle state event occurred");
      MirrorBreaker.this.exit();
    }
  }
}
