package eu.leward.kafka.test;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class MyConsumer {

    public static void main(String[] args) {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        String topic = "testing";
        consumer.subscribe(Collections.singleton(topic));
        consumer.poll(0);
        consumer.seekToBeginning(Collections.emptyList());
        consumer.subscribe(Collections.singletonList(topic), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // Stuff to commit my offset
                consumer.commitSync();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                //
            }
        });
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            records.forEach(MyConsumer::printRecord);
        }

    }

    private static PrintStream printRecord(ConsumerRecord<String, String> record) {
        return System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(),
                record.value());
    }

}
