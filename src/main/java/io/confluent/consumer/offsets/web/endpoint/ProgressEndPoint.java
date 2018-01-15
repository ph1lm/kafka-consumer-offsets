package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.MirrorStateStore;
import io.confluent.consumer.offsets.web.HandlerEndPoint;

public class ProgressEndPoint implements HandlerEndPoint {

  public static final String ENDPOINT_CONTEXT = "progress";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange parameters) {
    return MirrorStateStore.getInstance().getProgress();
  }
}
