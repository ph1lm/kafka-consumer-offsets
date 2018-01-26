package io.confluent.consumer.offsets.web.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public enum JsonSerializer {
  OBJECT_MAPPER(new ObjectMapper());

  private final ObjectMapper objectMapper;

  JsonSerializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ObjectMapper getInstance() {
    return this.objectMapper;
  }
}
