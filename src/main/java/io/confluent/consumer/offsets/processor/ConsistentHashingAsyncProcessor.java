package io.confluent.consumer.offsets.processor;

import io.confluent.consumer.offsets.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsistentHashingAsyncProcessor<K, V> implements Processor<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(ConsistentHashingAsyncProcessor.class);

  private final Processor<K, V> delegate;
  private final Function<K, ?> keyConverter;
  private final ExecutorService[] executors;

  public ConsistentHashingAsyncProcessor(int numberOfThreads, Function<K, ?> keyConverter,
                                         Processor<K, V> delegate) {
    this.delegate = delegate;
    this.keyConverter = keyConverter;
    this.executors = new ExecutorService[numberOfThreads];
    for (int i = 0; i < this.executors.length; i++) {
      this.executors[i] = Executors.newSingleThreadExecutor();
    }
  }

  @Override
  public void process(final K key, final V value) {
    Object newKey = this.keyConverter.apply(key);
    int executorIndex = Math.abs(newKey.hashCode()) % this.executors.length;
    this.executors[executorIndex].execute(new Runnable() {
      @Override
      public void run() {
        try {
          ConsistentHashingAsyncProcessor.this.delegate.process(key, value);
        } catch (Exception e) {
          LOG.error("Error while processing: " + key + " = " + value, e);
        }
      }
    });
  }

  @Override
  public void close() {
    LOG.debug("Submitting close for {} executors", this.executors.length);
    for (ExecutorService executor : this.executors) {
      executor.submit(new Runnable() {
        @Override
        public void run() {
          try {
            ConsistentHashingAsyncProcessor.this.delegate.close();
          } catch (Exception e) {
            LOG.error("Error while closing", e);
          }
        }
      });
    }
    LOG.debug("Shutdown {} executors", this.executors.length);
    for (ExecutorService executor : this.executors) {
      executor.shutdown();
    }
  }
}
