package io.confluent.consumer.offsets.converter;

import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class RestorerConverterTest {

  @Test(expected = Exception.class)
  public void applyInvalid() throws Exception {
    RestorerConverter restorerConverter = new RestorerConverter();
    restorerConverter.apply(new ConsumerRecord<>("topic", 0, 0L, "", ""));
  }

  @Test
  public void applyValid() throws Exception {
    RestorerConverter restorerConverter = new RestorerConverter();
    Map.Entry<GroupTopicPartition, Long> entry = restorerConverter.apply(
        new ConsumerRecord<>("topic", 0, 0L, "group/topic/1", "1"));

    GroupTopicPartition groupTopicPartition = entry.getKey();
    Long offset = entry.getValue();

    assertEquals("group", groupTopicPartition.group());
    assertEquals("topic", groupTopicPartition.topicPartition().topic());
    assertEquals(1, groupTopicPartition.topicPartition().partition());
    assertEquals(1L, offset.longValue());
  }
}