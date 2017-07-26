package eu.leward.kafka;

import eu.leward.kafka.metrics.Metric;
import org.apache.kafka.streams.kstream.Window;

import java.util.*;

public class Statistics {

    private Map<String, SortedMap<Window, Metric>> data = new HashMap<>();

    public void add(String host, Window window, Metric metric) {
        SortedMap<Window, Metric> metricsForHost;
        if(!data.containsKey(host)) {
            metricsForHost = new TreeMap<>(Comparator.comparingLong(Window::start));
            data.put(host, metricsForHost);
        } else {
            metricsForHost = data.get(host);
        }
        metricsForHost.put(window, metric);
    }

    public Map<String, SortedMap<Window, Metric>> getData() {
        return data;
    }
}
