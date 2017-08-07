package io.confluent.consumer.offsets;

import kafka.consumer.BaseConsumerRecord;
import kafka.tools.MirrorMaker;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.Record;

import java.util.Collections;
import java.util.List;

public class PartitionsAwareMirrorMakerHandler implements MirrorMaker.MirrorMakerMessageHandler {

  @Override
  public List<ProducerRecord<byte[], byte[]>> handle(BaseConsumerRecord record) {
    Long timestamp = record.timestamp() == Record.NO_TIMESTAMP ? null : record.timestamp();
    return Collections.singletonList(new ProducerRecord<>(record.topic(), record.partition(), timestamp,
        record.key(), record.value()));
  }
}
