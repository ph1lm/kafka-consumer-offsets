package io.confluent.consumer.offsets.mirror.service;

import com.codahale.metrics.Meter;
import io.confluent.consumer.offsets.mirror.MirrorMakerHandlerContext;
import io.confluent.consumer.offsets.mirror.entity.Metrics;
import io.confluent.consumer.offsets.mirror.infrastructure.MirrorHandlerConstants;
import io.confluent.consumer.offsets.mirror.tool.MetricsReporter;

public class MirrorMetricsService {
  private static final MirrorMetricsService INSTANCE = new MirrorMetricsService();
  private final Meter meter;
  private final MetricsReporter consoleReporter;
  private final MetricsReporter jmxReporter;

  private MirrorMetricsService() {
    final MirrorMakerHandlerContext context = MirrorMakerHandlerContext.getInstance();
    this.meter = context.getMetricRegistry().meter(MirrorHandlerConstants.RECORD_METER_REGISTRY_NAME);
    this.consoleReporter = context.getConsoleMetricsReporter();
    this.jmxReporter = context.getJmxMetricsReporter();
  }

  public static MirrorMetricsService getInstance() {
    return INSTANCE;
  }

  public Metrics getMetrics() {
    return Metrics.builder()
        .count(this.meter.getCount())
        .meanRate(this.meter.getMeanRate())
        .oneMinuteRate(this.meter.getOneMinuteRate())
        .fiveMinuteRate(this.meter.getFiveMinuteRate())
        .fifteenMinuteRate(this.meter.getFifteenMinuteRate())
        .build();
  }

  public void stopConsoleReporter() {
    this.consoleReporter.stop();
  }

  public void startConsoleReporter() {
    this.consoleReporter.start();
  }

  public void stopJmxReporter() {
    this.jmxReporter.stop();
  }

  public void startJmxReporter() {
    this.jmxReporter.start();
  }
}
