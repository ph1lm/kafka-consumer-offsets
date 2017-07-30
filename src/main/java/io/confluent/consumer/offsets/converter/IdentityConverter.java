package io.confluent.consumer.offsets.converter;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.AbstractMap;
import java.util.Map;

public class IdentityConverter<I, O> implements Converter<I, O, I, O> {
  @Override
  public Map.Entry<I, O> apply(ConsumerRecord<I, O> consumerRecord) {
    return new AbstractMap.SimpleImmutableEntry<>(consumerRecord.key(), consumerRecord.value());
  }
}
