package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.entity.MirrorBreaker;
import io.confluent.consumer.offsets.mirror.infrastructure.MirrorHandlerConstants;
import io.confluent.consumer.offsets.mirror.service.MirrorBreakerService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import io.confluent.consumer.offsets.web.common.JsonSerializer;
import io.confluent.consumer.offsets.web.model.MirrorBreakerDto;

import java.io.IOException;

public class MirrorBreakerEndPoint implements HandlerEndPoint {

  private static final String ENDPOINT_CONTEXT = "mirror/breaker";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange httpExchange) {
    return new MirrorBreakerDto(MirrorBreakerService.getInstance().getCurrentMode());
  }

  @Override
  public Object processPostRequest(HttpExchange httpExchange) {
    try {
      final MirrorBreakerDto mirrorBreakerDto = JsonSerializer.OBJECT_MAPPER
          .getInstance().readValue(httpExchange.getRequestBody(), MirrorBreakerDto.class);
      MirrorBreakerService.getInstance().update(new MirrorBreaker(mirrorBreakerDto));
    } catch (IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return MirrorHandlerConstants.SUCCESS_RESPONSE;
  }
}
