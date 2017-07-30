package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.IgnoreNothingBlacklist;
import io.confluent.consumer.offsets.converter.ConsumerOffsetsConverter;
import io.confluent.consumer.offsets.converter.RestorerConverter;
import io.confluent.consumer.offsets.function.GroupNameFunction;
import io.confluent.consumer.offsets.processor.ConsistentHashingAsyncProcessor;
import io.confluent.consumer.offsets.processor.ConsumerOffsetsProcessor;
import io.confluent.consumer.offsets.processor.LoggingProcessor;
import io.confluent.consumer.offsets.processor.CompositeProcessor;
import io.confluent.consumer.offsets.processor.OffsetsRestoreProcessor;
import io.confluent.consumer.offsets.processor.ThreadLocalProcessor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import kafka.coordinator.GroupTopicPartition;

import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.exit;

public class ConsumerOffsetsRestorer {

  public static void main(String[] args) throws Exception {
    OptionParser parser = new OptionParser();
    OptionSpec<String> consumerConfig = parser.accepts("consumer.config",
        "Embedded consumer config where offsets should be be restored.")
        .withRequiredArg()
        .ofType(String.class);
    OptionSpec<String> sourceTopic = parser.accepts("source.topic",
        "Source topic to read consumer offsets.")
        .withRequiredArg()
        .ofType(String.class)
        .defaultsTo("replica_consumer_offsets");
    OptionSpec<Integer> numberOfThreads = parser.accepts("num.threads",
        "Number of production threads.")
        .withRequiredArg()
        .ofType(Integer.class)
        .defaultsTo(10);
    OptionSpec fromBeginning = parser.accepts("from-beginning",
        "Start consumption from the beginning of a topic.");
    OptionSpec<Integer> pollTimeoutMs = parser.accepts("poll-timeout-ms",
        "Poll timeout for source topic.")
        .withRequiredArg()
        .ofType(Integer.class)
        .defaultsTo(5000);
    OptionSpec help = parser.accepts("help", "Print this message.");

    OptionSet options = parser.parse(args);

    if (args.length == 0 || options.has(help)) {
      parser.printHelpOn(System.out);
      exit(0);
    }

    if (!options.has(consumerConfig)) {
      System.out.println(format("Missing required argument: %s", consumerConfig));
      parser.printHelpOn(System.out);
      exit(0);
    }

    Properties consumerProperties = new Properties();
    try (Reader reader = new FileReader(options.valueOf(consumerConfig))) {
      consumerProperties.load(reader);
    }

    ConsumerOffsetsProcessor<GroupTopicPartition, Long> offsetsProcessor
        = new CompositeProcessor.Builder<GroupTopicPartition, Long>()
          .process(new LoggingProcessor<GroupTopicPartition, Long>())
          .process(new ConsistentHashingAsyncProcessor<>(options.valueOf(numberOfThreads),
              new GroupNameFunction(), new ThreadLocalProcessor<>(
              new OffsetsRestoreProcessor.Builder().withProperties(consumerProperties))))
          .build();

    ConsumerOffsetsConverter<String, String, GroupTopicPartition, Long> restorerConverter = new RestorerConverter();

    final ConsumerOffsetsLoop<String, String, GroupTopicPartition, Long> consumerOffsetsLoop =
        new ConsumerOffsetsLoop<>(consumerProperties, offsetsProcessor,
            new IgnoreNothingBlacklist<GroupTopicPartition, Long>(), restorerConverter, options.valueOf(sourceTopic),
            options.has(fromBeginning), options.valueOf(pollTimeoutMs), true);

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
