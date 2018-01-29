package io.confluent.consumer.offsets.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.confluent.consumer.offsets.web.common.JsonSerializer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class BaseHttpHandler implements HttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(BaseHttpHandler.class);
  private final HandlerEndPoint endPoint;

  public BaseHttpHandler(HandlerEndPoint handlerEndPoint) {
    this.endPoint = handlerEndPoint;
  }

  public String context() {
    return this.endPoint.context();
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    final OutputStream outputStream = httpExchange.getResponseBody();
    final Response response = processRequest(httpExchange);
    final String responseBody = JsonSerializer.OBJECT_MAPPER.getInstance()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(response.getBody());
    httpExchange.sendResponseHeaders(response.getStatus(), responseBody.length());
    outputStream.write(responseBody.getBytes());
    outputStream.close();
  }

  private Response processRequest(HttpExchange httpExchange) {
    final Response response = new Response();
    try {
      response.setBody(ResponseWrapper
          .create(pushToEndPoint(httpExchange)));
      response.setStatus(HttpURLConnection.HTTP_OK);

    } catch (IllegalArgumentException e) {
      LOG.error(e.getMessage(), e);
      response.setBody(ResponseWrapper.create(e.getMessage()));
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      response.setBody(ResponseWrapper.create(e.getMessage()));
      response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
    return response;
  }

  private Object pushToEndPoint(HttpExchange httpExchange) {
    switch (httpExchange.getRequestMethod()) {
      case HandlerEndPoint.GET_REQUEST_METHOD:
        return this.endPoint.processGetRequest(httpExchange);
      case HandlerEndPoint.POST_REQUEST_METHOD:
        return this.endPoint.processPostRequest(httpExchange);
      default:
        throw new IllegalArgumentException(HandlerEndPoint.NOT_SUPPORTED_REQUEST_METHOD);
    }
  }

  @Getter
  @Setter
  class Response {
    private int status;
    private Object body;

    public Object getBody() {
      return this.body;
    }
  }
}
