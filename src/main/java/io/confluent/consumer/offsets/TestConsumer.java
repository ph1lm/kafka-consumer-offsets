package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.CompositeBlacklist;
import io.confluent.consumer.offsets.blacklist.ConsumerOffsetsBlacklist;
import io.confluent.consumer.offsets.blacklist.IgnoreNothingBlacklist;
import io.confluent.consumer.offsets.converter.ConsumerOffsetsConverter;
import io.confluent.consumer.offsets.converter.IdentityConverter;
import io.confluent.consumer.offsets.processor.CompositeProcessor;
import io.confluent.consumer.offsets.processor.ConsumerOffsetsProcessor;
import io.confluent.consumer.offsets.processor.LoggingProcessor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class TestConsumer {

  public static void main(String[] args) throws InterruptedException {
    String topic = "testTopic";
    String testGroup = "testGroup";

    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, testGroup);
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    ConsumerOffsetsProcessor<String, String> offsetsProcessor
        = new CompositeProcessor.Builder<String, String>()
        .process(new LoggingProcessor<String, String>())
        .build();

    ConsumerOffsetsBlacklist<String, String> offsetsBlacklist
        = new CompositeBlacklist.Builder<String, String>()
        .ignore(new IgnoreNothingBlacklist<String, String>())
        .build();

    ConsumerOffsetsConverter<String, String, String, String> offsetsConverter = new IdentityConverter<>();

    final ConsumerOffsetsLoop<String, String, String, String> consumerOffsetsLoop = new ConsumerOffsetsLoop<>(
        properties, offsetsProcessor, offsetsBlacklist, offsetsConverter, topic, false, Integer.MAX_VALUE,
        false);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        consumerOffsetsLoop.stop();
      }
    });

    Thread thread = new Thread(consumerOffsetsLoop);
    thread.start();
    thread.join();
  }
}
