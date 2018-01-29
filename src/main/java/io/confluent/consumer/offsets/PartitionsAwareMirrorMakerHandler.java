package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.mirror.MirrorMakerHandlerContext;
import io.confluent.consumer.offsets.mirror.infrastructure.EventTopic;
import io.confluent.consumer.offsets.mirror.infrastructure.Subject;
import kafka.consumer.BaseConsumerRecord;
import kafka.tools.MirrorMaker;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.Record;

import java.util.Collections;
import java.util.List;

public class PartitionsAwareMirrorMakerHandler implements MirrorMaker.MirrorMakerMessageHandler {

  private final EventTopic<BaseConsumerRecord> topic;

  public PartitionsAwareMirrorMakerHandler() {
    MirrorMakerHandlerContext context = MirrorMakerHandlerContext.getInstance();
    context.injectProperties();
    context.start();
    this.topic = context.getProcessedRecordsTopic();
  }

  @Override
  public List<ProducerRecord<byte[], byte[]>> handle(BaseConsumerRecord record) {

    this.topic.publishEvent(new Subject<>(record));
    Long timestamp = record.timestamp() == Record.NO_TIMESTAMP ? null : record.timestamp();

    return Collections.singletonList(new ProducerRecord<>(record.topic(), record.partition(), timestamp,
        record.key(), record.value()));
  }
}
