package io.confluent.consumer.offsets.converter;

import kafka.common.KafkaException;
import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadata;
import kafka.coordinator.GroupMetadataManager$;
import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MirrorerConverterTest {

  private MirrorerConverter mirrorerConverter = new MirrorerConverter();

  @Test
  public void applyValid() throws Exception {
    final String topic = "topic";
    final String group = "group";
    final int partition = 0;
    final long offset = 0L;

    Map.Entry<GroupTopicPartition, OffsetAndMetadata> converted = this.mirrorerConverter.apply(
        record(topic, group, partition, offset));
    GroupTopicPartition groupTopicPartition = converted.getKey();
    OffsetAndMetadata offsetAndMetadata = converted.getValue();

    assertEquals(group, groupTopicPartition.group());
    assertEquals(topic, groupTopicPartition.topicPartition().topic());
    assertEquals(partition, groupTopicPartition.topicPartition().partition());
    assertEquals(offset, offsetAndMetadata.offset());
  }

  @Test(expected = KafkaException.class)
  public void applyInvalid() throws Exception {
    final byte[] bytes = "invalid".getBytes();
    this.mirrorerConverter.apply(new ConsumerRecord<>("topic", 0, 0L, new Bytes(bytes), new Bytes(bytes)));
  }

  private static ConsumerRecord<Bytes, Bytes> record(String topic, String group, int partition, long offset) {
    GroupMetadataManager$ groupMetadataManager = GroupMetadataManager$.MODULE$;
    byte[] key = groupMetadataManager.offsetCommitKey(group, new TopicPartition(topic, partition), (short) 0);
    OffsetMetadata offsetMetadata = new OffsetMetadata(offset, OffsetMetadata.NoMetadata());
    OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offsetMetadata, 0L, 0L);
    byte[] value = groupMetadataManager.offsetCommitValue(offsetAndMetadata);

    return new ConsumerRecord<>(topic, partition, offset, new Bytes(key), new Bytes(value));
  }
}