package io.confluent.consumer.offsets.mirror.infrastructure;

public interface EventObserver<T> {

  void onSubjectEvent(Subject<T> context);
}
