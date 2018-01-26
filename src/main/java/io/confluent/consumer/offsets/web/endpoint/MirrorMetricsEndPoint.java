package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.service.MirrorMetricsService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import io.confluent.consumer.offsets.web.model.MetricsDto;

public class MirrorMetricsEndPoint implements HandlerEndPoint {
  private static final String ENDPOINT_CONTEXT = "mirror/metrics";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange parameters) {
    return new MetricsDto(MirrorMetricsService.getInstance().getMetrics());
  }
}
