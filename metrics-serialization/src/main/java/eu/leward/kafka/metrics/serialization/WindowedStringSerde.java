package eu.leward.kafka.metrics.serialization;

import org.apache.kafka.common.serialization.Serde;

public class WindowedStringSerde extends WindowedSerde<String> {
    public WindowedStringSerde(Serde<String> serde) {
        super(serde);
    }
}
