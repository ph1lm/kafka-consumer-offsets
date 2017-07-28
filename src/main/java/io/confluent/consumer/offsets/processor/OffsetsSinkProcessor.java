package io.confluent.consumer.offsets.processor;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class OffsetsSinkProcessor implements ConsumerOffsetsProcessor<GroupTopicPartition, OffsetAndMetadata> {

  private static final Logger LOG = LoggerFactory.getLogger(OffsetsSinkProcessor.class);
  private static final String OFFSET_KEY_FORMAT = "%s/%s/%d";
  private static final Callback LOGGING_CALLBACK = new Callback() {
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
      if (exception != null) {
        LOG.error("Error during sink", exception);
      }
    }
  };

  private final Properties properties;
  private final String topic;
  private final ThreadLocal<KafkaProducer<String, String>> offsetsProducer;

  public OffsetsSinkProcessor(Properties properties, String topic) {
    this.properties = properties;
    this.topic = topic;
    this.offsetsProducer = new ThreadLocal<KafkaProducer<String, String>>() {
      @Override
      protected KafkaProducer<String, String> initialValue() {
        return new KafkaProducer<>(OffsetsSinkProcessor.this.properties);
      }
    };
  }

  @Override
  public void process(GroupTopicPartition groupTopicPartition, OffsetAndMetadata offsetAndMetadata) {
    this.offsetsProducer.get().send(new ProducerRecord<>(this.topic,
            String.format(OFFSET_KEY_FORMAT,
                groupTopicPartition.group(),
                groupTopicPartition.topicPartition().topic(),
                groupTopicPartition.topicPartition().partition()),
            Long.toString(offsetAndMetadata.offset())),
        LOGGING_CALLBACK);
  }

  public void close() {
    LOG.debug("Closing producer");
    this.offsetsProducer.get().flush();
    this.offsetsProducer.get().close();
  }
}
