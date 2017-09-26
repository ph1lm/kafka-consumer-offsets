package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.concurrent.IdleStateCondition;
import kafka.consumer.BaseConsumerRecord;
import kafka.tools.MirrorMaker;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.Record;

import java.util.Collections;
import java.util.List;

public class PartitionsAwareMirrorMakerHandler implements MirrorMaker.MirrorMakerMessageHandler {

  @Override
  public List<ProducerRecord<byte[], byte[]>> handle(BaseConsumerRecord record) {
    IDLE_STATE_CONDITION.postpone();
    Long timestamp = record.timestamp() == Record.NO_TIMESTAMP ? null : record.timestamp();
    return Collections.singletonList(new ProducerRecord<>(record.topic(), record.partition(), timestamp,
        record.key(), record.value()));
  }

  private static final IdleStateCondition IDLE_STATE_CONDITION;

  static {
    String idleStateTimeoutSecs = System.getProperty("idle-state-timeout-secs", Long.toString(5 * 60)); //5min by default
    IDLE_STATE_CONDITION = new IdleStateCondition(Long.parseLong(idleStateTimeoutSecs));
    IDLE_STATE_CONDITION.async(new Runnable() {
      @Override
      public void run() {
        System.out.println("Idle state event occurred");
        System.exit(0);
      }
    });
  }
}
