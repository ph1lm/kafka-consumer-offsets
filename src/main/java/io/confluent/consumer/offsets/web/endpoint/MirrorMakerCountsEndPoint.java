package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.entity.MirrorMakerCounts;
import io.confluent.consumer.offsets.mirror.service.MirrorMakerService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class MirrorMakerCountsEndPoint implements HandlerEndPoint {
  private static final String ENDPOINT_CONTEXT = "mirror/maker/counts";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange parameters) {
    return Optional.ofNullable(MirrorMakerService.getInstance().getMirrorMakerCounts())
        .orElse(Collections.emptyList())
        .stream()
        .map(entity -> new MirrorMakerCounts(entity.getTopic(), entity.getCounts()))
        .collect(Collectors.toList());
  }
}
