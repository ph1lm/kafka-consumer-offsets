package io.confluent.consumer.offsets.web.common;

import io.confluent.consumer.offsets.mirror.infrastructure.MirrorHandlerConstants;
import io.confluent.consumer.offsets.web.BaseHttpHandler;
import io.confluent.consumer.offsets.web.HandlerEndPoint;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EndPointsBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(EndPointsBuilder.class);

  public List<BaseHttpHandler> build() {
    final Set<Class<? extends HandlerEndPoint>> endPoints = fetchExistedClasses();
    return endPoints
        .stream()
        .map(this::buildEndpoint)
        .map(BaseHttpHandler::new)
        .collect(Collectors.toList());
  }

  private Set<Class<? extends HandlerEndPoint>> fetchExistedClasses() {
    final Reflections reflections = new Reflections(MirrorHandlerConstants.BASE_ENDPOINT_PACKAGE);
    return reflections.getSubTypesOf(HandlerEndPoint.class);
  }

  private HandlerEndPoint buildEndpoint(Class<? extends HandlerEndPoint> clazz) {
    try {
      final HandlerEndPoint endPoint = clazz.newInstance();
      LOG.info("Endpoint was build {}", endPoint.context());
      return endPoint;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
