package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.CompositeBlacklist;
import io.confluent.consumer.offsets.blacklist.Blacklist;
import io.confluent.consumer.offsets.blacklist.GroupRegexpBlacklist;
import io.confluent.consumer.offsets.blacklist.IgnoreNothingBlacklist;
import io.confluent.consumer.offsets.blacklist.TopicRegexpBlacklist;
import io.confluent.consumer.offsets.converter.Converter;
import io.confluent.consumer.offsets.converter.MirrorerConverter;
import io.confluent.consumer.offsets.function.IdentityFunction;
import io.confluent.consumer.offsets.processor.ConsistentHashingAsyncProcessor;
import io.confluent.consumer.offsets.processor.Processor;
import io.confluent.consumer.offsets.processor.LoggingProcessor;
import io.confluent.consumer.offsets.processor.CompositeProcessor;
import io.confluent.consumer.offsets.processor.OffsetsSinkProcessor;
import io.confluent.consumer.offsets.processor.ThreadLocalProcessor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import kafka.common.OffsetAndMetadata;
import kafka.coordinator.GroupTopicPartition;
import org.apache.kafka.common.utils.Bytes;

import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.exit;

public class ConsumerOffsetsMirrorer {

  public static void main(String[] args) throws Exception {
    OptionParser parser = new OptionParser();
    OptionSpec<String> consumerConfig = parser.accepts("consumer.config",
        "Embedded consumer config for reading from the source cluster.")
        .withRequiredArg()
        .ofType(String.class);
    OptionSpec<String> producerConfig = parser.accepts("producer.config",
        "Embedded producer config for writing events on target cluster.")
        .withRequiredArg()
        .ofType(String.class);
    OptionSpec<String> sourceTopic = parser.accepts("source.topic",
        "Source topic to read consumer offsets.")
        .withRequiredArg()
        .ofType(String.class)
        .defaultsTo("__consumer_offsets");
    OptionSpec<String> targetTopic = parser.accepts("target.topic",
        "Target topic to write consumer offsets.")
        .withRequiredArg()
        .ofType(String.class)
        .defaultsTo("replica_consumer_offsets");
    OptionSpec<Integer> numberOfThreads = parser.accepts("num.threads",
        "Number of production threads.")
        .withRequiredArg()
        .ofType(Integer.class)
        .defaultsTo(10);
    OptionSpec<Integer> pollTimeoutMs = parser.accepts("poll-timeout-ms",
        "Poll timeout for source topic.")
        .withRequiredArg()
        .ofType(Integer.class)
        .defaultsTo(Integer.MAX_VALUE);
    OptionSpec<Integer> idleStateTimeoutSecs = parser.accepts("idle-state-timeout-secs",
        "Application closes if nothing is arrived from the topic during this timeout.")
        .withRequiredArg()
        .ofType(Integer.class);
    OptionSpec<String> groupBlackList = parser.accepts("groupBlacklist",
        "Regular expression that filters out groups that should not be restored.")
        .withRequiredArg()
        .ofType(String.class);
    OptionSpec<String> topicBlackList = parser.accepts("topicBlacklist",
        "Regular expression that filters out topics that should not be restored.")
        .withRequiredArg()
        .ofType(String.class);
    OptionSpec fromBeginning = parser.accepts("from-beginning",
        "Start consumption from the beginning of a topic.");
    OptionSpec help = parser.accepts("help", "Print this message.");

    OptionSet options = parser.parse(args);

    if (args.length == 0 || options.has(help)) {
      parser.printHelpOn(System.out);
      exit(0);
    }

    for (OptionSpec<?> requiredOption : Arrays.asList(consumerConfig, producerConfig, idleStateTimeoutSecs)) {
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

    Properties producerProperties = new Properties();
    try (Reader reader = new FileReader(options.valueOf(producerConfig))) {
      producerProperties.load(reader);
    }

    Processor<GroupTopicPartition, OffsetAndMetadata> processor
        = new CompositeProcessor.Builder<GroupTopicPartition, OffsetAndMetadata>()
            .process(new LoggingProcessor<GroupTopicPartition, OffsetAndMetadata>())
            .process(new ConsistentHashingAsyncProcessor<>(options.valueOf(numberOfThreads),
                new IdentityFunction<GroupTopicPartition>(),
                new ThreadLocalProcessor<>(new OffsetsSinkProcessor.Builder()
                    .withProperties(producerProperties)
                    .withTopic(options.valueOf(targetTopic)))))
            .build();

    Blacklist<GroupTopicPartition, OffsetAndMetadata> blacklist
        = new CompositeBlacklist.Builder<GroupTopicPartition, OffsetAndMetadata>()
          .ignore(new GroupRegexpBlacklist("kafka-consumer-offsets.*")) // ignore offsets generated by this tool
          .ignore(new GroupRegexpBlacklist("console-consumer.*")) // ignore all console consumers
          .ignore(new TopicRegexpBlacklist("_.*")) // ignore system topics
          .ignore(options.has(groupBlackList) ? new GroupRegexpBlacklist(options.valueOf(groupBlackList)) :
              new IgnoreNothingBlacklist<GroupTopicPartition, OffsetAndMetadata>())
          .ignore(options.has(topicBlackList) ? new TopicRegexpBlacklist(options.valueOf(topicBlackList)) :
              new IgnoreNothingBlacklist<GroupTopicPartition, OffsetAndMetadata>())
          .build();

    Converter<Bytes, Bytes, GroupTopicPartition, OffsetAndMetadata> converter =
        new MirrorerConverter();

    final ConsumerLoop<Bytes, Bytes, GroupTopicPartition, OffsetAndMetadata> consumerLoop =
        new ConsumerLoop<>(consumerProperties, processor, blacklist, converter,
            options.valueOf(sourceTopic), options.has(fromBeginning), options.valueOf(pollTimeoutMs),
              options.valueOf(idleStateTimeoutSecs));

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        consumerLoop.stop();
      }
    });

    Thread thread = new Thread(consumerLoop);
    thread.start();
    thread.join();

    System.out.println("Exiting...");
    System.exit(0);
  }
}
