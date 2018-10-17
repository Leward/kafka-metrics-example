package eu.leward.kafka;

import eu.leward.kafka.metrics.Metric;
import eu.leward.kafka.metrics.avro.AvroMetric;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.kstream.Window;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@SpringBootApplication
@RestController
public class MetricsAggregateReaderApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetricsAggregateReaderApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MetricsAggregateReaderApplication.class, args);
	}

	private Statistics statistics = new Statistics();

	@KafkaListener(topics = "avg_metrics2")
	public void listen(ConsumerRecord<Windowed<String>, AvroMetric> record) throws Exception {
		String host = record.key().key();
		Window window = record.key().window();
		Metric metric = new Metric(record.value());
		statistics.add(host, window, metric);
	}

	@GetMapping("/values")
	public Map<Long, Metric> show() {
		Map<String, SortedMap<Window, Metric>> data = statistics.getData();
		String host = "localhost";
		if(!data.containsKey(host)) {
			return new HashMap<>();
		}
		Map<Long, Metric> responseData = new TreeMap<>();
		data.get(host).forEach((window, metric) -> responseData.put(window.start(), metric));
		return responseData;
	}
}
