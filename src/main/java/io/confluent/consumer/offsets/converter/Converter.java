package io.confluent.consumer.offsets.converter;

import io.confluent.consumer.offsets.function.Function;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public interface Converter<IK, IV, OK, OV> extends Function<ConsumerRecord<IK, IV>, Map.Entry<OK, OV>> {
}
