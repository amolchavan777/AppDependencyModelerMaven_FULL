package com.modeler.app;

/**
 * Utility for producing a flat string signature for a dependency claim.
 * Allows dependency injection of custom formatting logic.
 */
public class StringClaimMerger {

    /**
     * Strategy interface for formatting a {@link Claim}.
     */
    @FunctionalInterface
    public interface Formatter {
        String format(Claim claim);
    }

    private final Formatter formatter;

    /**
     * Create a merger with the given formatter.
     *
     * @param formatter formatting strategy
     */
    public StringClaimMerger(Formatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Flatten the provided claim using the configured {@link Formatter}.
     *
     * @param claim the claim to flatten
     * @return flat string representation
     */
    public String merge(Claim claim) {
        return formatter.format(claim);
    }

    /**
     * Default merger that concatenates key claim fields. The output is of the
     * form:
     * {@code from->to|exists=true|confidence=0.9|source=s1|timestamp=ts|metadata=m}
     */
    public static StringClaimMerger defaultMerger() {
        return new StringClaimMerger(c -> {
            StringBuilder sb = new StringBuilder();
            sb.append(c.fromApp).append("->").append(c.toApp);
            sb.append("|exists=").append(c.exists);
            sb.append("|confidence=").append(c.confidence);
            sb.append("|source=").append(c.source);
            if (c.timestamp != null) sb.append("|timestamp=").append(c.timestamp);
            if (c.metadata != null) sb.append("|metadata=").append(c.metadata);
            return sb.toString();
        });
    }
}
