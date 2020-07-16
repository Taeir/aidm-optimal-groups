package nl.tudelft.aidm.optimalgroups.metric.matching.aupcr;

import org.jetbrains.annotations.NotNull;

/**
 * Area Under Profile Curve Ratio (AUPCR) metric.
 * An AUPCR of 1 is perfect and an AUPCR of 0 is terrible.
 * Defined on page 8 of (Diebold & Bichler, 2016)
 */
public abstract class AUPCR implements Comparable<AUPCR> {

    protected double aupcr = -1;

    public Double asDouble() {
        if (this.aupcr == -1) {
            this.aupcr = ((double) this.aupc()) / this.totalArea();
        }

        return this.aupcr;
    }

    public abstract void printResult();

    protected abstract float totalArea();

    protected abstract int aupc();

    @Override
    public int compareTo(@NotNull AUPCR o)
    {
        return this.asDouble().compareTo(o.asDouble());
    }
}
