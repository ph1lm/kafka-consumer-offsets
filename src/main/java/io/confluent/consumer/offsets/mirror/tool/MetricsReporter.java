package io.confluent.consumer.offsets.mirror.tool;

import com.codahale.metrics.MetricRegistry;

public interface MetricsReporter {

  void registerMetrics(MetricRegistry metricRegistry);

  void start();

  void stop();
}
