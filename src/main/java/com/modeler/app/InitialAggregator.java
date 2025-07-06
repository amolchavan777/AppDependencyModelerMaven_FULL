package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Simple aggregator that merges confidence scores for identical
 * dependency pairs before running the latent truth model.
 */
public class InitialAggregator {

    /** Key representing a (from,to) pair. */
    public static class Pair {
        public final String from;
        public final String to;
        public Pair(String f, String t) { this.from = f; this.to = t; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair p)) return false;
            return from.equals(p.from) && to.equals(p.to);
        }
        @Override public int hashCode() { return Objects.hash(from, to); }
    }

    /**
     * Aggregate confidence for each dependency pair.
     * Positive claims add confidence while negative claims subtract it.
     */
    public static Map<Pair, Double> aggregate(List<Claim> claims) {
        Map<Pair, Double> result = new LinkedHashMap<>();
        for (Claim c : claims) {
            Pair key = new Pair(c.fromApp, c.toApp);
            double val = c.exists ? c.confidence : -c.confidence;
            result.merge(key, val, Double::sum);
        }
        return result;
    }
}
