package io.confluent.consumer.offsets.web.endpoint;

import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.infrastructure.MirrorHandlerConstants;
import io.confluent.consumer.offsets.web.common.JsonSerializer;
import io.confluent.consumer.offsets.web.model.MetricsReporterDto;

import java.io.IOException;

public abstract class ReporterEndPoint {

  public Object processPostRequest(HttpExchange httpExchange) {
    try {
      final MetricsReporterDto metricsReporterDto = JsonSerializer.OBJECT_MAPPER
          .getInstance().readValue(httpExchange.getRequestBody(), MetricsReporterDto.class);
      if (metricsReporterDto.isEnabled()) {
        start();
      } else {
        stop();
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return MirrorHandlerConstants.SUCCESS_RESPONSE;
  }

  protected abstract void stop();

  protected abstract void start();
}
