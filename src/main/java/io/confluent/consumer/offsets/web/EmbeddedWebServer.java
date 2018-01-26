package io.confluent.consumer.offsets.web;

import com.sun.net.httpserver.HttpServer;
import io.confluent.consumer.offsets.PartitionsAwareMirrorMakerHandler;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

@Setter
public class EmbeddedWebServer {
  private static final Logger LOG = LoggerFactory.getLogger(PartitionsAwareMirrorMakerHandler.class);

  private int socketAddress;
  private List<BaseHttpHandler> endPoints;

  public void registerEndPoints(List<BaseHttpHandler> endPoints) {
    this.endPoints = endPoints;
  }

  public void start() {
    try {
      LOG.info("Server initialization...");
      final HttpServer server = HttpServer.create(new InetSocketAddress(this.socketAddress), 0);
      this.endPoints
          .forEach(handler -> server
              .createContext("/" + handler.context(), handler));
      server.setExecutor(null);
      LOG.info("Server initialized");
      LOG.info("Server starting...");
      server.start();
      LOG.debug("Server started on {}", server.getAddress().toString());
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }
}
