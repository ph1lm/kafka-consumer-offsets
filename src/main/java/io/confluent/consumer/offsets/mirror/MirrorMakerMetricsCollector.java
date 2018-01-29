package io.confluent.consumer.offsets.mirror;

import com.codahale.metrics.Meter;
import io.confluent.consumer.offsets.mirror.infrastructure.EventObserver;
import io.confluent.consumer.offsets.mirror.infrastructure.Subject;
import kafka.consumer.BaseConsumerRecord;
import lombok.Setter;

@Setter
public class MirrorMakerMetricsCollector implements EventObserver<BaseConsumerRecord> {
  private Meter records;

  @Override
  public void onSubjectEvent(Subject<BaseConsumerRecord> context) {
    this.records.mark();
  }
}
