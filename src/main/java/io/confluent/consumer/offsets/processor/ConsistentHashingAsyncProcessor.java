package io.confluent.consumer.offsets.processor;

import io.confluent.consumer.offsets.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConsistentHashingAsyncProcessor<K, V> implements Processor<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(ConsistentHashingAsyncProcessor.class);
  private static final int QUEUE_MAX_SIZE = 1024;

  private final Processor<K, V> delegate;
  private final Function<K, ?> keyConverter;
  private final ExecutorService[] executors;

  public ConsistentHashingAsyncProcessor(int numberOfThreads, Function<K, ?> keyConverter,
                                         Processor<K, V> delegate) {
    this.delegate = delegate;
    this.keyConverter = keyConverter;
    this.executors = new ExecutorService[numberOfThreads];
    for (int i = 0; i < this.executors.length; i++) {
      this.executors[i] = createExecutorService();
    }
  }

  private ExecutorService createExecutorService() {
    return new ThreadPoolExecutor(1, 1,
        0L, TimeUnit.MILLISECONDS, new LimitedQueue<Runnable>(QUEUE_MAX_SIZE));
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
      LOG.debug("Submitting close for executor {}...", executor);
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

  /**
   * Turns offer() and add() into a blocking calls (unless interrupted).
   * Be aware that this approach can be used only if thread pool has {@code corePoolSize == maxPoolSize}.
   */
  private static class LimitedQueue<E> extends LinkedBlockingQueue<E> {

    public LimitedQueue(int maxSize) {
      super(maxSize);
    }

    @Override
    public boolean offer(E e) {
      // turn offer() and add() into a blocking calls (unless interrupted)
      try {
        put(e);
        return true;
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      return false;
    }
  }
}
