package io.confluent.consumer.offsets.mirror.service;

import com.google.common.base.Preconditions;
import io.confluent.consumer.offsets.mirror.MirrorMakerHandlerContext;
import io.confluent.consumer.offsets.mirror.MirrorMakerStateStore;
import io.confluent.consumer.offsets.mirror.entity.MirrorMakerStats;
import io.confluent.consumer.offsets.mirror.entity.TopicStats;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSortedMap.copyOf;

public class MirrorMakerService {
  private static final MirrorMakerService INSTANCE = new MirrorMakerService();
  private final MirrorMakerStateStore mirrorStateStore;

  private MirrorMakerService() {
    final MirrorMakerHandlerContext context = MirrorMakerHandlerContext.getInstance();
    this.mirrorStateStore = context.getMirrorStateStore();
  }

  public static MirrorMakerService getInstance() {
    return INSTANCE;
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
                .build())
        .sorted()
        .collect(Collectors.toList());
  }

  public MirrorMakerStats getMirrorMakerStats() {
    final MirrorMakerStats mirrorMaker = new MirrorMakerStats();
    mirrorMaker.setMirrorMakerState(this.mirrorStateStore.getState());

    mirrorMaker.setStats(copyOf(this.mirrorStateStore.getTopicsStats()
        .entrySet()
        .stream()
        .collect(Collectors.groupingBy(entry -> entry.getKey().getTopic(),
            Collectors.summingLong(entry -> entry.getValue().getCount())))));
    return mirrorMaker;
  }
}
