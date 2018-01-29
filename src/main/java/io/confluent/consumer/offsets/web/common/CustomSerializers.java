package io.confluent.consumer.offsets.web.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.confluent.consumer.offsets.web.model.TopicStatDto;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class CustomSerializers {
  private static final String FIELD_NAME_OFFSET = "offset";
  private static final String FIELD_NAME_COUNT = "count";
  private static final ThreadLocal<DecimalFormat> DOUBLE_FORMATTER = ThreadLocal.withInitial(() -> new DecimalFormat(".##"));

  private CustomSerializers() {
  }

  public static class TopicSerializer extends JsonSerializer<List<TopicStatDto>> {
    @Override
    public void serialize(List<TopicStatDto> values, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      values.stream().forEach(value -> {
        try {
          gen.writeFieldName(new StringBuilder()
              .append(value.getName())
              .append(":")
              .append(value.getPartition())
              .toString());
          gen.writeStartObject();
          gen.writeNumberField(FIELD_NAME_OFFSET, value.getOffset());
          gen.writeNumberField(FIELD_NAME_COUNT, value.getCount());
          gen.writeEndObject();
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      });
      gen.writeEndObject();
    }
  }

  public static class DoubleSerializer extends JsonSerializer<Double> {
    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeNumber(DOUBLE_FORMATTER.get().format(value));
    }
  }
}
