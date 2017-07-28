package io.confluent.consumer.offsets.blacklist;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.GroupTopicPartition;

public class TopicRegexpBlacklist extends AbstractRegexpBlacklist<GroupTopicPartition, OffsetAndMetadata> {

  public TopicRegexpBlacklist(String pattern) {
    super(pattern);
  }

  @Override
  public boolean shouldIgnore(GroupTopicPartition groupTopicPartition, OffsetAndMetadata offsetAndMetadata) {
    return getPattern().matcher(groupTopicPartition.topicPartition().topic()).matches();
  }
}
