package io.confluent.consumer.offsets.mirror.infrastructure;

import com.google.common.collect.Lists;
import kafka.consumer.BaseConsumerRecord;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProcessedRecordsTopic implements EventTopic<BaseConsumerRecord> {

  private final List<EventObserver<BaseConsumerRecord>> observes;

  public ProcessedRecordsTopic() {
    this.observes = Lists.newArrayList();
  }

  @Override
  public void registerObserver(EventObserver<BaseConsumerRecord> observer) {
    this.observes.add(observer);
  }

  @Override
  public void publishEvent(Subject<BaseConsumerRecord> subject) {
    Optional.ofNullable(this.observes).orElse(Collections.emptyList())
        .stream()
        .forEach(observer -> observer.onSubjectEvent(subject));
  }
}

