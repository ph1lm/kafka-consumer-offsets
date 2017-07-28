package io.confluent.consumer.offsets.converter;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.BaseKey;
import kafka.coordinator.GroupTopicPartition;
import kafka.coordinator.OffsetKey;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;

import java.util.AbstractMap;
import java.util.Map;

import static java.nio.ByteBuffer.wrap;
import static kafka.coordinator.GroupMetadataManager.readMessageKey;
import static kafka.coordinator.GroupMetadataManager.readOffsetMessageValue;

public class MirrorerConverter
    implements ConsumerOffsetsConverter<Bytes, Bytes, GroupTopicPartition, OffsetAndMetadata> {

  @Override
  public Map.Entry<GroupTopicPartition, OffsetAndMetadata> apply(ConsumerRecord<Bytes, Bytes> consumerRecord) {
    byte[] key = consumerRecord.key().get();
    byte[] value = consumerRecord.value().get();
    BaseKey baseKey = readMessageKey(wrap(key));
    if (baseKey instanceof OffsetKey) {
      OffsetKey offsetKey = (OffsetKey) baseKey;
      GroupTopicPartition groupTopicPartition = offsetKey.key();
      OffsetAndMetadata offsetAndMetadata = value == null ? null : readOffsetMessageValue(wrap(value));
      return new AbstractMap.SimpleImmutableEntry<>(groupTopicPartition, offsetAndMetadata);
    }
    return null;
  }
}
