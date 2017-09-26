package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.CompositeBlacklist;
import io.confluent.consumer.offsets.blacklist.Blacklist;
import io.confluent.consumer.offsets.blacklist.IgnoreNothingBlacklist;
import io.confluent.consumer.offsets.converter.Converter;
import io.confluent.consumer.offsets.converter.IdentityConverter;
import io.confluent.consumer.offsets.processor.CompositeProcessor;
import io.confluent.consumer.offsets.processor.Processor;
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

    Processor<String, String> processor
        = new CompositeProcessor.Builder<String, String>()
        .process(new LoggingProcessor<String, String>())
        .build();

    Blacklist<String, String> blacklist
        = new CompositeBlacklist.Builder<String, String>()
        .ignore(new IgnoreNothingBlacklist<String, String>())
        .build();

    Converter<String, String, String, String> converter = new IdentityConverter<>();

    final ConsumerLoop<String, String, String, String> consumerLoop = new ConsumerLoop<>(
        properties, processor, blacklist, converter, topic, false, Integer.MAX_VALUE,
        30);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        consumerLoop.stop();
      }
    });

    Thread thread = new Thread(consumerLoop);
    thread.start();
    thread.join();
  }
}
