package io.confluent.consumer.offsets.mirror.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public enum JsonSerializer {
  OBJECT_MAPPER(new ObjectMapper());

  final private ObjectMapper objectMapper;

  JsonSerializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ObjectMapper getInstance() {
    return this.objectMapper;
  }
}
