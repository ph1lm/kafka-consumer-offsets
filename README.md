
## How to install
```bash
mvn clean install
cp -rf target/kafka-consumer-offsets-3.2.1-package/* ~/confluent-3.2.1/
```
If you installed from deb or rpm packages, the contents are installed globally and you’ll need to adjust the paths used below:
```
/usr/bin/                  # Confluent CLI and individual driver scripts for starting/stopping services, prefixed with <package> names
/etc/<package>/            # Configuration files
/usr/share/java/<package>/ # Jars
```

## How to run
```
kafka-consumer-offsets-mirrorer --consumer.config etc/kafka-consumer-offsets/dev.mirrorer.consumer.properties --producer.config etc/kafka-consumer-offsets/local.mirrorer.producer.properties --from-beginning

kafka-consumer-offsets-restorer --consumer.config etc/kafka-consumer-offsets/local.restorer.consumer.properties --from-beginning
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
  fi
done
```
## Mirroring options
##### Idle state timeout
```bash
-Didle-state-timeout-secs - time since the last processed message after which the mirroring will be shut downed, default value is 300 seconds
```
##### Example
```bash
./bin/kafka-run-class -Didle-state-timeout-secs=60 kafka.tools.MirrorMaker \
 --consumer.config ./etc/kafka/consumer.properties \
 --producer.config ./etc/kafka/producer-m.properties \
 --message.handler io.confluent.consumer.offsets.PartitionsAwareMirrorMakerHandler \
 --whitelist ".*"
```
##### Socket address
```bash
-Dsocket-address - webserver socket address, default value is 3131
```
##### Example
```
./bin/kafka-run-class -Dsocket-address=3131 kafka.tools.MirrorMaker \
 --consumer.config ./etc/kafka/consumer.properties \
 --producer.config ./etc/kafka/producer-m.properties \
 --message.handler io.confluent.consumer.offsets.PartitionsAwareMirrorMakerHandler \
 --whitelist ".*"
```
## Mirroring control
##### Progress tracking
```bash
GET http://hostname:[port]/progress - provides detailed progress report per topic/partition
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/progress
```

###### Response
```bash
HTTP/1.1 200 OK
Date: Fri, 12 Jan 2018 10:11:58 GMT
Content-length: 1793

{
  "content" : {
    "ProgressKey(topic=cities, partition=2)" : {
      "offset" : 3327901,
      "count" : 47593
    },
    "ProgressKey(topic=cities, partition=3)" : {
      "offset" : 2974566,
      "count" : 47573
    },
    "ProgressKey(topic=cities, partition=0)" : {
      "offset" : 3272839,
      "count" : 47647
    }
  }
}
```

##### State tracking
```bash
GET http://hostname:[port]/state - provides current message processing state
```
```
RUNNING - at least one message was received during the time period that equals to <idle-state-timeout-secs>
WAITING - no messages were received during the time period that equals to <idle-state-timeout-secs> 
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/state
```

###### Response
```bash
HTTP/1.1 200 OK
Date: Fri, 12 Jan 2018 10:21:55 GMT
Content-length: 27

{
  "content" : "WAITING"
}
```

##### Mode tracking
```bash
GET http://hostname:[port]/mode - provides current message processing state
```
```
DAEMON - The system will not be shutted down after an idle state timeout  
WAITING - The system will be shutted down after an idle state timeout 
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/mode
```

###### Response
```
HTTP/1.1 200 OK
Date: Fri, 12 Jan 2018 10:26:26 GMT
Content-length: 26
 
{
  "content" : "DAEMON"
}
```

##### Mode changing
```bash
POST http://hostname:[port]/mode - switches mode between DAEMON/NORMAL
```
##### Example

###### Request
```bash
curl -H "Content-Type: application/json" -X POST -d '{"mode":"DAEMON"}' http://localhost:3131/mode
```

###### Response
```
{
  "content" : "Mode was switched to DAEMON"
}
```
