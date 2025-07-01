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
    /** relationship type (e.g. calls, default_db) */
    public String type;
    /** whether the dependency exists in the source (true) or is an absence claim */
    public boolean exists;
    /** confidence level assigned by the source */
    public double confidence;
    /** optional timestamp associated with the claim */
    public String timestamp;
    /** optional metadata or raw line */
    public String metadata;

    /**
     * Construct a new {@code Claim} instance using the default relationship type
     * of {@code "calls"}.
     */
    public Claim(String source, String fromApp, String toApp, boolean exists, double confidence) {
        this(source, fromApp, toApp, "calls", exists, confidence, null, null);
    }

    /**
     * Construct a new {@code Claim} with optional timestamp and metadata using
     * the default relationship type of {@code "calls"}.
     */
    public Claim(String source, String fromApp, String toApp, boolean exists, double confidence,
                 String timestamp, String metadata) {
        this(source, fromApp, toApp, "calls", exists, confidence, timestamp, metadata);
    }

    /**
     * Construct a new {@code Claim} with an explicit relationship type.
     */
    public Claim(String source, String fromApp, String toApp, String type, boolean exists, double confidence) {
        this(source, fromApp, toApp, type, exists, confidence, null, null);
    }

    /**
     * Fully specified constructor.
     */
    public Claim(String source, String fromApp, String toApp, String type,
                 boolean exists, double confidence, String timestamp, String metadata) {
        this.source = source;
        this.fromApp = fromApp;
        this.toApp = toApp;
        this.type = type;
        this.exists = exists;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        String base = source + ": " + fromApp + " -> " + toApp + " | exists=" + exists + ", confidence=" + confidence;
        if (timestamp != null) base += ", timestamp=" + timestamp;
        if (metadata != null) base += ", metadata=" + metadata;
        return base;
    }
}