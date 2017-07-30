package io.confluent.consumer.offsets.processor;

public class ThreadLocalProcessor<K, V> implements ConsumerOffsetsProcessor<K, V> {

  private final ThreadLocal<ConsumerOffsetsProcessor<K, V>> threadLocalDelegate;

  public ThreadLocalProcessor(final ProcessorBuilder<ConsumerOffsetsProcessor<K, V>> builder) {
    this.threadLocalDelegate = new ThreadLocal<ConsumerOffsetsProcessor<K, V>>() {
      @Override
      protected ConsumerOffsetsProcessor<K, V> initialValue() {
        return builder.build();
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
