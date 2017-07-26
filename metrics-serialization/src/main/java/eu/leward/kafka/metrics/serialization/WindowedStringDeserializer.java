package eu.leward.kafka.metrics.serialization;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer;

public class WindowedStringDeserializer extends WindowedDeserializer<String> {
    public WindowedStringDeserializer() {
        super(new StringDeserializer());
    }
}