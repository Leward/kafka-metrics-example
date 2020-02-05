## Start Confluent Kafka

Download: 
```bash
wget https://packages.confluent.io/archive/5.0/confluent-5.0.0-2.11.tar.gz && /
  tar -xzf confluent-5.0.0-2.11.tar.gz
```

Start the Confluent platform: 
```bash
$ ./bin/confluent start
```

More details about the Setup on the [Confluent Documentation - Quickstart](http://docs.confluent.io/current/quickstart.html#quickstart)


## Generate metrics

You can build the project and run the program in one go:

```bash
$ mvn clean package && java -jar metrics-producer/target/metrics-producer-1.0-SNAPSHOT.jar localhost host1 host2 host3 host4 host5 host6 host7 host8 host9
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

### Read the produced messages from command line

Use `kafka-avro-console-consumer` as messages have been serialized using Avro.

```bash
$ bin/kafka-avro-console-consumer --bootstrap-server=localhost:9092 --topic=metrics
```

## Run Kafka Stream processing

```bash
$ java -jar metrics-aggregator/target/metrics-aggregator-1.0-SNAPSHOT.jar
```

### View result of the Stream processing from command line

```bash
$ bin/kafka-avro-console-consumer --bootstrap-server=localhost:9092 --topic=avg_metrics2
```

## Access aggregated metrics from the dashboard

Start the web app:

```bash
$ java -jar metrics-aggregate-reader/target/metrics-aggregate-reader-1.0-SNAPSHOT.jar
```

Then browse: http://localhost:8080/ 

![Screenshot of the metrics dashboard](Kafka%20Metrics%20Example.png)

The dashboard reads the values for `localhost` only (this is hardcoded), that's why in the first example 
it is important to generate the metrics for `localhost`. 
