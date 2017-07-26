package eu.leward.kafka.metrics;

import eu.leward.kafka.metrics.avro.AvroMetric;

import java.util.Random;

public class Metric {

    public final float cpuUsage;
    public final float memoryUsage;
    public final float diskUsage;

    public Metric(float cpuUsage, float memoryUsage, float diskUsage) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
    }

    public Metric(AvroMetric avroMetric) {
        this(avroMetric.getCpuUsage(), avroMetric.getMemoryUsage(), avroMetric.getDiskUsage());
    }

    public static Metric randomMetric() {
        Random random = new Random();
        return new Metric(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public Metric generateNextRandomMetric() {
        Random random = new Random();
        float cpuUsageEvolution = (random.nextInt(30) * randomSignMultiplier()) / 100.0f;
        float memoryUsageEvolution = (random.nextInt(5) * randomSignMultiplier()) / 100.0f;
        float diskUsageEvolution = (random.nextInt(1) * randomSignMultiplier()) / 100.0f;

        float newCpuUsage = cpuUsage + cpuUsageEvolution;
        float newMemoryUsage = memoryUsage + memoryUsageEvolution;
        float newDiskUsage = diskUsage + diskUsageEvolution;

        if (newCpuUsage > 1) newCpuUsage = 1;
        else if(newCpuUsage < 0) newCpuUsage = 0;
        if (newMemoryUsage > 1) newMemoryUsage = 1;
        else if(newMemoryUsage < 0) newMemoryUsage = 0;
        if (newDiskUsage > 1) newDiskUsage = 1;
        else if(newDiskUsage < 0) newDiskUsage = 0;

        return new Metric(newCpuUsage, newMemoryUsage, newDiskUsage);
    }

    public AvroMetric toAvro() {
        return new AvroMetric(cpuUsage, memoryUsage, diskUsage);
    }

    private int randomSignMultiplier() {
        return (Math.random() < 0.5) ? 1 : -1;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "cpuUsage=" + cpuUsage +
                ", memoryUsage=" + memoryUsage +
                ", diskUsage=" + diskUsage +
                '}';
    }
}
