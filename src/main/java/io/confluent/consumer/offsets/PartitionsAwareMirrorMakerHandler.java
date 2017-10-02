package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.concurrent.IdleStateCondition;
import kafka.consumer.BaseConsumerRecord;
import kafka.tools.MirrorMaker;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PartitionsAwareMirrorMakerHandler implements MirrorMaker.MirrorMakerMessageHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PartitionsAwareMirrorMakerHandler.class);

  @Override
  public List<ProducerRecord<byte[], byte[]>> handle(BaseConsumerRecord record) {
    IDLE_STATE_CONDITION.postpone();
    logCount();
    Long timestamp = record.timestamp() == Record.NO_TIMESTAMP ? null : record.timestamp();
    return Collections.singletonList(new ProducerRecord<>(record.topic(), record.partition(), timestamp,
        record.key(), record.value()));
  }

  private void logCount() {
    COUNTER.incrementAndGet();
    if (System.currentTimeMillis() - LAST_TS.get() >= LOGGING_TIME_SAMPLE) {
      LAST_TS.set(System.currentTimeMillis());
      LOG.warn(String.format("Processed record: %s", COUNTER.getAndSet(0)));
    }
  }

  private static final IdleStateCondition IDLE_STATE_CONDITION;

  private static final long LOGGING_TIME_SAMPLE;
  private static final AtomicLong COUNTER = new AtomicLong();
  private static final AtomicLong LAST_TS = new AtomicLong(System.currentTimeMillis());

  static {
    String idleStateTimeoutSecs = System.getProperty("idle-state-timeout-secs", Long.toString(5 * 60)); //5min by default
    IDLE_STATE_CONDITION = new IdleStateCondition(Long.parseLong(idleStateTimeoutSecs));
    IDLE_STATE_CONDITION.async(new Runnable() {
      @Override
      public void run() {
        LOG.warn("Idle state event occurred");
        System.exit(0);
      }
    });

    LOGGING_TIME_SAMPLE = Long.parseLong(System.getProperty("logging-time-sample-ms", Long.toString(60 * 1000))); //1min by default
  }
}
