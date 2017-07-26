package eu.leward.kafka.metrics.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import eu.leward.kafka.metrics.Metric;

import java.util.*;

@SpringBootApplication
public class MetricsProducerApplication implements CommandLineRunner {

	private static Logger LOGGER = LoggerFactory.getLogger(MetricsProducerApplication.class);
	private final KafkaTemplate<Object, Object> template;

	@Autowired
	public MetricsProducerApplication(KafkaTemplate<Object, Object> template) {
		this.template = template;
	}

	public static void main(String[] args) {
		SpringApplication.run(MetricsProducerApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		if(strings == null || strings.length == 0) {
			strings = new String[] { "localhost" };
		}
		Map<String, Metric> metrics = new HashMap<>();
		while(true) {
			for(String hostName : strings) {
				if(isRandomlyTrue()) {
					continue;
				}
				Metric metric;
				if(!metrics.containsKey(hostName)) {
					metric = Metric.randomMetric();
				} else {
					metric = metrics.get(hostName);
				}
				template.send("metrics", hostName, metric.toAvro());
				LOGGER.info("Sending metrics about {} to Kafka {}", hostName, metric.toString());
				metrics.put(hostName, metric.generateNextRandomMetric());
			}
			Thread.sleep(100);
		}
	}

	private boolean isRandomlyTrue() {
		return Math.random() < 0.5;
	}
}
