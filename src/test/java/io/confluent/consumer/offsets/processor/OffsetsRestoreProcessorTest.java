package io.confluent.consumer.offsets.processor;

import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class OffsetsRestoreProcessorTest extends KafkaTestBase {

  private static final String TOPIC = "testTopic";
  private static boolean isDataInserted;

  private KafkaConsumer<String, String> kafkaConsumer;
  private KafkaProducer<String, String> kafkaProducer;
  private OffsetsRestoreProcessor offsetsRestoreProcessor;

  @Before
  public void topicDataFixture() throws Exception {
    if (isDataInserted) {
      return;
    }
    this.kafkaProducer.send(new ProducerRecord<String, String>(TOPIC, "testValue1")).get();
    this.kafkaProducer.send(new ProducerRecord<String, String>(TOPIC, "testValue2")).get();
    this.kafkaProducer.send(new ProducerRecord<String, String>(TOPIC, "testValue3")).get();
    isDataInserted = true;
  }

  @Before
  public void setUp() throws Exception {
    this.offsetsRestoreProcessor = new OffsetsRestoreProcessor.Builder().withProperties(consumerProps).build();
    this.kafkaProducer = new KafkaProducer<>(producerProps);
    this.kafkaConsumer = new KafkaConsumer<>(consumerProps);
    this.kafkaConsumer.subscribe(Collections.singleton(TOPIC));
  }

  @After
  public void tearDown() throws Exception {
    this.kafkaConsumer.close();
    this.kafkaProducer.close();
    this.offsetsRestoreProcessor.close();
  }

  @Test
  public void testSettingOffset() throws Exception {
    final long offsetToSet = 1L;
    this.offsetsRestoreProcessor.process(new GroupTopicPartition(GROUP, TOPIC, 0), offsetToSet);
    ConsumerRecords<String, String> records = this.kafkaConsumer.poll(POLL_TIMEOUT);
    assertEquals(2, records.count());
    assertNull(findRecord(records, "testValue1"));
    assertNotNull(findRecord(records, "testValue2"));
    assertNotNull(findRecord(records, "testValue3"));
  }

  @Test
  public void testSettingOffsetOutOfRange() throws Exception {
    final long offsetToSet = 5L;
    this.offsetsRestoreProcessor.process(new GroupTopicPartition(GROUP, TOPIC, 0), offsetToSet);
    ConsumerRecords<String, String> records = this.kafkaConsumer.poll(POLL_TIMEOUT);
    assertEquals(0, records.count());
  }

  @Test
  public void testNonExistentPartition() throws Exception {
    final long offsetToSet = 1L;
    this.offsetsRestoreProcessor.process(new GroupTopicPartition(GROUP, TOPIC, 999), offsetToSet);
  }
}