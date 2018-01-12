package io.confluent.consumer.offsets.mirror;

import com.google.common.collect.ImmutableMap;
import io.confluent.consumer.offsets.handler.HandlerMode;
import io.confluent.consumer.offsets.handler.HandlerState;
import kafka.consumer.BaseConsumerRecord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class MirrorStateStore {

  private static volatile MirrorStateStore instance;
  private final AtomicReference<HandlerMode> mode;
  private final AtomicReference<HandlerState> state;
  private final ConcurrentHashMap<ProgressKey, ProgressValue> progress;

  public static MirrorStateStore getInstance() {
    if (instance == null) {
      synchronized (MirrorStateStore.class) {
        if (instance == null) {
          instance = new MirrorStateStore();
        }
      }
    }
    return instance;
  }

  private MirrorStateStore() {
    this.mode = new AtomicReference<>(HandlerMode.DAEMON);
    this.state = new AtomicReference<>(HandlerState.WAITING);
    this.progress = new ConcurrentHashMap<>();
  }

  public void switchModeTo(HandlerState mode) {
    this.state.getAndSet(mode);
  }

  public HandlerMode getMode() {
    return this.mode.get();
  }

  public void put(BaseConsumerRecord record) {
    this.progress.compute(new ProgressKey(record.topic(), record.partition()), new OnMessage(record.offset()));
  }

  public void switchModeTo(HandlerMode mode) {
    this.mode.getAndSet(mode);
  }

  public HandlerState getState() {
    return this.state.get();
  }

  public Map getProgress() {
    return ImmutableMap.copyOf(this.progress);
  }

  class OnMessage implements BiFunction<ProgressKey, ProgressValue, ProgressValue> {

    private long offset;

    public OnMessage(long offset) {
      this.offset = offset;
    }

    @Override
    public ProgressValue apply(ProgressKey progressKey, ProgressValue progressValue) {
      return recalculate(Optional
          .ofNullable(progressValue)
          .orElse(new ProgressValue()));
    }

    private ProgressValue recalculate(ProgressValue value) {
      value.incrementCount();
      value.setOffset(this.offset);
      return value;
    }
  }
}
