package io.confluent.consumer.offsets.web;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ResponseWrapper<T> {
  private T content;

  private ResponseWrapper(T content) {
    this.content = content;
  }

  public static <T> ResponseWrapper create(T content) {
    return new ResponseWrapper(content);
  }
}
