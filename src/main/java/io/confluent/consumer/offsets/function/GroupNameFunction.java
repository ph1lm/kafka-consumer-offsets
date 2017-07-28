package io.confluent.consumer.offsets.function;

import kafka.coordinator.GroupTopicPartition;

public class GroupNameFunction implements Function<GroupTopicPartition, String> {
  @Override
  public String apply(GroupTopicPartition groupTopicPartition) {
    return groupTopicPartition.group();
  }
}
