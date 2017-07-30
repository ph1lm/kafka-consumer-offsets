package io.confluent.consumer.offsets.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CompositeProcessor<K, V> implements ConsumerOffsetsProcessor<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(CompositeProcessor.class);

  private final List<ConsumerOffsetsProcessor<K, V>> processors;

  private CompositeProcessor(List<ConsumerOffsetsProcessor<K, V>> processors) {
    this.processors = processors;
  }

  @Override
  public void process(K key, V value) {
    for (ConsumerOffsetsProcessor<K, V> processor : this.processors) {
      try {
        processor.process(key, value);
      } catch (Exception e) {
        LOG.error("Error while processing", e);
      }
    }
  }

  @Override
  public void close() {
    LOG.trace("Closing {} processors", this.processors.size());
    for (ConsumerOffsetsProcessor<K, V> processor : this.processors) {
      try {
        processor.close();
      } catch (Exception e) {
        LOG.error("Error while closing", e);
      }
    }
  }

  public static class Builder<K, V> implements
      ProcessorBuilder<ConsumerOffsetsProcessor<K, V>> {

    private final List<ConsumerOffsetsProcessor<K, V>> processors = new LinkedList<>();

    public Builder<K, V> process(ConsumerOffsetsProcessor<K, V> processor) {
      this.processors.add(processor);
      return this;
    }

    @Override
    public ConsumerOffsetsProcessor<K, V> build() {
      return new CompositeProcessor<>(Collections.unmodifiableList(this.processors));
    }
  }
}
