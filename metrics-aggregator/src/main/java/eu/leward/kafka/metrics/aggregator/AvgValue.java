package eu.leward.kafka.metrics.aggregator;


public class AvgValue {

    int count;
    float sum;

    public AvgValue(int count, float sum) {
        this.count = count;
        this.sum = sum;
    }

    public AvgValue() {
        count = 0;
        sum = 0;
    }

    public float getAvg() {
        return sum / count;
    }

    public void addValue(float value) {
        count++;
        sum += value;
    }

}
