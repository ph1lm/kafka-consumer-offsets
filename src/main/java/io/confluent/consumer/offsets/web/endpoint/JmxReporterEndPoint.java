package io.confluent.consumer.offsets.web.endpoint;

import io.confluent.consumer.offsets.mirror.service.MirrorMetricsService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;

public class JmxReporterEndPoint extends ReporterEndPoint implements HandlerEndPoint {
  private static final String ENDPOINT_CONTEXT = "mirror/metrics/jmx";

  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  protected void stop() {
    MirrorMetricsService.getInstance().stopJmxReporter();
  }

  @Override
  protected void start() {
    MirrorMetricsService.getInstance().startJmxReporter();
  }
}
