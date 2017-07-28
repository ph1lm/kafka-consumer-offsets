package io.confluent.consumer.offsets.blacklist;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.GroupTopicPartition;

public class GroupRegexpBlacklist extends AbstractRegexpBlacklist<GroupTopicPartition, OffsetAndMetadata> {

  public GroupRegexpBlacklist(String pattern) {
    super(pattern);
  }

  @Override
  public boolean shouldIgnore(GroupTopicPartition groupTopicPartition, OffsetAndMetadata offsetAndMetadata) {
    return getPattern().matcher(groupTopicPartition.group()).matches();
  }
}
