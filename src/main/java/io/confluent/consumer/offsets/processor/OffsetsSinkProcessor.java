package io.confluent.consumer.offsets.processor;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;

public class OffsetsSinkProcessor implements Processor<GroupTopicPartition, OffsetAndMetadata> {

  private static final Logger LOG = LoggerFactory.getLogger(OffsetsSinkProcessor.class);
  private static final String OFFSET_KEY_FORMAT = "%s/%s/%d";
  private static final Callback LOGGING_CALLBACK = new Callback() {
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
      if (exception != null) {
        LOG.error("Error while sinking", exception);
      }
    }
  };

  private final String topic;
  private final KafkaProducer<String, String> producer;

  private OffsetsSinkProcessor(Properties properties, String topic) {
    this.topic = Objects.requireNonNull(topic, "topic is null");
    this.producer = new KafkaProducer<>(Objects.requireNonNull(properties, "properties is null"));
  }

  @Override
  public void process(GroupTopicPartition groupTopicPartition, OffsetAndMetadata offsetAndMetadata) {
    this.producer.send(new ProducerRecord<>(this.topic,
            String.format(OFFSET_KEY_FORMAT,
                groupTopicPartition.group(),
                groupTopicPartition.topicPartition().topic(),
                groupTopicPartition.topicPartition().partition()),
            Long.toString(offsetAndMetadata.offset())),
            LOGGING_CALLBACK);
  }

  public void close() {
    LOG.debug("Closing producer");
    this.producer.flush();
    this.producer.close();
  }

  public static class Builder implements ProcessorBuilder<GroupTopicPartition, OffsetAndMetadata> {

    private Properties properties;
    private String topic;

    public Builder withProperties(Properties properties) {
      this.properties = properties;
      return this;
    }

    public Builder withTopic(String topic) {
      this.topic = topic;
      return this;
    }


    @Override
    public OffsetsSinkProcessor build() {
      return new OffsetsSinkProcessor(this.properties, this.topic);
    }
  }
}
