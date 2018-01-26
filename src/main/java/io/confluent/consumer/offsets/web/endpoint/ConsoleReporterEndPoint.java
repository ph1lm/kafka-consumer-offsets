package io.confluent.consumer.offsets.web.endpoint;

import io.confluent.consumer.offsets.mirror.service.MirrorMetricsService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;

public class ConsoleReporterEndPoint extends ReporterEndPoint implements HandlerEndPoint {
  private static final String ENDPOINT_CONTEXT = "mirror/metrics/console";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  protected void stop() {
    MirrorMetricsService.getInstance().stopConsoleReporter();
  }

  @Override
  protected void start() {
    MirrorMetricsService.getInstance().startConsoleReporter();
  }
}
