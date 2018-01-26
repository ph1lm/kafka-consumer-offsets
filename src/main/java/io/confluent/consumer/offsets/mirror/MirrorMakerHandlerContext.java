package io.confluent.consumer.offsets.mirror;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.confluent.consumer.offsets.concurrent.IdleStateCondition;
import io.confluent.consumer.offsets.mirror.infrastructure.EventTopic;
import io.confluent.consumer.offsets.mirror.infrastructure.MirrorHandlerConstants;
import io.confluent.consumer.offsets.mirror.infrastructure.MirrorHandlerProperties;
import io.confluent.consumer.offsets.mirror.infrastructure.ProcessedRecordsTopic;
import io.confluent.consumer.offsets.mirror.tool.ConsoleMetricsReporter;
import io.confluent.consumer.offsets.mirror.tool.JmxMetricsReporter;
import io.confluent.consumer.offsets.web.EmbeddedWebServer;
import io.confluent.consumer.offsets.web.common.EndPointsBuilder;
import kafka.consumer.BaseConsumerRecord;
import lombok.Getter;

@Getter
public class MirrorMakerHandlerContext {

  private static final MirrorMakerHandlerContext INSTANCE = new MirrorMakerHandlerContext();

  private final MirrorBreakerProcessor mirrorBreakerProcessor;
  private final MirrorMakerStateStore mirrorStateStore;
  private final EmbeddedWebServer webServer;
  private final EventTopic<BaseConsumerRecord> processedRecordsTopic;
  private final MirrorMakerMetricsCollector mirrorStatisticsCollector;
  private final ConsoleMetricsReporter consoleMetricsReporter;
  private final JmxMetricsReporter jmxMetricsReporter;
  private final MirrorHandlerProperties properties;
  private final MetricRegistry metricRegistry;

  public static MirrorMakerHandlerContext getInstance() {
    return INSTANCE;
  }

  private MirrorMakerHandlerContext() {
    this.webServer = new EmbeddedWebServer();
    this.mirrorStateStore = new MirrorMakerStateStore();
    this.mirrorBreakerProcessor = new MirrorBreakerProcessor();
    this.processedRecordsTopic = new ProcessedRecordsTopic();
    this.mirrorStatisticsCollector = new MirrorMakerMetricsCollector();
    this.consoleMetricsReporter = new ConsoleMetricsReporter();
    this.jmxMetricsReporter = new JmxMetricsReporter();
    this.metricRegistry = new MetricRegistry();
    this.properties = new MirrorHandlerProperties();
  }


  public void injectProperties() {
    this.mirrorBreakerProcessor.setIdleStateCondition(new IdleStateCondition(this.properties.getIdleStateTimeoutSecs()));
    this.mirrorBreakerProcessor.setMirrorStateStore(this.mirrorStateStore);
    this.mirrorBreakerProcessor.switchModeTo(this.properties.getMirrorBreakerWorkingMode());

    this.processedRecordsTopic.registerObserver(this.mirrorStateStore);
    this.processedRecordsTopic.registerObserver(this.mirrorBreakerProcessor);
    this.processedRecordsTopic.registerObserver(this.mirrorStatisticsCollector);

    final Meter meter = this.metricRegistry.meter(MirrorHandlerConstants.RECORD_METER_REGISTRY_NAME);
    this.mirrorStatisticsCollector.setRecords(meter);
    this.jmxMetricsReporter.registerMetrics(this.metricRegistry);
    this.consoleMetricsReporter.registerMetrics(this.metricRegistry);
    this.consoleMetricsReporter.setPeriod(this.properties.getConsoleReporterPeriod());

    this.webServer.registerEndPoints(new EndPointsBuilder().build());
    this.webServer.setSocketAddress(this.properties.getSocketAddress());
  }

  public void start() {
    this.webServer.start();
    this.mirrorBreakerProcessor.schedule();
  }
}
