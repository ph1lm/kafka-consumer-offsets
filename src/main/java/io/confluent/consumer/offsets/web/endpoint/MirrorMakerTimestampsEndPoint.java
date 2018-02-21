package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.service.MirrorMakerService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import io.confluent.consumer.offsets.web.model.MirrorMakerTimestampsDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class MirrorMakerTimestampsEndPoint implements HandlerEndPoint {
  private static final String ENDPOINT_CONTEXT = "mirror/maker/timestamps";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange parameters) {
    return Optional.ofNullable(MirrorMakerService.getInstance().getMirrorMakerTimestamps())
        .orElse(Collections.emptyList())
        .stream()
        .map(entity -> new MirrorMakerTimestampsDto(entity.getTopic(), entity.getTimestamp()))
        .sorted(Comparator.comparing(MirrorMakerTimestampsDto::getTimestamp))
        .collect(Collectors.toList());
  }
}
