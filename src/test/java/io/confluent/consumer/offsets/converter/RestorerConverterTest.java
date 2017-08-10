package io.confluent.consumer.offsets.converter;

import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class RestorerConverterTest {

  private RestorerConverter restorerConverter;

  @Before
  public void setUp() throws Exception {
    this.restorerConverter = new RestorerConverter();
  }

  @Test(expected = Exception.class)
  public void applyInvalid() throws Exception {
    this.restorerConverter.apply(new ConsumerRecord<>("topic", 0, 0L, "", ""));
  }

  @Test
  public void applyValid() throws Exception {
    Map.Entry<GroupTopicPartition, Long> entry = this.restorerConverter.apply(
        new ConsumerRecord<>("topic", 0, 0L, "group/topic/1", "1"));

    GroupTopicPartition groupTopicPartition = entry.getKey();
    Long offset = entry.getValue();

    assertEquals("group", groupTopicPartition.group());
    assertEquals("topic", groupTopicPartition.topicPartition().topic());
    assertEquals(1, groupTopicPartition.topicPartition().partition());
    assertEquals(1L, offset.longValue());
  }
}