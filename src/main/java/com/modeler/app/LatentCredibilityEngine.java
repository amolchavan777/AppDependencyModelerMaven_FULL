package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Implements a very small Expectation-Maximization style algorithm based on
 * the Latent Truth Model. Source reliability is iteratively refined while
 * aggregating claims into a final dependency model.
 */
public class LatentCredibilityEngine {
    /** Per-source statistics used while iterating. */
    static class SourceStats {
        double trust = 0.8;
        double totalWeight = 0.0;
    }

    /** Helper key representing a unique dependency claim. */
    static class ClaimKey {
        String from, to;
        public ClaimKey(String from, String to) { this.from = from; this.to = to; }
        public boolean equals(Object o) {
            if (!(o instanceof ClaimKey k)) return false;
            return from.equals(k.from) && to.equals(k.to);
        }
        public int hashCode() { return Objects.hash(from, to); }
    }


    /**
     * Run the truth discovery algorithm.
     *
     * @param claims     all dependency claims from every adapter
     * @param iterations number of EM iterations to perform
     * @return resolved application dependency graph
     */


    public Map<ClaimKey, Double> computeCredibility(List<Claim> claims, int iterations) {

        Map<String, SourceStats> sources = new HashMap<>();
        for (Claim c : claims) sources.putIfAbsent(c.source, new SourceStats());

        Map<ClaimKey, Double> credibility = new HashMap<>();
        for (int it = 0; it < iterations; it++) {
            credibility.clear();
            // Expectation step: accumulate credibility for each dependency
            for (Claim c : claims) {
                ClaimKey key = new ClaimKey(c.fromApp, c.toApp);
                double t = sources.get(c.source).trust;
                double score = c.exists ? t * c.confidence : (1 - t) * c.confidence;
                credibility.merge(key, score, Double::sum);
            }
            // Maximization step: update source trust using the accumulated scores
            for (var entry : sources.entrySet()) {
                double weighted = 0.0, total = 0.0;
                for (Claim c : claims) {
                    if (!c.source.equals(entry.getKey())) continue;
                    ClaimKey key = new ClaimKey(c.fromApp, c.toApp);
                    double cred = credibility.getOrDefault(key, 0.0);
                    weighted += (c.exists ? cred : (1 - cred)) * c.confidence;
                    total += c.confidence;
                }
                entry.getValue().trust = total > 0 ? weighted / total : 0.5;
            }
        }


        // Build the resulting dependency model where credibility > 0.5

        return credibility;
    }

    public Map<String, Set<String>> run(List<Claim> claims, int iterations) {
        Map<ClaimKey, Double> credibility = computeCredibility(claims, iterations);
        Map<String, Set<String>> result = new HashMap<>();
        for (Claim c : claims) {
            ClaimKey key = new ClaimKey(c.fromApp, c.toApp);
            if (credibility.getOrDefault(key, 0.0) > 0.5)
                result.computeIfAbsent(c.fromApp, k -> new HashSet<>()).add(c.toApp);
        }
        return result;
    }
}