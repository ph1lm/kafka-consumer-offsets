package io.confluent.consumer.offsets.mirror.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.confluent.consumer.offsets.mirror.service.MirrorMakerService;
import io.confluent.consumer.offsets.web.common.JsonSerializer;
import io.confluent.consumer.offsets.web.model.MirrorMakerTimestampsDto;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Setter
public class TimestampsLogger {
  private static final Logger LOG = LoggerFactory.getLogger(TimestampsLogger.class);
  private final ScheduledExecutorService scheduler;
  private long period;

  public TimestampsLogger() {
    this.scheduler = Executors.newScheduledThreadPool(1);
  }

  public void start() {
    this.scheduler.schedule(() -> {
      try {
        LOG.info("TIMESTAMPS: {}", JsonSerializer.OBJECT_MAPPER.getInstance()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(MirrorMakerService.getInstance().getMirrorMakerTimestamps()
                .stream()
                .map(entity -> new MirrorMakerTimestampsDto(entity.getTopic(), entity.getTimestamp()))
                .collect(Collectors.toList())
            ));
      } catch (JsonProcessingException e) {
        LOG.error("Timestamps logging failed");
      }
    }, this.period, TimeUnit.SECONDS);
  }
}
