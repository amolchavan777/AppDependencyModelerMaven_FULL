package com.modeler.app.domain;

/** Wrapper around a numeric confidence value. */
public class ConfidenceScore {
    private final double value;

    private ConfidenceScore(double value) {
        this.value = value;
    }

    /**
     * Create a validated confidence score between 0 and 1.
     * @param v numeric value
     * @return score instance
     * @throws IllegalArgumentException if out of range
     */
    public static ConfidenceScore of(double v) {
        if (v < 0.0 || v > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
        return new ConfidenceScore(v);
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
