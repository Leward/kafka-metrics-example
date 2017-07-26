package eu.leward.kafka.metrics.aggregator;

import eu.leward.kafka.metrics.Metric;
import eu.leward.kafka.metrics.avro.AvroAvgMetric;
import eu.leward.kafka.metrics.avro.AvroMetric;

public class AvgMetric {

    private AvgValue avgCpuUsage = new AvgValue();
    private AvgValue avgMemoryUsage = new AvgValue();
    private AvgValue avgDiskUsage = new AvgValue();

    public AvgMetric() {}

    public AvgMetric(AvroAvgMetric data) {
        avgCpuUsage = new AvgValue(data.getCpuUsageCount(), data.getCpuUsageSum());
        avgMemoryUsage = new AvgValue(data.getMemoryUsageCount(), data.getMemoryUsageSum());
        avgDiskUsage = new AvgValue(data.getDiskUsageCount(), data.getDiskUsageSum());
    }

    public AvgMetric addMetric(AvroMetric metric) {
        avgCpuUsage.addValue(metric.getCpuUsage());
        avgMemoryUsage.addValue(metric.getMemoryUsage());
        avgDiskUsage.addValue(metric.getDiskUsage());
        return this;
    }

    public Metric toMetric() {
        return new Metric(avgCpuUsage.getAvg(), avgMemoryUsage.getAvg(), avgDiskUsage.getAvg());
    }

    public AvroAvgMetric toAvroAvgMetric() {
        return AvroAvgMetric.newBuilder()
                .setCpuUsageCount(avgCpuUsage.count)
                .setCpuUsageSum(avgCpuUsage.sum)
                .setMemoryUsageCount(avgMemoryUsage.count)
                .setMemoryUsageSum(avgMemoryUsage.sum)
                .setDiskUsageCount(avgDiskUsage.count)
                .setDiskUsageSum(avgDiskUsage.sum)
                .build();
    }

}
