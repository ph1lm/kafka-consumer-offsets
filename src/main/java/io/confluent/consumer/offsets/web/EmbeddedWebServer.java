package io.confluent.consumer.offsets.web;

import com.sun.net.httpserver.HttpServer;
import io.confluent.consumer.offsets.PartitionsAwareMirrorMakerHandler;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EmbeddedWebServer {
  private static final Logger LOG = LoggerFactory.getLogger(PartitionsAwareMirrorMakerHandler.class);
  private static final String BASE_ENDPOINT_PACKAGE = "io.confluent.consumer.offsets.web.endpoint";
  private final HttpServer server;

  public EmbeddedWebServer() {
    try {
      LOG.info("Server initialization...");
      final String socketAddress = System.getProperty("socket-address", "3131");
      this.server = HttpServer.create(new InetSocketAddress(Integer.parseInt(socketAddress)), 0);
      getEndPoints()
          .forEach(handler -> this.server.createContext("/" + handler.context(), handler));
      this.server.setExecutor(null);
      LOG.info("Server initialized");
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  public void start() {
    LOG.info("Server starting...");
    this.server.start();
    LOG.debug("Server started on {}", this.server.getAddress().toString());
  }

  public List<BaseHttpHandler> getEndPoints() {
    final Reflections reflections = new Reflections(BASE_ENDPOINT_PACKAGE);
    final Set<Class<? extends HandlerEndPoint>> endPoints = reflections.getSubTypesOf(HandlerEndPoint.class);
    return endPoints
        .stream()
        .map(this::buildEndpoint)
        .map(BaseHttpHandler::new)
        .collect(Collectors.toList());
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
