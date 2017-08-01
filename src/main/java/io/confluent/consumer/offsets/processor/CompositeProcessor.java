package io.confluent.consumer.offsets.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CompositeProcessor<K, V> implements Processor<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(CompositeProcessor.class);

  private final List<Processor<K, V>> processors;

  private CompositeProcessor(List<Processor<K, V>> processors) {
    this.processors = Objects.requireNonNull(processors, "processors is null");
  }

  @Override
  public void process(K key, V value) {
    for (Processor<K, V> processor : this.processors) {
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
    for (Processor<K, V> processor : this.processors) {
      try {
        processor.close();
      } catch (Exception e) {
        LOG.error("Error while closing", e);
      }
    }
  }

  public static class Builder<K, V> implements ProcessorBuilder<K, V> {

    private final List<Processor<K, V>> processors = new LinkedList<>();

    public Builder<K, V> process(Processor<K, V> processor) {
      this.processors.add(processor);
      return this;
    }

    @Override
    public Processor<K, V> build() {
      return new CompositeProcessor<>(Collections.unmodifiableList(this.processors));
    }
  }
}
