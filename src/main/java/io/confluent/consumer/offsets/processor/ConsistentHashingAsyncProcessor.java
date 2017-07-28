package io.confluent.consumer.offsets.processor;

import io.confluent.consumer.offsets.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConsistentHashingAsyncProcessor<K, V> implements ConsumerOffsetsProcessor<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(ConsistentHashingAsyncProcessor.class);

  private final ConsumerOffsetsProcessor<K, V> delegate;
  private final Function<K, ?> converter;
  private final ExecutorService[] executors;

  public ConsistentHashingAsyncProcessor(int numberOfThreads, Function<K, ?> converter,
                                         ConsumerOffsetsProcessor<K, V> delegate) {
    this.delegate = delegate;
    this.converter = converter;
    this.executors = new ExecutorService[numberOfThreads];
    for (int i = 0; i < this.executors.length; i++) {
      this.executors[i] = Executors.newSingleThreadExecutor();
    }
  }

  @Override
  public void process(final K key, final V value) {
    Object newKey = this.converter.apply(key);
    int executorIndex = Math.abs(newKey.hashCode()) % this.executors.length;
    this.executors[executorIndex].execute(new Runnable() {
      @Override
      public void run() {
        try {
          ConsistentHashingAsyncProcessor.this.delegate.process(key, value);
        } catch (Exception e) {
          LOG.error("Error during process", e);
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
            LOG.error("Error during close", e);
          }
        }
      });
    }
    LOG.debug("Shutdown {} executors", this.executors.length);
    for (ExecutorService executor : this.executors) {
      executor.shutdown();
    }
    LOG.debug("Await termination for {} executors", this.executors.length);
    for (ExecutorService executor : this.executors) {
      try {
        boolean success = executor.awaitTermination(30, TimeUnit.SECONDS);
        if (!success) {
          LOG.error("Executor service was shutdown while some tasks were still running");
        }
      } catch (Exception e) {
        LOG.error("Error during close", e);
      }
    }
    LOG.debug("{} executors were shutdown", this.executors.length);
  }
}
