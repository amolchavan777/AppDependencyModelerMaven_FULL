package com.modeler.app;

public class Claim {
    public String source;
    public String fromApp;
    public String toApp;
    public boolean exists;
    public double confidence;

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