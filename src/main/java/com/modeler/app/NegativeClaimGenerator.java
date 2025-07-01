package com.modeler.app;

import java.util.*;

/** Utility for generating negative claims based on missing dependencies. */
public class NegativeClaimGenerator {
    /**
     * Generate negative claims for sources that observed a given application but
     * did not report a dependency other sources saw.
     */
    public static List<Claim> generate(List<Claim> normalizedClaims) {
        Map<String, Set<String>> sourcesByApp = new HashMap<>();
        Set<InitialAggregator.Pair> positivePairs = new LinkedHashSet<>();
        for (Claim c : normalizedClaims) {
            sourcesByApp.computeIfAbsent(c.source, k -> new HashSet<>()).add(c.fromApp);
            if (c.exists) {
                positivePairs.add(new InitialAggregator.Pair(c.fromApp, c.toApp));
            }
        }
        // Quick lookup of existing claims per source
        Set<String> existing = new HashSet<>();
        for (Claim c : normalizedClaims) {
            existing.add(c.source + "|" + c.fromApp + "|" + c.toApp);
        }
        List<Claim> negatives = new ArrayList<>();
        for (var entry : sourcesByApp.entrySet()) {
            String source = entry.getKey();
            Set<String> fromSeen = entry.getValue();
            for (InitialAggregator.Pair pair : positivePairs) {
                if (!fromSeen.contains(pair.from)) continue; // source never reported this app
                String key = source + "|" + pair.from + "|" + pair.to;
                if (existing.contains(key)) continue; // source already has claim
                negatives.add(new Claim(source, pair.from, pair.to, false, 1.0));
            }
        }
        return negatives;
    }
}
