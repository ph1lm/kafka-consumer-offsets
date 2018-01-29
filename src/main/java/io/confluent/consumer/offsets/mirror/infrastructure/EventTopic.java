package io.confluent.consumer.offsets.mirror.infrastructure;

public interface EventTopic<T> {
  void registerObserver(EventObserver<T> observer);

  void publishEvent(Subject<T> subject);
}
