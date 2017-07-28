package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.ConsumerOffsetsBlacklist;
import io.confluent.consumer.offsets.converter.ConsumerOffsetsConverter;
import io.confluent.consumer.offsets.processor.ConsumerOffsetsProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConsumerOffsetsLoop<IK, IV, OK, OV> implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ConsumerOffsetsLoop.class);

  private final KafkaConsumer<IK, IV> offsetsConsumer;
  private final ConsumerOffsetsProcessor<OK, OV> offsetsProcessor;
  private final ConsumerOffsetsBlacklist<OK, OV> offsetsBlacklist;
  private final ConsumerOffsetsConverter<IK, IV, OK, OV> offsetsConverter;
  private final String topic;
  private final boolean fromBeginning;
  private final long pollTimeoutMs;
  private final boolean exitIfExhausted;
  private volatile boolean isRunning = true;

  public ConsumerOffsetsLoop(Properties properties,
                             ConsumerOffsetsProcessor<OK, OV> offsetsProcessor,
                             ConsumerOffsetsBlacklist<OK, OV> offsetsBlacklist,
                             ConsumerOffsetsConverter<IK, IV, OK, OV> offsetsConverter,
                             String topic, boolean fromBeginning, long pollTimeoutMs, boolean exitIfExhausted) {
    this.offsetsConsumer = new KafkaConsumer<>(properties);
    this.offsetsProcessor = offsetsProcessor;
    this.offsetsBlacklist = offsetsBlacklist;
    this.offsetsConverter = offsetsConverter;
    this.topic = topic;
    this.fromBeginning = fromBeginning;
    this.pollTimeoutMs = pollTimeoutMs;
    this.exitIfExhausted = exitIfExhausted;
  }

  @Override
  public void run() {
    try {
      subscribe();
      while (this.isRunning) {
        try {
          LOG.debug("Spin start");
          ConsumerRecords<IK, IV> consumerRecords = this.offsetsConsumer.poll(this.pollTimeoutMs);
          LOG.debug("Spin end (exitIfExhausted is {}): {}", this.exitIfExhausted, consumerRecords.count());
          process(convert(consumerRecords));
          if (isTopicExhausted(consumerRecords)) {
            break;
          }
        } catch (WakeupException e) {
          // ignore
        }
      }
    } catch (Exception e) {
      LOG.error("Error in main loop", e);
    } finally {
      this.offsetsConsumer.close();
      this.offsetsProcessor.close();
    }
  }

  private List<Map.Entry<OK, OV>> convert(ConsumerRecords<IK, IV> consumerRecords) {
    // we need to preserve an order
    List<Map.Entry<OK, OV>> entries = new ArrayList<>(consumerRecords.count());
    for (ConsumerRecord<IK, IV> consumerRecord : consumerRecords) {
      try {
        Map.Entry<OK, OV> entry = this.offsetsConverter.apply(consumerRecord);
        if (entry != null) {
          entries.add(entry);
        }
      } catch (Exception e) {
        LOG.error("Error during convert", e);
      }
    }
    return entries;
  }

  private void subscribe() {
    this.offsetsConsumer.subscribe(Collections.singletonList(this.topic));
    if (this.fromBeginning) {
      // initial poll to get list of assignments which is calculated lazily
      this.offsetsConsumer.poll(1000);
      this.offsetsConsumer.seekToBeginning(this.offsetsConsumer.assignment());
    }
  }

  private void process(List<Map.Entry<OK, OV>> entries) {
    for (Map.Entry<OK, OV> entry : entries) {
      try {
        if (!this.offsetsBlacklist.shouldIgnore(entry.getKey(), entry.getValue())) {
          this.offsetsProcessor.process(entry.getKey(), entry.getValue());
        }
      } catch (Exception e) {
        LOG.error("Error during process", e);
      }
    }
  }

  private boolean isTopicExhausted(ConsumerRecords<IK, IV> records) {
    return this.exitIfExhausted && records.isEmpty();
  }

  public void stop() {
    this.isRunning = false;
    this.offsetsConsumer.wakeup();
  }
}
