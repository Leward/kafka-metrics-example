package eu.leward.kafka;

import eu.leward.kafka.metrics.avro.AvroMetric;
import eu.leward.kafka.metrics.serialization.SpecificAvroDeserializer;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.kstream.TimeWindowedDeserializer;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfiguration {

    private final KafkaProperties properties;

    public KafkaConsumerConfiguration(KafkaProperties properties) {
        this.properties = properties;
    }

    @Bean
    SchemaRegistryClient schemaRegistryClient() {
        String schemaRegistryUrl = properties.getProperties().get("schema.registry.url");
        if (schemaRegistryUrl == null) {
            throw new NullPointerException("schema.registry.url property is not defined");
        }
        return new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
    }

    @Bean
    ConsumerFactory<Windowed<String>, AvroMetric> consumerFactory() {
        Map<String, Object> consumerProperties = properties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new TimeWindowedDeserializer<>(new StringDeserializer()),
                new SpecificAvroDeserializer<>(schemaRegistryClient(), consumerProperties)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Windowed<String>, AvroMetric> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Windowed<String>, AvroMetric> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
