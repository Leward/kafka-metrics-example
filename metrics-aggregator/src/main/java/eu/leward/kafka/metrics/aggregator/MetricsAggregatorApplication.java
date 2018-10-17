package eu.leward.kafka.metrics.aggregator;

import eu.leward.kafka.metrics.avro.AvroAvgMetric;
import eu.leward.kafka.metrics.avro.AvroMetric;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;

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
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new StreamsConfig(props);
    }

    @Bean
    SchemaRegistryClient schemaRegistryClient() {
        return new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
    }

    @Bean
    KStream<String, AvroMetric> metricsStream(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream("metrics");
    }

    @Bean
    SpecificAvroSerde<AvroAvgMetric> avroAvgMetricSerde(SchemaRegistryClient schemaRegistryClient) {
        return new SpecificAvroSerde<>(schemaRegistryClient);
    }

    @Bean
    KTable<Windowed<String>, AvroAvgMetric> avgMetrics(KStream<String, AvroMetric> metricsStream,
                                                       SpecificAvroSerde<AvroAvgMetric> avroAvgMetricSerde) {
        long windowSizeMs = TimeUnit.MINUTES.toMillis(5);
        long advanceMs = TimeUnit.MINUTES.toMillis(1);
        TimeWindows hoppingWindows = TimeWindows.of(windowSizeMs).advanceBy(advanceMs);


        // Do the aggregation of metrics
        KTable<Windowed<String>, AvroAvgMetric> avgMetrics = metricsStream.groupByKey()
                .windowedBy(hoppingWindows)
                .aggregate(
                        () -> new AvgMetric().toAvroAvgMetric(),
                        (key, value, aggregate) -> new AvgMetric(aggregate).addMetric(value).toAvroAvgMetric(),
                        Materialized.as("avg_metrics")
                );

        avgMetrics.mapValues(value -> new AvgMetric(value).toMetric().toAvro())
                .toStream()
                .through("avg_metrics2", Produced.keySerde(WindowedSerdes.timeWindowedSerdeFrom(String.class)))
                .foreach((key, value) -> System.out.printf("Window:  %d -> %d = %s\n", key.window().start(), key.window().end(), value));

        // Make a simple view for consumers (show metric instead of avg metric)
//        avgMetrics.mapValues(value -> new AvgMetric(value).toMetric().toAvro())
//                .toStream()
//                .through("avg_metrics2")
//                .foreach((key, value) -> System.out.printf("Window:  %d -> %d\n", key.window().start(), key.window().end()));

        return avgMetrics;
    }

}
