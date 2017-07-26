package eu.leward.kafka.metrics.serialization;

import eu.leward.kafka.metrics.avro.AvroMetric;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;

import java.util.Map;

public class AvroMetricSerde extends SpecificAvroSerde<AvroMetric> {
    public AvroMetricSerde() {
        super();
    }

    public AvroMetricSerde(SchemaRegistryClient client) {
        super(client);
    }

    public AvroMetricSerde(SchemaRegistryClient client, Map<String, ?> props) {
        super(client, props);
    }
}
