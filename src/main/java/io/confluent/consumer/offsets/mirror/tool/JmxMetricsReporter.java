package io.confluent.consumer.offsets.mirror.tool;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class JmxMetricsReporter implements MetricsReporter {
  private static final Logger LOG = LoggerFactory.getLogger(JmxMetricsReporter.class);
  private final AtomicReference<JmxReporter> reporter;
  private MetricRegistry metrics;

  public JmxMetricsReporter() {
    this.reporter = new AtomicReference<>();
  }

  @Override
  public void registerMetrics(final MetricRegistry metrics) {
    this.metrics = metrics;
    LOG.warn("JMX reporter was not Started");
  }

  @Override
  public void start() {
    Preconditions.checkArgument(this.reporter.get() == null, "JMX reporter was already started");
    this.reporter.set(JmxReporter
        .forRegistry(this.metrics)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build());
    this.reporter.get().start();
    LOG.info("JMX reporter was Started");
  }

  @Override
  public void stop() {
    JmxReporter jmxReporter = this.reporter.getAndSet(null);
    Preconditions.checkArgument(jmxReporter != null, "JMX reporter is not running");
    jmxReporter.stop();
    LOG.info("JMX reporter was Stopped");
  }
}
