package io.confluent.consumer.offsets.web;

import com.sun.net.httpserver.HttpExchange;

public interface HandlerEndPoint {

  String POST_REQUEST_METHOD = "POST";
  String GET_REQUEST_METHOD = "GET";
  String NOT_SUPPORTED_REQUEST_METHOD = "Not supported request method";

  String context();

  default Object processGetRequest(HttpExchange parameters) {
    throw new IllegalArgumentException(NOT_SUPPORTED_REQUEST_METHOD);
  }

  default Object processPostRequest(HttpExchange body) {
    throw new IllegalArgumentException(NOT_SUPPORTED_REQUEST_METHOD);
  }
}
