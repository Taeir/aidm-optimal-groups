package nl.tudelft.aidm.optimalgroups.metric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Distribution {

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

    public float getStartValue() { return this.startValue; }
    public float getEndValue() { return this.endValue; }

    protected void calculate() {}

    public String distributionName() {
        return "distribution";
    }

    public static class Partition {
        private float startInclusive;
        private float endExclusive;
        private List<Float> values;

        public Partition(float startInclusive, float endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
            this.values = new ArrayList<>();
        }

        public List<Float> getValues() { return this.values; }
        public float getStart() { return this.startInclusive; }
        public float getEnd() { return this.endExclusive; }

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

    public static class AverageDistribution extends Distribution {

        Distribution[] distributions;
        private int partitionAmount;

        public AverageDistribution(Distribution[] distributions) {
            super(distributions[0].getStartValue(), distributions[0].getEndValue(), distributions[0].asList().size());
            this.distributions = distributions;
            this.partitionAmount = distributions[0].asList().size();
        }

        @Override
        public void printResult() {
            float[] averagePartitions = this.getResult();

            System.out.printf("Average result of %s: \n", this.distributions[0].distributionName());
            for (int i = 0; i < this.partitionAmount; i++) {
                System.out.printf("\t\t- Partition: start: %f, end: %f, values in partition: %f\n",
                        this.distributions[0].asList().get(i).getStart(),
                        this.distributions[0].asList().get(i).getEnd(),
                        averagePartitions[i]);
            }
        }

        public float[] getResult() {
            float[] averagePartitions = new float[this.partitionAmount];

            int partitionIndex;
            for (Distribution distribution : this.distributions) {
                partitionIndex = 0;
                for (Partition partition : distribution.asList()) {
                    averagePartitions[partitionIndex] += partition.getValues().size();
                    partitionIndex++;
                }
            }

            for (int i = 0; i < this.partitionAmount; i++) {
                averagePartitions[i] = averagePartitions[i] / this.distributions.length;
            }

            return averagePartitions;
        }

        public void printToTxtFile(String fileName) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
                writer.write(String.format("%f %f %d\r\n", this.startValue, this.endValue, this.partitionAmount));
                for (float f : this.getResult()) {
                    writer.write(String.valueOf(f));
                    writer.write("\r\n");
                }
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
