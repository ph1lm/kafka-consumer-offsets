package io.confluent.consumer.offsets.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetricsReporterDto {
  private boolean enabled;
}
