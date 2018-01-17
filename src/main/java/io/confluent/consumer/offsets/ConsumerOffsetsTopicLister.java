package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.IgnoreNothingBlacklist;
import io.confluent.consumer.offsets.converter.Converter;
import io.confluent.consumer.offsets.converter.RestorerConverter;
import io.confluent.consumer.offsets.processor.CompositeProcessor;
import io.confluent.consumer.offsets.processor.LoggingProcessor;
import io.confluent.consumer.offsets.processor.Processor;
import io.confluent.consumer.offsets.processor.TopicListProcessor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import kafka.coordinator.GroupTopicPartition;

import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.exit;

public class ConsumerOffsetsTopicLister {

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
    OptionSpec fromBeginning = parser.accepts("from-beginning",
        "Start consumption from the beginning of a topic.");
    OptionSpec<Integer> pollTimeoutMs = parser.accepts("poll-timeout-ms",
        "Poll timeout for source topic.")
        .withRequiredArg()
        .ofType(Integer.class)
        .defaultsTo(Integer.MAX_VALUE);
    OptionSpec<Integer> idleStateTimeoutSecs = parser.accepts("idle-state-timeout-secs",
        "Application closes if nothing is arrived from the topic during this timeout.")
        .withRequiredArg()
        .ofType(Integer.class);

    OptionSpec help = parser.accepts("help", "Print this message.");

    OptionSet options = parser.parse(args);

    if (args.length == 0 || options.has(help)) {
      parser.printHelpOn(System.out);
      exit(0);
    }

    for (OptionSpec<?> requiredOption : Arrays.asList(consumerConfig, idleStateTimeoutSecs)) {
      if (!options.has(requiredOption)) {
        System.out.println(format("Missing required argument: %s", requiredOption));
        parser.printHelpOn(System.out);
        exit(0);
      }
    }

    Properties consumerProperties = new Properties();
    try (Reader reader = new FileReader(options.valueOf(consumerConfig))) {
      consumerProperties.load(reader);
    }

    Processor<GroupTopicPartition, Long> processor
        = new CompositeProcessor.Builder<GroupTopicPartition, Long>()
          .process(new LoggingProcessor<>())
          .process(new TopicListProcessor())
          .build();

    Converter<String, String, GroupTopicPartition, Long> restorerConverter = new RestorerConverter();

    final ConsumerLoop<String, String, GroupTopicPartition, Long> consumerLoop =
        new ConsumerLoop<>(consumerProperties, processor,
            new IgnoreNothingBlacklist<>(), restorerConverter, options.valueOf(sourceTopic),
              options.has(fromBeginning), options.valueOf(pollTimeoutMs), options.valueOf(idleStateTimeoutSecs));

    Runtime.getRuntime().addShutdownHook(new Thread(consumerLoop::stop));

    Thread thread = new Thread(consumerLoop);
    thread.start();
    thread.join();

    System.out.println("Exiting...");
    System.exit(0);
  }
}
