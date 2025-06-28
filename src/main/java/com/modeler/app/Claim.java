package com.modeler.app;

/**
 * Represents a single dependency claim between two applications. Claims can
 * originate from any number of discovery sources (logs, configs, etc.) and
 * have an associated confidence score.
 */
public class Claim {
    /** discovery source identifier */
    public String source;
    /** name of the calling application */
    public String fromApp;
    /** name of the called application */
    public String toApp;
    /** whether the dependency exists in the source (true) or is an absence claim */
    public boolean exists;
    /** confidence level assigned by the source */
    public double confidence;

    /**
     * Construct a new {@code Claim} instance.
     */
    public Claim(String source, String fromApp, String toApp, boolean exists, double confidence) {
        this.source = source;
        this.fromApp = fromApp;
        this.toApp = toApp;
        this.exists = exists;
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return source + ": " + fromApp + " -> " + toApp + " | exists=" + exists + ", confidence=" + confidence;
    }
}