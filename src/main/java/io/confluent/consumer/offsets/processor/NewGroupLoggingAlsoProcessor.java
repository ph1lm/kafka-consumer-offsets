package io.confluent.consumer.offsets.processor;

import kafka.coordinator.GroupTopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Logging processor that in addition logs new consumer groups when they read from offsets topic.
 * This can be used to debug white/black lists.
 *
 * Be aware that {@link WeakHashMap} is used in order to track consumer groups,
 * so already processed groups can be logged few times if they were garbage collected from the map.
 *
 * @param <V>
 */
public class NewGroupLoggingAlsoProcessor<V> extends LoggingProcessor<GroupTopicPartition, V> {

  private static final Logger LOG = LoggerFactory.getLogger(NewGroupLoggingAlsoProcessor.class);

  private final Map<String, String> groups = new WeakHashMap<>();

  @Override
  public void process(GroupTopicPartition key, V value) {
    String group = key.group();
    if (!this.groups.containsKey(group)) {
      LOG.info("New group {}", group);
      this.groups.put(group, group);
    }
    super.process(key, value);
  }
}
