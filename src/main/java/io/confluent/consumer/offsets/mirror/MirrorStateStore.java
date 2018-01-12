package io.confluent.consumer.offsets.mirror;

import com.google.common.collect.ImmutableSortedMap;
import kafka.consumer.BaseConsumerRecord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class MirrorStateStore {

  private static volatile MirrorStateStore instance = new MirrorStateStore();
  private final AtomicReference<MirrorBreakerMode> mode;
  private final AtomicReference<HandlerState> state;
  private final ConcurrentHashMap<ProgressKey, ProgressValue> progress;

  public static MirrorStateStore getInstance() {
    return instance;
  }

  private MirrorStateStore() {
    this.mode = new AtomicReference<>(MirrorBreakerMode.DAEMON);
    this.state = new AtomicReference<>(HandlerState.WAITING);
    this.progress = new ConcurrentHashMap<>();
  }

  public void switchModeTo(HandlerState mode) {
    this.state.getAndSet(mode);
  }

  public MirrorBreakerMode getMode() {
    return this.mode.get();
  }

  public void put(BaseConsumerRecord record) {
    this.progress.compute(new ProgressKey(record.topic(), record.partition()), new OnMessage(record.offset()));
  }

  public void switchModeTo(MirrorBreakerMode mode) {
    this.mode.getAndSet(mode);
  }

  public HandlerState getState() {
    return this.state.get();
  }

  public Map getProgress() {
    return ImmutableSortedMap.copyOf(this.progress, (thisKey, thatKey) -> {
      if (thisKey.getTopic() != thatKey.getTopic()) {
        if (thisKey.getTopic().compareTo(thatKey.getTopic()) < 0) {
          return -1;
        } else if (thisKey.getTopic().compareTo(thatKey.getTopic()) > 0) {
          return 1;
        }
      }
      return Integer.compare(thisKey.getPartition(), thatKey.getPartition());
    });
  }

  class OnMessage implements BiFunction<ProgressKey, ProgressValue, ProgressValue> {

    private final long offset;

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
