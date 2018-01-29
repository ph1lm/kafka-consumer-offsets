
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
export CLASSPATH=<CONFLUENT_HOME>/share/java/kafka-consumer-offsets/*

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

##### Mirror breaker working mode
```bash
-Dmirror-breaker-working-mode - mirror breaker working mode, default value is NORMAL
Available values: DAEMON, NORMAL
```
##### Example
```
./bin/kafka-run-class -Dmirror-breaker-working-mode=DAEMON kafka.tools.MirrorMaker \
 --consumer.config ./etc/kafka/consumer.properties \
 --producer.config ./etc/kafka/producer-m.properties \
 --message.handler io.confluent.consumer.offsets.PartitionsAwareMirrorMakerHandler \
 --whitelist ".*"
```

##### Console reporter period
```bash
-Dconsole-reporter-period-secs - default value is 60
Available values: DAEMON, NORMAL
```
##### Example
```
./bin/kafka-run-class --Dconsole-reporter-period-secs=20 kafka.tools.MirrorMaker \
 --consumer.config ./etc/kafka/consumer.properties \
 --producer.config ./etc/kafka/producer-m.properties \
 --message.handler io.confluent.consumer.offsets.PartitionsAwareMirrorMakerHandler \
 --whitelist ".*"
```

## Mirroring control
##### Progress tracking
```bash
GET http://hostname:[port]/mirror/maker - provides overall progress report per topic and current mirroring state
State can obtain two values
RUNNING - at least one message was received during the time period that equals to <idle-state-timeout-secs>
WAITING - no messages were received during the time period that equals to <idle-state-timeout-secs>
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/mirror/maker
```

###### Response
```bash
HTTP/1.1 200 OK
Date: Tue, 23 Jan 2018 11:19:40 GMT
Content-length: 187

{
  "content" : {
    "progress" : {
      "animals" : 1781522,
      "cities" : 875167,
      "tusers" : 2140138
    },
    "state" : "RUNNING",
    "total_count" : 4794799
  }
}
```

##### All Topic progress tracking
```bash
GET http://hostname:[port]/mirror/maker/topics - provides current message processing state for each topic/partition
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/mirror/maker/topics
```

###### Response
```bash
HTTP/1.1 200 OK
Date: Tue, 23 Jan 2018 11:22:01 GMT
Content-length: 1084

{
  "content" : {
    "topics" : {
      "animals:0" : {
        "offset" : 6039253,
        "count" : 1007517
      },
      "animals:1" : {
        "offset" : 6199661,
        "count" : 1032455
      },
      "animals:2" : {
        "offset" : 5405359,
        "count" : 899790
      },
      "animals:3" : {
        "offset" : 6355723,
        "count" : 1060238
      },
      "cities:0" : {
        "offset" : 3033293,
        "count" : 1010099
      },
      "cities:1" : {
        "offset" : 2937351,
        "count" : 978766
      },
      "cities:2" : {
        "offset" : 3032241,
        "count" : 1011350
      },
      "cities:3" : {
        "offset" : 2997111,
        "count" : 999785
      },
      "users:0" : {
        "offset" : 6072837,
        "count" : 1012299
      },
      "users:1" : {
        "offset" : 6375128,
        "count" : 1062227
      },
      "sers:2" : {
        "offset" : 5784641,
        "count" : 964929
      },
      "users:3" : {
        "offset" : 5767390,
        "count" : 960545
      }
    }
  }
}
```
##### Particular Topic progress tracking
```bash
GET http://hostname:[port]/mirror/maker/topics/{topic_name} - provides current message processing state for each topic/partition
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/mirror/maker/topics/animals
```

###### Response
```bash
HTTP/1.1 200 OK
Date: Tue, 23 Jan 2018 11:22:01 GMT
Content-length: 1084

{
  "content" : {
    "topics" : {
      "animals:0" : {
        "offset" : 6039253,
        "count" : 1007517
      },
      "animals:1" : {
        "offset" : 6199661,
        "count" : 1032455
      },
      "animals:2" : {
        "offset" : 5405359,
        "count" : 899790
      },
      "animals:3" : {
        "offset" : 6355723,
        "count" : 1060238
      }
    }
  }
}
```
##### Mirror Breaker tracking
```bash
GET http://hostname:[port]/mirror/breaker - controls application shutdown
```
```
DAEMON - The system will not be shutted down after an idle state timeout  
NORMAL - The system will be shutted down after an idle state timeout 
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/mirror/breaker
```

###### Response
```
HTTP/1.1 200 OK
Date: Tue, 23 Jan 2018 11:25:37 GMT
Content-length: 45

{
  "content" : {
    "mode" : "DAEMON"
  }
}
```

##### Mirror Breaker Mode changing
```bash
POST http://hostname:[port]/mirror/breaker - switches mode between DAEMON/NORMAL
```
##### Example

###### Request
```bash
curl -H "Content-Type: application/json" -X POST -d '{"mode":"DAEMON"}' http://localhost:3131/mirror/breaker
```

###### Response
```
{
  "content" : "Success"
}
```

##### Metrics
```bash
GET http://hostname:[port]/mirror/maker/meter
```
##### Example

###### Request
```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:3131/mirror/metrics
```

###### Response
```
HTTP/1.1 200 OK
Date: Thu, 25 Jan 2018 15:09:07 GMT
Content-length: 182

{
  "content" : {
    "count" : 2997371,
    "mean_rate" : 61699.65,
    "one_minute_rate" : 204497.5,
    "five_minute_rate" : 335071.71,
    "fifteen_minute_rate" : 363852.03
  }
}
```

##### Console reporter
```bash
POST http://hostname:[port]/mirror/metrics/console
```
##### Example

###### Enable Request
```bash
curl -H "Content-Type: application/json" -X POST -d '{"enabled":"true"}' http://localhost:3131/mirror/metrics/console

```
###### Response
```
HTTP/1.1 200 OK
Date: Thu, 25 Jan 2018 15:09:07 GMT
Content-length: 182

{
  "content" : "Success"
}
```
###### Disable Request
```bash
curl -H "Content-Type: application/json" -X POST -d '{"enabled":"false"}' http://localhost:3131/mirror/metrics/console

```
###### Response
```
HTTP/1.1 200 OK
Date: Thu, 25 Jan 2018 15:09:07 GMT
Content-length: 182

{
  "content" : "Success"
}
```

##### JMX reporter
```bash
POST http://hostname:[port]/mirror/metrics/jmx
```
##### Example

###### Enable Request
```bash
curl -H "Content-Type: application/json" -X POST -d '{"enabled":"true"}' http://localhost:3131/mirror/metrics/jmx

```
###### Response
```
HTTP/1.1 200 OK
Date: Thu, 25 Jan 2018 15:09:07 GMT
Content-length: 182

{
  "content" : "Success"
}
```
###### Disable Request
```bash
curl -H "Content-Type: application/json" -X POST -d '{"enabled":"false"}' http://localhost:3131/mirror/metrics/jmx

```
###### Response
```
HTTP/1.1 200 OK
Date: Thu, 25 Jan 2018 15:09:07 GMT
Content-length: 182

{
  "content" : "Success"
}
```

## LOGGING
###### Open file
```bash
<CONFLUENT_HOME>/etc/kafka/tools-log4j.properties
```
###### Add row
```bash
log4j.logger.io.confluent.consumer.offsets=DEBUG
```
