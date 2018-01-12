package io.confluent.consumer.offsets.web.endpoint;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.MirrorStateStore;
import io.confluent.consumer.offsets.mirror.ProgressKey;
import io.confluent.consumer.offsets.mirror.ProgressValue;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Map;

public class ProgressEndPoint implements HandlerEndPoint {

  public static final String ENDPOINT_CONTEXT = "progress";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange parameters) {
    return new Progress(MirrorStateStore.getInstance().getProgress());
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class Progress {
    @JsonSerialize(keyUsing = ProgressKeySerializer.class)
    private Map<ProgressKey, ProgressValue> progress;
  }

  public static class ProgressKeySerializer extends JsonSerializer<ProgressKey> {

    @Override
    public void serialize(ProgressKey value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeFieldName(new StringBuilder().append(value.getTopic())
          .append(":")
          .append(value.getPartition())
          .toString());
    }
  }
}
