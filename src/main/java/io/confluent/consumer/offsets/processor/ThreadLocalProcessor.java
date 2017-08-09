package io.confluent.consumer.offsets.processor;

public class ThreadLocalProcessor<K, V> implements Processor<K, V> {

  private final ThreadLocal<Processor<K, V>> threadLocalDelegate;

  public ThreadLocalProcessor(final ProcessorBuilder<K, V> builder) {
    this.threadLocalDelegate = new ThreadLocal<Processor<K, V>>() {
      @Override
      protected Processor<K, V> initialValue() {
        synchronized (builder) {
          return builder.build();
        }
      }
    };
  }

  @Override
  public void process(K key, V value) {
    this.threadLocalDelegate.get().process(key, value);
  }

  @Override
  public void close() {
    this.threadLocalDelegate.get().close();
  }
}
