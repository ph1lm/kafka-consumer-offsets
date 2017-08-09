package io.confluent.consumer.offsets.converter;

import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import java.util.AbstractMap;
import java.util.Map;

public class RestorerConverter
    implements Converter<String, String, GroupTopicPartition, Long> {

  private static final String KEY_SPLIT = "/";

  @Override
  public Map.Entry<GroupTopicPartition, Long> apply(ConsumerRecord<String, String> consumerRecord) {
    String key = consumerRecord.key();
    String[] split = key.split(KEY_SPLIT);
    String group = split[0];
    String topic = split[1];
    Integer partition = Integer.parseInt(split[2]);

    String value = consumerRecord.value();
    long offset = Long.parseLong(value);

    TopicPartition topicPartition = new TopicPartition(topic, partition);
    GroupTopicPartition groupTopicPartition = new GroupTopicPartition(group, topicPartition);
    return new AbstractMap.SimpleImmutableEntry<>(groupTopicPartition, offset);
  }
}
