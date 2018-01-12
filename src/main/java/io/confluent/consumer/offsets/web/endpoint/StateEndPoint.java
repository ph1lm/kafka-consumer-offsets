package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.MirrorStateStore;
import io.confluent.consumer.offsets.web.HandlerEndPoint;

public class StateEndPoint implements HandlerEndPoint {

  private static final String ENDPOINT_CONTEXT = "state";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange headers) {
    return MirrorStateStore.getInstance().getState();
  }
}
