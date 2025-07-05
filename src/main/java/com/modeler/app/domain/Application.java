package com.modeler.app.domain;

import java.util.Objects;

/**
 * Represents an application within the dependency model.
 * Instances can be persisted via {@link com.modeler.app.PersistenceManager}.
 */
public class Application {
    private final String name;
    private final String type;
    private final String environment;
    private final String owner;

    private Application(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.environment = builder.environment;
        this.owner = builder.owner;
    }

    /**
     * Builder for {@link Application} instances.
     */
    public static class Builder {
        private String name;
        private String type;
        private String environment;
        private String owner;

        /** Set the unique application name. */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Optional application type (e.g., service, database). */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /** Deployment environment (e.g., prod, dev). */
        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        /** Application owner or team. */
        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        /**
         * Build a validated {@link Application} instance.
         * @throws IllegalArgumentException if name is missing
         */
        public Application build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Application name is required");
            }
            return new Application(this);
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return name + "(" + type + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
