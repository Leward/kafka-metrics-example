## Start Confluent Kafka

Download: 
```bash
wget http://packages.confluent.io/archive/3.2/confluent-oss-3.2.1-2.11.tar.gz && /
  tar -xzf confluent-oss-3.2.1-2.11.tar.gz
```

In different terminal windows run the following commands to start the different required components: 

```bash
$ ./bin/zookeeper-server-start ./etc/kafka/zookeeper.properties
$ ./bin/kafka-server-start ./etc/kafka/server.properties
$ ./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties
```

More details about the Setup on the [Confluent Documentation - Quickstart](http://docs.confluent.io/current/quickstart.html#quickstart)


## Generate metrics

You can build the project and run the program in one go:

```bash
$ mvn clean package && java -jar metrics-producer/target/metrics-producer-1.0-SNAPSHOT.jar host1 host2 host3 host4 host5 host6 host7 host8 host9
```

Provided as arguments the hostnames for which we want to generate metrics so that we have so data to process later on.

You should then see in the logs something like:

```
...
2017-05-11 20:10:14.984  INFO 14220 --- [           main] e.l.k.m.p.MetricsProducerApplication     : Sending metrics about host5 to Kafka Metric{cpuUsage=0.0, memoryUsage=0.39001772, diskUsage=0.8403008}
2017-05-11 20:10:14.984  INFO 14220 --- [           main] e.l.k.m.p.MetricsProducerApplication     : Sending metrics about host6 to Kafka Metric{cpuUsage=0.34, memoryUsage=0.5301579, diskUsage=0.8651164}
2017-05-11 20:10:15.085  INFO 14220 --- [           main] e.l.k.m.p.MetricsProducerApplication     : Sending metrics about host3 to Kafka Metric{cpuUsage=1.0, memoryUsage=0.92406934, diskUsage=0.49175727}
2017-05-11 20:10:15.085  INFO 14220 --- [           main] e.l.k.m.p.MetricsProducerApplication     : Sending metrics about host5 to Kafka Metric{cpuUsage=0.0, memoryUsage=0.35001773, diskUsage=0.8403008}
2017-05-11 20:10:15.085  INFO 14220 --- [           main] e.l.k.m.p.MetricsProducerApplication     : Sending metrics about host8 to Kafka Metric{cpuUsage=1.0, memoryUsage=0.92707837, diskUsage=0.016572356}
2017-05-11 20:10:15.085  INFO 14220 --- [           main] e.l.k.m.p.MetricsProducerApplication     : Sending metrics about host9 to Kafka Metric{cpuUsage=0.22, memoryUsage=0.66362697, diskUsage=0.6916841}
...
```

## Read the produced messages fron command line

Use `kafka-avro-console-consumer` as messages have been serialized using Avro.

```bash
$ bin/kafka-avro-console-consumer --bootstrap-server=localhost:9092 --topic=metrics
```

## Run Kafka Stream processing
