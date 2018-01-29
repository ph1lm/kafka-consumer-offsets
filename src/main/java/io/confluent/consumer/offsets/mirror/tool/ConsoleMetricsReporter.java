package io.confluent.consumer.offsets.mirror.tool;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Setter
public class ConsoleMetricsReporter implements MetricsReporter {
  private static final Logger LOG = LoggerFactory.getLogger(ConsoleMetricsReporter.class);
  private int period;
  private final AtomicReference<ConsoleReporter> reporter;
  private MetricRegistry metrics;

  public ConsoleMetricsReporter() {
    this.reporter = new AtomicReference<>();
  }

  @Override
  public void registerMetrics(MetricRegistry metrics) {
    this.metrics = metrics;
    LOG.warn("Console reporter was not Started");
  }

  @Override
  public void start() {
    Preconditions.checkArgument(this.reporter.get() == null, "Console reporter was already started");
    this.reporter.set(ConsoleReporter
        .forRegistry(this.metrics)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build());

    this.reporter.get().start(this.period, TimeUnit.SECONDS);
    LOG.info("Console reporter was Started");
  }

  @Override
  public void stop() {
    ConsoleReporter consoleReporter = this.reporter.getAndSet(null);
    Preconditions.checkArgument(consoleReporter != null, "Console reporter is not running");
    consoleReporter.stop();
    LOG.info("Console reporter was Stopped");
  }
}
