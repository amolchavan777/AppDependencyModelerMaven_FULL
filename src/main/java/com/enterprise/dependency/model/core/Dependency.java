package com.enterprise.dependency.model.core;

import java.util.Objects;

/**
 * Represents a resolved dependency between two applications.
 */
public class Dependency {
    private final Application from;
    private final Application to;
    private final DependencyType type;
    private final double confidence;

    private Dependency(Builder b) {
        this.from = b.from;
        this.to = b.to;
        this.type = b.type;
        this.confidence = b.confidence;
    }

    /** Builder for {@link Dependency}. */
    public static class Builder {
        private Application from;
        private Application to;
        private DependencyType type = DependencyType.CALLS;
        private double confidence;

        /** Source application. */
        public Builder from(Application from) { this.from = from; return this; }
        /** Target application. */
        public Builder to(Application to) { this.to = to; return this; }
        /** Relationship type. */
        public Builder type(DependencyType type) { this.type = type; return this; }
        /** Confidence level. */
        public Builder confidence(double confidence) { this.confidence = confidence; return this; }

        /** Validate and build the dependency instance. */
        public Dependency build() {
            if (from == null || to == null) {
                throw new IllegalArgumentException("Both from and to applications are required");
            }
            return new Dependency(this);
        }
    }

    public Application getFrom() { return from; }
    public Application getTo() { return to; }
    public DependencyType getType() { return type; }
    public double getConfidence() { return confidence; }

    @Override
    public String toString() {
        return from.getName() + " -> " + to.getName() + " (" + confidence + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, type);
    }
}
