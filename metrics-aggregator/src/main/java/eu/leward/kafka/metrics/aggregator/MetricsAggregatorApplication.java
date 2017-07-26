package eu.leward.kafka.metrics.aggregator;

import eu.leward.kafka.metrics.avro.AvroAvgMetric;
import eu.leward.kafka.metrics.avro.AvroMetric;
import eu.leward.kafka.metrics.serialization.AvroMetricSerde;
import eu.leward.kafka.metrics.serialization.SpecificAvroSerde;
import eu.leward.kafka.metrics.serialization.WindowedSerde;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@EnableKafkaStreams
@SpringBootApplication
public class MetricsAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricsAggregatorApplication.class, args);
    }

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url:http://localhost:8081}")
    private String schemaRegistryUrl;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    StreamsConfig streamsConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationName);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, AvroMetricSerde.class);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new StreamsConfig(props);
    }

    @Bean
    SchemaRegistryClient schemaRegistryClient() {
        return new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
    }

    @Bean
    KStream<String, AvroMetric> metricsStream(KStreamBuilder kStreamBuilder) {
        return kStreamBuilder.stream("metrics");
    }

    @Bean
    SpecificAvroSerde<AvroAvgMetric> avroAvgMetricerde(SchemaRegistryClient schemaRegistryClient) {
        Map<String, Object> props = new HashMap<>();
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put("specific.avro.reader", true);
        return new SpecificAvroSerde<AvroAvgMetric>(schemaRegistryClient, props);
    }

    @Bean
    AvroMetricSerde avroMetricSerde(SchemaRegistryClient schemaRegistryClient) {
        Map<String, Object> props = new HashMap<>();
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put("specific.avro.reader", true);
        return new AvroMetricSerde(schemaRegistryClient, props);
    }

    @Bean
    KTable<Windowed<String>, AvroMetric> avgMetrics(KStream<String, AvroMetric> metricsStream,
                                                    SpecificAvroSerde<AvroAvgMetric> avroAvgMetricerde,
                                                    AvroMetricSerde avroMetricSerde) {
        long windowSizeMs = TimeUnit.MINUTES.toMillis(5);
        long advanceMs = TimeUnit.MINUTES.toMillis(1);
        TimeWindows hoppingWindows = TimeWindows.of(windowSizeMs).advanceBy(advanceMs);

        KTable<Windowed<String>, AvroMetric> avgMetrics = metricsStream
                .groupByKey()
                .aggregate(
                        () -> new AvgMetric().toAvroAvgMetric(),
                        (key, value, aggregate) -> new AvgMetric(aggregate).addMetric(value).toAvroAvgMetric(),
                        hoppingWindows,
                        avroAvgMetricerde,
                        "avg_metrics"
                )
                .mapValues(value -> new AvgMetric(value).toMetric().toAvro());


        avgMetrics.to(new WindowedSerde<String>(Serdes.String()), avroMetricSerde, "avg_metrics2");
        avgMetrics.foreach((key, value) -> System.out.printf("Window:  %d -> %d\n", key.window().start(), key.window().end()));

        return avgMetrics;
    }

}
