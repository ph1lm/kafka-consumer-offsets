package io.confluent.consumer.offsets.processor;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import kafka.coordinator.GroupTopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicListProcessor implements Processor<GroupTopicPartition, Long> {

  private static final Logger LOG = LoggerFactory.getLogger(TopicListProcessor.class);

  private final Multimap<String, String> groupTopic = TreeMultimap.create();
  private final Multimap<String, String> topicGroup = TreeMultimap.create();

  @Override
  public void process(GroupTopicPartition key, Long value) {
    this.groupTopic.put(key.group(), key.topicPartition().topic());
    this.topicGroup.put(key.topicPartition().topic(), key.group());
  }

  @Override
  public void close() {
    LOG.warn("Groups:");
    for (String group : this.groupTopic.keys()) {
      LOG.warn(group);
    }

    LOG.warn("Topics:");
    for (String topic : this.topicGroup.keys()) {
      LOG.warn(topic);
    }
  }
}
