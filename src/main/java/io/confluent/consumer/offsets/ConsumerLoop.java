package io.confluent.consumer.offsets;

import io.confluent.consumer.offsets.blacklist.Blacklist;
import io.confluent.consumer.offsets.concurrent.IdleStateCondition;
import io.confluent.consumer.offsets.converter.Converter;
import io.confluent.consumer.offsets.processor.Processor;
import org.apache.kafka.clients.consumer.Consumer;
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
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerLoop<IK, IV, OK, OV> implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ConsumerLoop.class);

  private final Consumer<IK, IV> consumer;
  private final Processor<OK, OV> processor;
  private final Blacklist<OK, OV> blacklist;
  private final Converter<IK, IV, OK, OV> converter;
  private final String topic;
  private final boolean fromBeginning;
  private final long pollTimeoutMs;
  private final IdleStateCondition idleStateCondition;
  private final AtomicInteger totalProcessed = new AtomicInteger();
  private final AtomicInteger totalIgnored = new AtomicInteger();

  public ConsumerLoop(Properties properties,
                      Processor<OK, OV> processor,
                      Blacklist<OK, OV> blacklist,
                      Converter<IK, IV, OK, OV> converter,
                      String topic,
                      boolean fromBeginning,
                      long pollTimeoutMs,
                      long idleStateTimeoutSecs) {
    this(new KafkaConsumer<>(properties), processor, blacklist, converter, topic, fromBeginning, pollTimeoutMs,
        idleStateTimeoutSecs);
  }

  public ConsumerLoop(Consumer<IK, IV> consumer,
                      Processor<OK, OV> processor,
                      Blacklist<OK, OV> blacklist,
                      Converter<IK, IV, OK, OV> converter,
                      String topic,
                      boolean fromBeginning,
                      long pollTimeoutMs,
                      long idleStateTimeoutSecs) {
    this.consumer = consumer;
    this.processor = processor;
    this.blacklist = blacklist;
    this.converter = converter;
    this.topic = topic;
    this.fromBeginning = fromBeginning;
    this.pollTimeoutMs = pollTimeoutMs;
    this.idleStateCondition = idleStateTimeoutSecs > 0 ? new IdleStateCondition(idleStateTimeoutSecs) : null;
  }

  @Override
  public void run() {
    try {
      subscribe();
      initIdleState();
      while (true) {
        LOG.debug("Poll start");

        ConsumerRecords<IK, IV> consumerRecords = this.consumer.poll(this.pollTimeoutMs);
        LOG.debug("Number of records is {}", consumerRecords.count());
        List<Map.Entry<OK, OV>> convertedRecords = convert(consumerRecords);
        LOG.debug("Number of records after conversion: {}", convertedRecords.size());
        int processed = process(convertedRecords);

        postponeIdelState(processed);

        this.totalProcessed.addAndGet(processed);
        this.totalIgnored.addAndGet(consumerRecords.count() - processed);

        LOG.debug("Poll end (total ignored: {}, total processed: {})", this.totalIgnored.get(),
            this.totalProcessed.get());
      }
    } catch (WakeupException e) {
      LOG.debug("Exiting main loop");
    } catch (Exception e) {
      LOG.error("Error in main loop", e);
    } finally {
      this.consumer.close();
      this.processor.close();
      if (this.idleStateCondition != null) {
        this.idleStateCondition.close();
      }
    }
  }

  private void postponeIdelState(int processed) {
    if (processed > 0 && this.idleStateCondition != null) {
      this.idleStateCondition.postpone();
    }
  }

  private void initIdleState() {
    if (this.idleStateCondition != null) {
      this.idleStateCondition.async(() -> {
        LOG.info("Idle state occurred");
        ConsumerLoop.this.stop();
      });
    }
  }

  private List<Map.Entry<OK, OV>> convert(ConsumerRecords<IK, IV> consumerRecords) {
    // we need to preserve an order
    List<Map.Entry<OK, OV>> entries = new ArrayList<>(consumerRecords.count());
    for (ConsumerRecord<IK, IV> consumerRecord : consumerRecords) {
      try {
        Map.Entry<OK, OV> entry = this.converter.apply(consumerRecord);
        if (entry != null) {
          entries.add(entry);
        }
      } catch (Exception e) {
        LOG.error("Error while converting record: " + consumerRecord, e);
      }
    }
    return entries;
  }

  private void subscribe() {
    this.consumer.subscribe(Collections.singletonList(this.topic));
    if (this.fromBeginning) {
      // initial poll to get list of assignments which is calculated lazily
      this.consumer.poll(1000);
      this.consumer.seekToBeginning(this.consumer.assignment());
    }
  }

  private int process(List<Map.Entry<OK, OV>> entries) {
    int ignored = 0;
    for (Map.Entry<OK, OV> entry : entries) {
      try {
        if (this.blacklist.shouldIgnore(entry.getKey(), entry.getValue())) {
          ignored++;
        } else {
          this.processor.process(entry.getKey(), entry.getValue());
        }
      } catch (Exception e) {
        LOG.error("Error while processing entry" + entry, e);
        ignored++;
      }
    }
    LOG.debug("{} records were ignored", ignored);
    return entries.size() - ignored;
  }

  public void stop() {
    this.consumer.wakeup();
  }
}
