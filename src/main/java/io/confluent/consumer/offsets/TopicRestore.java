package io.confluent.consumer.offsets;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.utils.Bytes;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class TopicRestore {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, BytesSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class.getName());

    KafkaProducer<Bytes, Bytes> producer = new KafkaProducer<>(properties);

    final String topic = "gv-edge-svc-BaseCompanyLastXIDState-changelog";
    final String fileName = String.format("%s.dat", topic);

    int messagesCount=0;
    try (DataInputStream din = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)))) {
      while (true) {
        byte[] key = readChunk(din);
        byte[] value = readChunk(din);

        producer.send(new ProducerRecord<>(topic, Bytes.wrap(key), Bytes.wrap(value))).get();
        System.out.println(new String(key) + " - " + new String(value));
        messagesCount++;
      }
    } catch (EOFException ignore) {
    } finally {
      System.out.println(String.format("%d messages produced to %s", messagesCount, topic));
    }
  }

  private static byte[] readChunk(DataInputStream din) throws IOException {
    int chunkLength = din.readInt();
    if (chunkLength < 0) {
      throw new EOFException();
    }
    byte[] chunk = new byte[chunkLength];
    if (chunkLength != din.read(chunk, 0, chunkLength)) {
      throw new RuntimeException("bad file");
    }
    return chunk;
  }
}
