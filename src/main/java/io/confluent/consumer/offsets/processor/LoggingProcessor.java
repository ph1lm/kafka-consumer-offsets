package io.confluent.consumer.offsets.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProcessor<K, V> implements ConsumerOffsetsProcessor<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingProcessor.class);

  @Override
  public void process(K key, V value) {
    LOG.trace("Processing offset: {} - {} ", key, value);
  }

  @Override
  public void close() {
  }
}
