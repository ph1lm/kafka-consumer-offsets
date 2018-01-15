package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.MirrorBreakerMode;
import io.confluent.consumer.offsets.mirror.MirrorStateStore;
import io.confluent.consumer.offsets.mirror.common.JsonSerializer;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;

public class ModeEndPoint implements HandlerEndPoint {

  private static final String ENDPOINT_CONTEXT = "mode";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange httpExchange) {
    return MirrorStateStore.getInstance().getMode();
  }

  @Override
  public Object processPostRequest(HttpExchange httpExchange) {
    try {
      Mode mode = JsonSerializer.OBJECT_MAPPER
          .getInstance().readValue(httpExchange.getRequestBody(), Mode.class);
      MirrorStateStore.getInstance().switchModeTo(mode.getMode());
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    return String.format("Mode was switched to %s", MirrorStateStore.getInstance().getMode());
  }

  @Getter
  @Setter
  @NoArgsConstructor
  static class Mode {
    private MirrorBreakerMode mode;
  }
}
