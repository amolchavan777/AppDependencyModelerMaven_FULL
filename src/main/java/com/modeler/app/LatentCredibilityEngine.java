package com.modeler.app;

import java.util.*;

public class LatentCredibilityEngine {
    static class SourceStats {
        double trust = 0.8;
        double totalWeight = 0.0;
    }

    static class ClaimKey {
        String from, to;
        public ClaimKey(String from, String to) { this.from = from; this.to = to; }
        public boolean equals(Object o) {
            if (!(o instanceof ClaimKey k)) return false;
            return from.equals(k.from) && to.equals(k.to);
        }
        public int hashCode() { return Objects.hash(from, to); }
    }

    public Map<String, Set<String>> run(List<Claim> claims, int iterations) {
        Map<String, SourceStats> sources = new HashMap<>();
        for (Claim c : claims) sources.putIfAbsent(c.source, new SourceStats());

        Map<ClaimKey, Double> credibility = new HashMap<>();
        for (int it = 0; it < iterations; it++) {
            credibility.clear();
            for (Claim c : claims) {
                ClaimKey key = new ClaimKey(c.fromApp, c.toApp);
                double t = sources.get(c.source).trust;
                double score = c.exists ? t * c.confidence : (1 - t) * c.confidence;
                credibility.merge(key, score, Double::sum);
            }
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
        Map<String, Set<String>> result = new HashMap<>();
        for (Claim c : claims) {
            ClaimKey key = new ClaimKey(c.fromApp, c.toApp);
            if (credibility.getOrDefault(key, 0.0) > 0.5)
                result.computeIfAbsent(c.fromApp, k -> new HashSet<>()).add(c.toApp);
        }
        return result;
    }
}