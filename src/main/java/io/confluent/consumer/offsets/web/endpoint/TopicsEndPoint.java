package io.confluent.consumer.offsets.web.endpoint;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sun.net.httpserver.HttpExchange;
import io.confluent.consumer.offsets.mirror.entity.TopicStats;
import io.confluent.consumer.offsets.mirror.service.MirrorMakerService;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import io.confluent.consumer.offsets.web.common.CustomSerializers;
import io.confluent.consumer.offsets.web.model.TopicStatDto;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TopicsEndPoint implements HandlerEndPoint {

  private static final String ENDPOINT_CONTEXT = "mirror/maker/topics";
  private static final String PATH_SEGMENT_TOPICS = "topics";


  @Override
  public String context() {
    return ENDPOINT_CONTEXT;
  }

  @Override
  public Object processGetRequest(HttpExchange parameters) {

    final TopicsProgress topicsProgress = new TopicsProgress();
    topicsProgress.setTopics(Optional.ofNullable(getTopics(parameters.getRequestURI().getPath()))
        .orElse(Collections.emptyList())
        .stream()
        .map(TopicStatDto::new)
        .collect(Collectors.toList()));

    return topicsProgress;
  }

  private List<TopicStats> getTopics(String path) {
    String[] segments = path.split("/");
    String lastSegment = segments[segments.length - 1];

    if (PATH_SEGMENT_TOPICS.equals(lastSegment))
      return MirrorMakerService.getInstance().getTopicStats();
    else
      return MirrorMakerService.getInstance().getTopicStats(lastSegment);
  }

  @Data
  public static class TopicsProgress {
    @JsonSerialize(using = CustomSerializers.TopicSerializer.class)
    private List<TopicStatDto> topics;
  }
}
