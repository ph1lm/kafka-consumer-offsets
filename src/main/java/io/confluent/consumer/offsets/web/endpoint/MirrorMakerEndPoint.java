package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.service.MirrorMakerService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import io.confluent.consumer.offsets.web.model.MirrorMakerStatsDto;

public class MirrorMakerEndPoint implements HandlerEndPoint {
  private static final String ENDPOINT_CONTEXT = "mirror/maker";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange parameters) {
    return new MirrorMakerStatsDto(MirrorMakerService.getInstance().getMirrorMakerStats());
  }
}
