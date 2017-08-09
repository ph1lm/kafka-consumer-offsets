package io.confluent.consumer.offsets.processor;

import kafka.metrics.KafkaMetricsReporter;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Time;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import scala.Option$;
import scala.collection.immutable.List$;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

public class KafkaTestBase {

  private static final String KAFKA_LOG_DIR = "embeddedKafka";

  protected static final String GROUP = "localTestGroup";

  protected static final int POLL_TIMEOUT = 1000;

  private static KafkaServer kafkaServer;
  private static TestingServer zookeeperServer;

  protected static Properties consumerProps;
  protected static Properties producerProps;

  @BeforeClass
  public static void testSetUp() throws Exception {
    Integer zookeeperPort = findRandomOpenPortOnAllLocalInterfaces();
    zookeeperServer = new TestingServer(zookeeperPort);
    zookeeperServer.start();

    String kafkaLogDir = System.getProperty("java.io.tmpdir") + File.separator + KAFKA_LOG_DIR;
    deleteDirectory(kafkaLogDir);

    Integer kafkaPort = findRandomOpenPortOnAllLocalInterfaces();
    Properties props = new Properties();
    props.setProperty("hostname", "localhost");
    props.setProperty("port", Integer.toString(kafkaPort));
    props.setProperty("brokerid", "0");
    props.setProperty("log.dir", kafkaLogDir);
    props.setProperty("zookeeper.connect", "localhost:" + zookeeperPort);

    kafkaServer = new KafkaServer(new KafkaConfig(props), Time.SYSTEM,
        Option$.MODULE$.<String>empty(), List$.MODULE$.<KafkaMetricsReporter>empty());
    kafkaServer.startup();

    consumerProps = new Properties();
    consumerProps.setProperty("bootstrap.servers", "localhost:" + kafkaPort);
    consumerProps.setProperty("group.id", GROUP);
    consumerProps.setProperty("enable.auto.commit", "false");
    consumerProps.setProperty("key.deserializer", StringDeserializer.class.getName());
    consumerProps.setProperty("value.deserializer", StringDeserializer.class.getName());

    producerProps = new Properties();
    producerProps.setProperty("bootstrap.servers", "localhost:" + kafkaPort);
    producerProps.setProperty("key.serializer", StringSerializer.class.getName());
    producerProps.setProperty("value.serializer", StringSerializer.class.getName());
  }

  @AfterClass
  public static void testTearDown() throws Exception {
    kafkaServer.shutdown();
    zookeeperServer.stop();
  }

  private static Integer findRandomOpenPortOnAllLocalInterfaces() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private static void deleteDirectory(String dir) throws IOException {
    Path directory = Paths.get(dir);
    if (!directory.toFile().exists()) {
      return;
    }
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  protected static <K, V> ConsumerRecord<K, V> findRecord(ConsumerRecords<K, V> records, V value) {
    for (ConsumerRecord<K, V> record : records) {
      if (value.equals(record.value())) {
        return record;
      }
    }
    return null;
  }
}
