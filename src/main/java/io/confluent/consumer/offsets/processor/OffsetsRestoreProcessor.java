package io.confluent.consumer.offsets.processor;

import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class OffsetsRestoreProcessor implements Processor<GroupTopicPartition, Long> {

  private static final Logger LOG = LoggerFactory.getLogger(OffsetsRestoreProcessor.class);

  private final Properties properties;
  private final Map<String, Consumer<Bytes, Bytes>> consumersCache = new HashMap<>();
  private final Map<GroupTopicPartition, Long> maxOffsetsCache = new HashMap<>();

  private OffsetsRestoreProcessor(Properties properties) {
    this.properties = Objects.requireNonNull(properties, "properties is null");
  }

  @Override
  public void process(GroupTopicPartition groupTopicPartition, Long offset) {
    String group = groupTopicPartition.group();
    Consumer<Bytes, Bytes> kafkaConsumer = this.consumersCache.get(group);
    if (kafkaConsumer == null) {
      kafkaConsumer = createKafkaConsumerForGroup(group);
      this.consumersCache.put(group, kafkaConsumer);
    }

    TopicPartition topicPartition = groupTopicPartition.topicPartition();
    if (!kafkaConsumer.assignment().contains(topicPartition)) {
      boolean topicPartitionExist = isTopicPartitionExist(topicPartition,
          kafkaConsumer.partitionsFor(topicPartition.topic()));
      if (topicPartitionExist) {
        List<TopicPartition> topicPartitions = Collections.singletonList(topicPartition);
        kafkaConsumer.assign(topicPartitions);
        Map<TopicPartition, Long> topicPartitionOffsets = kafkaConsumer.endOffsets(topicPartitions);
        long maxOffset = topicPartitionOffsets.get(topicPartition);
        this.maxOffsetsCache.put(groupTopicPartition, maxOffset);
      } else {
        LOG.error("Non-existent topic/partition: {} - {}", topicPartition, offset);
        return;
      }
    }

    long maxOffset = this.maxOffsetsCache.get(groupTopicPartition);
    kafkaConsumer.seek(topicPartition, offset > maxOffset ? maxOffset : offset);
    kafkaConsumer.commitSync();
    LOG.debug("Offset was set: {} - {}", topicPartition, offset);
  }

  private boolean isTopicPartitionExist(TopicPartition topicPartition, List<PartitionInfo> partitionInfos) {
    for (PartitionInfo partitionInfo : partitionInfos) {
      if (partitionInfo.partition() == topicPartition.partition()) {
        return true;
      }
    }
    return false;
  }

  private Consumer<Bytes, Bytes> createKafkaConsumerForGroup(String group) {
    HashMap<String, Object> newProperties = copyProperties(this.properties);
    newProperties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
    newProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE.toString());
    newProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    newProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
    newProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
    return new KafkaConsumer<>(newProperties);
  }

  @Override
  public void close() {
    Collection<Consumer<Bytes, Bytes>> kafkaConsumers = this.consumersCache.values();
    LOG.debug("Closing {} consumers", kafkaConsumers.size());
    for (Consumer<Bytes, Bytes> kafkaConsumer : kafkaConsumers) {
      try {
        kafkaConsumer.close();
      } catch (Exception e) {
        LOG.error("Error while closing", e);
      }
    }
  }

  private static HashMap<String, Object> copyProperties(Properties properties) {
    HashMap<String, Object> newProperties = new HashMap<>();
    for (String property: properties.stringPropertyNames()) {
      newProperties.put(property, properties.getProperty(property));
    }
    return newProperties;
  }

  public static class Builder implements ProcessorBuilder<GroupTopicPartition, Long> {

    private Properties properties;

    public Builder withProperties(Properties properties) {
      this.properties = properties;
      return this;
    }

    @Override
    public OffsetsRestoreProcessor build() {
      return new OffsetsRestoreProcessor(this.properties);
    }
  }
}
