package io.confluent.consumer.offsets;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Properties;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer;
import kafka.common.MessageFormatter;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;

public class AvroMessageFormatterTimestamped extends AbstractKafkaAvroDeserializer implements MessageFormatter {
  private final EncoderFactory encoderFactory = EncoderFactory.get();
  private boolean printKey = false;
  private boolean printTimestamp = false;
  private byte[] keySeparator = "\t".getBytes();
  private byte[] timestampSeparator = "\t".getBytes();
  private byte[] lineSeparator = "\n".getBytes();

  public AvroMessageFormatterTimestamped() {
  }

  AvroMessageFormatterTimestamped(SchemaRegistryClient schemaRegistryClient, boolean printKey) {
    this.schemaRegistry = schemaRegistryClient;
    this.printKey = printKey;
  }

  public void init(Properties props) {
    if (props == null) {
      throw new ConfigException("Missing schema registry url!");
    } else {
      String url = props.getProperty("schema.registry.url");
      if (url == null) {
        throw new ConfigException("Missing schema registry url!");
      } else {
        this.schemaRegistry = new CachedSchemaRegistryClient(url, 1000);
        if (props.containsKey("print.key")) {
          this.printKey = props.getProperty("print.key").trim().toLowerCase().equals("true");
        }

        if (props.containsKey("print.timestamp")) {
          this.printTimestamp = props.getProperty("print.timestamp").trim().toLowerCase().equals("true");
        }

        if (props.containsKey("key.separator")) {
          this.keySeparator = props.getProperty("key.separator").getBytes();
        }

        if (props.containsKey("timestamp.separator")) {
          this.keySeparator = props.getProperty("timestamp.separator").getBytes();
        }

        if (props.containsKey("line.separator")) {
          this.lineSeparator = props.getProperty("line.separator").getBytes();
        }

      }
    }
  }

  public void writeTo(ConsumerRecord<byte[], byte[]> consumerRecord, PrintStream output) {
    if (this.printKey) {
      try {
        this.writeTo((byte[]) consumerRecord.key(), output);
        output.write(this.keySeparator);
      } catch (IOException var5) {
        throw new SerializationException("Error while formatting the key", var5);
      }
    }

    if (this.printTimestamp) {
      try {
        byte[] timestamp = Instant.ofEpochMilli(consumerRecord.timestamp()).toString().getBytes();
        output.write(timestamp);
        output.write(this.timestampSeparator);
      } catch (IOException var5) {
        throw new SerializationException("Error while formatting the key", var5);
      }
    }

    try {
      this.writeTo((byte[]) consumerRecord.value(), output);
      output.write(this.lineSeparator);
    } catch (IOException var4) {
      throw new SerializationException("Error while formatting the value", var4);
    }
  }

  private void writeTo(byte[] data, PrintStream output) throws IOException {
    Object object = this.deserialize(data);
    Schema schema = this.getSchema(object);

    try {
      JsonEncoder encoder = this.encoderFactory.jsonEncoder(schema, output);
      DatumWriter<Object> writer = new GenericDatumWriter(schema);
      writer.write(object, encoder);
      encoder.flush();
    } catch (AvroRuntimeException var7) {
      throw new SerializationException(String.format("Error serializing Avro data of schema %s to json", schema), var7);
    }
  }

  public void close() {
  }
}
