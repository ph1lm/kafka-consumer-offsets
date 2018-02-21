package io.confluent.consumer.offsets.mirror.service;

import com.google.common.base.Preconditions;
import io.confluent.consumer.offsets.mirror.MirrorMakerHandlerContext;
import io.confluent.consumer.offsets.mirror.MirrorMakerStateStore;
import io.confluent.consumer.offsets.mirror.entity.MirrorMaker;
import io.confluent.consumer.offsets.mirror.entity.MirrorMakerCounts;
import io.confluent.consumer.offsets.mirror.entity.MirrorMakerTimestamps;
import io.confluent.consumer.offsets.mirror.entity.TopicStats;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MirrorMakerService {
  private static final MirrorMakerService INSTANCE = new MirrorMakerService();
  private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MM-yyyy hh:mm:ss SSS"));
  private final MirrorMakerStateStore mirrorStateStore;

  private MirrorMakerService() {
    final MirrorMakerHandlerContext context = MirrorMakerHandlerContext.getInstance();
    this.mirrorStateStore = context.getMirrorStateStore();
  }

  public static MirrorMakerService getInstance() {
    return INSTANCE;
  }

  public MirrorMaker getMirrorMakerState() {
    return new MirrorMaker(this.mirrorStateStore.getState());
  }

  public List<TopicStats> getTopicStats() {
    return this.mirrorStateStore.getTopicsStats()
        .entrySet()
        .stream()
        .map(entry ->
            TopicStats.builder()
                .name(entry.getKey().getTopic())
                .partition(entry.getKey().getPartition())
                .offset(entry.getValue().getOffset())
                .count(entry.getValue().getCount())
                .date(entry.getValue().getDate())
                .build())
        .sorted()
        .collect(Collectors.toList());
  }

  public List<TopicStats> getTopicStats(String topicName) {
    Preconditions.checkArgument(topicName != null);

    return this.mirrorStateStore.getTopicsStats()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().getTopic().equals(topicName))
        .map(entry ->
            TopicStats.builder()
                .name(entry.getKey().getTopic())
                .partition(entry.getKey().getPartition())
                .offset(entry.getValue().getOffset())
                .count(entry.getValue().getCount())
                .date(entry.getValue().getDate())
                .build())
        .sorted()
        .collect(Collectors.toList());
  }

  public List<MirrorMakerCounts> getMirrorMakerCounts() {
    return this.mirrorStateStore.getTopicsStats()
        .entrySet()
        .stream()
        .collect(Collectors.groupingBy(entry -> entry.getKey().getTopic(),
            Collectors.summingLong(entry -> entry.getValue().getCount())))
        .entrySet()
        .stream()
        .map(entry -> new MirrorMakerCounts(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  public List<MirrorMakerTimestamps> getMirrorMakerTimestamps() {
    return this.mirrorStateStore.getTopicsStats()
        .entrySet()
        .stream()
        .collect(Collectors.groupingBy(entry -> entry.getKey().getTopic(),
            Collectors.mapping(entry -> entry.getValue().getDate(), Collectors.collectingAndThen(
                Collectors.maxBy(Date::compareTo), optional -> optional.get()))))
        .entrySet()
        .stream().map(entry -> new MirrorMakerTimestamps(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}
