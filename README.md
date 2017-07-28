```bash
mvn clean install

cp -rf target/kafka-consumer-offsets-3.2.1-package/* ~/confluent-3.2.1/

cd ~/confluent-3.2.1

bin/kafka-consumer-offsets-mirrorer --consumer.config etc/kafka-consumer-offsets/dev.mirrorer.consumer.properties --producer.config etc/kafka-consumer-offsets/local.mirrorer.producer.properties --from-beginning

bin/kafka-consumer-offsets-restorer --consumer.config etc/kafka-consumer-offsets/local.restorer.consumer.properties --from-beginning
```
