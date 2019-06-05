package nl.tudelft.aidm.optimalgroups.metric;

import java.util.ArrayList;
import java.util.List;

public abstract class Distribution {

    protected float startValue;
    protected float endValue;
    protected List<Partition> partitions;
    private Partition lastPartition;

    protected boolean calculated = false;

    public Distribution(float startValue, float endValue, int partitionAmount) {
        this.partitions = new ArrayList<>(partitionAmount);
        this.startValue = startValue;
        this.endValue = endValue;

        float stepSize = (endValue - startValue) / partitionAmount;

        for (int i = 0; i < partitionAmount; i++) {
            Partition partition = new Partition(startValue + i * stepSize, startValue + (i+1) * stepSize);
            this.partitions.add(partition);
        }

        // Explicitly keep last partition to check in addValue if a value is the exact end value
        // (for all other partitions, the endValue is exclusive, but not for the last one)
        this.lastPartition = this.partitions.get(this.partitions.size() - 1);
    }

    protected boolean addValue(float value) {

        // Only if it is in range
        if (value < this.startValue || value > this.endValue) {
            System.out.printf("Distribution cannot add value %f: lower than %f or higher than %f\n", value, this.startValue, this.endValue);
            return false;
        }

        for (Partition partition : this.partitions) {
            if (partition.isInRange(value)) {
                partition.addValue(value);

                return true;
            }
        }

        if (value == this.endValue) {
            this.lastPartition.addValue(value);
            return true;
        }

        System.out.printf("Distribution cannot add value %f: not equal to %f\n", value, this.endValue);

        return false;
    }

    public List<Partition> asList() {
        if (!calculated) {
            this.calculated = true;
            this.calculate();
        }

        return this.partitions;
    }

    public void printResult() {
        System.out.printf("Result of %s: \n", this.distributionName());
        for (Partition partition : this.asList()) {
            System.out.printf("\t\t- %s\n", partition.toString());
        }
    }

    protected abstract void calculate();

    protected abstract String distributionName();

    public static class Partition {
        private float startInclusive;
        private float endExclusive;

        List<Float> values;

        public Partition(float startInclusive, float endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
            this.values = new ArrayList<>();
        }

        public void addValue(float value) {
            this.values.add(value);
        }

        public boolean isInRange(float value) {
            return (value >= this.startInclusive && value < this.endExclusive);
        }

        public String toString() {
            return String.format("Partition: start: %f, end: %f, values in partition: %d", this.startInclusive, this.endExclusive, this.values.size());
        }
    }
}
