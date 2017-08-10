package io.confluent.consumer.offsets.processor;

import kafka.common.OffsetAndMetadata$;
import kafka.common.OffsetMetadata;
import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OffsetsSinkProcessorTest extends KafkaTestBase {

  private static final String TOPIC = "testTopic";

  private OffsetsSinkProcessor offsetsSinkProcessor;
  private KafkaConsumer<String, String> kafkaConsumer;

  @Before
  public void setUp() throws Exception {
    this.offsetsSinkProcessor = new OffsetsSinkProcessor.Builder()
        .withProperties(producerProps)
        .withTopic(TOPIC)
        .build();

    this.kafkaConsumer = new KafkaConsumer<>(consumerProps);
    this.kafkaConsumer.subscribe(Collections.singletonList(TOPIC));
    this.kafkaConsumer.poll(POLL_TIMEOUT);
    this.kafkaConsumer.seekToBeginning(Collections.singletonList(new TopicPartition(TOPIC, 0)));
  }

  @Test
  public void testProcess() throws Exception {
    final int partition = 0;
    final int offset = 10;
    final String group = "group";
    final String topic = "topic";

    this.offsetsSinkProcessor.process(new GroupTopicPartition(group, topic, partition),
        OffsetAndMetadata$.MODULE$.apply(offset, OffsetMetadata.NoMetadata()));

    this.offsetsSinkProcessor.flush();

    ConsumerRecords<String, String> records = this.kafkaConsumer.poll(POLL_TIMEOUT);

    assertEquals(1, records.count());
    assertNotNull(findRecord(records, Integer.toString(offset)));
  }
}