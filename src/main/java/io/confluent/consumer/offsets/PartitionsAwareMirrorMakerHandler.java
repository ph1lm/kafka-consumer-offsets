package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.mirror.MirrorBreaker;
import io.confluent.consumer.offsets.mirror.MirrorStateStore;
import io.confluent.consumer.offsets.web.EmbeddedWebServer;
import kafka.consumer.BaseConsumerRecord;
import kafka.tools.MirrorMaker;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.Record;

import java.util.Collections;
import java.util.List;

public class PartitionsAwareMirrorMakerHandler implements MirrorMaker.MirrorMakerMessageHandler {

  private final MirrorBreaker mirroringBreaker;
  private final MirrorStateStore mirrorStateStore;

  public PartitionsAwareMirrorMakerHandler() {
    final EmbeddedWebServer embeddedWebServer = new EmbeddedWebServer();
    embeddedWebServer.start();
    this.mirrorStateStore = MirrorStateStore.getInstance();
    this.mirroringBreaker = new MirrorBreaker();
    this.mirroringBreaker.schedule();
    Runtime.getRuntime().addShutdownHook(new Thread(PartitionsAwareMirrorMakerHandler.this.mirrorStateStore::dumpProgress));
  }

  @Override
  public List<ProducerRecord<byte[], byte[]>> handle(BaseConsumerRecord record) {

    this.mirroringBreaker.postpone();
    this.mirrorStateStore.put(record);
    Long timestamp = record.timestamp() == Record.NO_TIMESTAMP ? null : record.timestamp();
    return Collections.singletonList(new ProducerRecord<>(record.topic(), record.partition(), timestamp,
        record.key(), record.value()));
  }
}
