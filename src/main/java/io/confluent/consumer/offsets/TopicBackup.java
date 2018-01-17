package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.Blacklist;
import io.confluent.consumer.offsets.blacklist.CompositeBlacklist;
import io.confluent.consumer.offsets.converter.Converter;
import io.confluent.consumer.offsets.processor.CompositeProcessor;
import io.confluent.consumer.offsets.processor.LoggingProcessor;
import io.confluent.consumer.offsets.processor.Processor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.utils.Bytes;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static java.lang.System.exit;

public class TopicBackup {

  public static void main(String[] args) throws Exception {
    final String topic = "gv-edge-svc-BaseCompanyLastXIDState-changelog";
    final String testGroup = "testGroup";
    final long timestamp = 0;

    OptionParser parser = new OptionParser();
    OptionSpec<String> consumerConfig = parser.accepts("consumer.config",
        "Embedded consumer config for reading from the source cluster.")
        .withRequiredArg()
        .ofType(String.class);
    OptionSpec<String> sourceTopic = parser.accepts("topics",
        "Source topics list deviedd by '' to read consumer offsets.")
        .withRequiredArg()
        .ofType(String.class);
    OptionSpec<String> targetTopic = parser.accepts("bootstrap",
        "Bootstrap url.")
        .withRequiredArg()
        .ofType(String.class)
        .defaultsTo("127.0.0.1:9092");
    OptionSpec help = parser.accepts("help", "Print this message.");

    OptionSet options = parser.parse(args);

    if (args.length == 0 || options.has(help)) {
      parser.printHelpOn(System.out);
      exit(0);
    }

    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, testGroup);
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());


    final List<KeyValue> result = Collections.synchronizedList(new ArrayList<>());
    final String fileName = String.format("%s.dat", topic);
    Processor<Bytes, KeyValue> processor = new CompositeProcessor.Builder<Bytes, KeyValue>()
        .process(new LoggingProcessor<>())
        .process(new Processor<Bytes, KeyValue>() {
          @Override
          public void process(Bytes key, KeyValue value) {
            result.add(value);
          }

          @Override
          public void close() {
            try (DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))) {
              for (KeyValue keyValue : result) {
                writeChunk(dout, keyValue.key);
                writeChunk(dout, keyValue.value);
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        }).build();

    Blacklist<Bytes, KeyValue> blacklist = new CompositeBlacklist.Builder<Bytes, KeyValue>()
        .ignore((key, value) -> value.timestamp < timestamp)
        .build();

    Converter<Bytes, Bytes, Bytes, KeyValue> converter = input ->
        new AbstractMap.SimpleEntry<>(input.key(), new KeyValue(input.key(), input.value(), input.timestamp()));

    final ConsumerLoop<Bytes, Bytes, Bytes, KeyValue> consumerLoop = new ConsumerLoop<>(
        properties, processor, blacklist, converter, topic, true, Integer.MAX_VALUE,
        30);

    Runtime.getRuntime().addShutdownHook(new Thread(consumerLoop::stop));

    Thread thread = new Thread(consumerLoop);
    thread.start();
    thread.join();
  }

  private static void writeChunk(DataOutputStream dout, Bytes chunk) throws IOException {
    byte[] bytes = chunk.get();
    dout.writeInt(bytes.length);
    dout.write(bytes, 0, bytes.length);
  }

  public static class KeyValue {
    final Bytes key;
    final Bytes value;
    final long timestamp;

    public KeyValue(Bytes key, Bytes value, long timestamp) {
      this.key = key;
      this.value = value;
      this.timestamp = timestamp;
    }
  }
}
