package io.confluent.consumer.offsets.mirror;

import com.google.common.collect.ImmutableMap;
import io.confluent.consumer.offsets.mirror.infrastructure.EventObserver;
import io.confluent.consumer.offsets.mirror.infrastructure.Subject;
import kafka.consumer.BaseConsumerRecord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class MirrorMakerStateStore implements EventObserver<BaseConsumerRecord> {

  private final AtomicReference<MirrorMakerState> state;
  private final ConcurrentHashMap<ProgressKey, ProgressValue> topicsStats;

  public MirrorMakerStateStore() {
    this.state = new AtomicReference<>(MirrorMakerState.WAITING);
    this.topicsStats = new ConcurrentHashMap<>();
  }

  public void switchModeTo(MirrorMakerState mode) {
    this.state.getAndSet(mode);
  }

  public MirrorMakerState getState() {
    return this.state.get();
  }

  public Map<ProgressKey, ProgressValue> getTopicsStats() {
    return ImmutableMap.copyOf(this.topicsStats);
  }

  @Override
  public void onSubjectEvent(Subject<BaseConsumerRecord> subject) {
    final BaseConsumerRecord record = subject.getEntry();
    this.topicsStats.compute(new ProgressKey(record.topic(), record.partition()), new OnMessage(record.offset()));
    switchModeTo(MirrorMakerState.RUNNING);
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
