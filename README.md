
## How to run
```bash
mvn clean install

cp -rf target/kafka-consumer-offsets-3.2.1-package/* ~/confluent-3.2.1/

cd ~/confluent-3.2.1

bin/kafka-consumer-offsets-mirrorer --consumer.config etc/kafka-consumer-offsets/dev.mirrorer.consumer.properties --producer.config etc/kafka-consumer-offsets/local.mirrorer.producer.properties --from-beginning

bin/kafka-consumer-offsets-restorer --consumer.config etc/kafka-consumer-offsets/local.restorer.consumer.properties --from-beginning
```
## How to mirror topics and paritions
```bash
source_zk="10.132.4.10:22181,10.132.4.11:22181,10.132.4.12:22181"
target_zk="127.0.0.1:2181"

for topic in `kafka-topics --list --zookeeper $source_zk`; do
  if [[ $topic =~ ^[0-9a-zA-Z].*$ ]]; then
    partitions=`kafka-topics --describe --topic $topic --zookeeper $source_zk | grep PartitionCount | awk '{ split($2, a, ":"); print a[2] }'`
    echo "$topic - creating $partitions partitions"
    kafka-topics --create --zookeeper $target_zk --topic $topic --partitions $partitions --replication-factor 1
    curl -X POST -d "{\"topic\":\"$topic\", \"numPartitions\":\"$partitions\"}" http://localhost:9000/topics
  fi
done
```
