package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.Blacklist;
import io.confluent.consumer.offsets.converter.IdentityConverter;
import io.confluent.consumer.offsets.processor.Processor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConsumerLoopTest {

  public static final String TOPIC = "testTopic";
  public static final int POLL_TIMEOUT = 1;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private Consumer<Object, Object> consumer;

  @Mock
  private Processor<Object, Object> processor;

  @Mock
  private Blacklist<Object, Object> blackList;

  private ConsumerLoop<Object, Object, Object, Object> consumerLoop;

  @Before
  public void setUp() throws Exception {
    this.consumerLoop = new ConsumerLoop<>(this.consumer, this.processor, this.blackList, new IdentityConverter<>(),
        TOPIC,true, POLL_TIMEOUT, true);
  }

  private static ConsumerRecords<Object, Object> emptyDataFixture() {
    return new ConsumerRecords<>(new HashMap<TopicPartition, List<ConsumerRecord<Object, Object>>>());
  }

  public static ConsumerRecords<Object, Object> topicDataFixture() throws Exception {
    final int partition = 0;
    Map<TopicPartition, List<ConsumerRecord<Object, Object>>> map = new HashMap<>();
    map.put(new TopicPartition(TOPIC, partition), Arrays.asList(
        new ConsumerRecord<Object, Object>(TOPIC, partition, 0, "testKey1", "testValue1"),
        new ConsumerRecord<Object, Object>(TOPIC, partition, 1, "testKey2", "testValue2")
    ));
    return new ConsumerRecords<>(map);
  }

  @Test
  public void testPoll() throws Exception {
    doReturn(topicDataFixture()).doReturn(emptyDataFixture()).when(this.consumer).poll(POLL_TIMEOUT);
    this.consumerLoop.run();
    verify(this.blackList).shouldIgnore("testKey1", "testValue1");
    verify(this.blackList).shouldIgnore("testKey2", "testValue2");
    verify(this.processor).process("testKey1", "testValue1");
    verify(this.processor).process("testKey2", "testValue2");
    verify(this.processor).close();
  }

  @Test
  public void testWhenAllIgnored() throws Exception {
    doReturn(topicDataFixture()).doReturn(emptyDataFixture()).when(this.consumer).poll(POLL_TIMEOUT);
    doReturn(true).when(this.blackList).shouldIgnore(any(), any());
    this.consumerLoop.run();
    verify(this.processor, never()).process(any(), any());
    verify(this.processor).close();
  }

  @Test
  public void testStop() throws Exception {
    doThrow(new WakeupException()).when(this.consumer).poll(POLL_TIMEOUT);
    this.consumerLoop.run();
    verify(this.processor, never()).process(any(), any());
    verify(this.processor).close();
  }

  @Test
  public void testErrorInProcessor() throws Exception {
    doReturn(topicDataFixture()).doReturn(emptyDataFixture()).when(this.consumer).poll(POLL_TIMEOUT);
    doThrow(new RuntimeException("test exception")).when(this.processor).process("testKey1", "testValue1");
    doReturn(false).when(this.blackList).shouldIgnore(any(), any());
    this.consumerLoop.run();
    verify(this.processor, times(2)).process(any(), any());
    verify(this.blackList, times(2)).shouldIgnore(any(), any());
    verify(this.processor).close();
  }

  @Test
  public void testErrorInBlackList() throws Exception {
    doReturn(topicDataFixture()).doReturn(emptyDataFixture()).when(this.consumer).poll(POLL_TIMEOUT);
    doThrow(new RuntimeException("test exception")).when(this.blackList).shouldIgnore("testKey1", "testValue1");
    doReturn(false).when(this.blackList).shouldIgnore(any(), any());
    this.consumerLoop.run();
    verify(this.processor, times(2)).process(any(), any());
    verify(this.blackList, times(2)).shouldIgnore(any(), any());
    verify(this.processor).close();
  }
}