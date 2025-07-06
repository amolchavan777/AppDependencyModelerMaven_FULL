package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Simple reliability-weighted voting strategy for dependency resolution.
 * Each claim contributes its confidence multiplied by the reliability weight
 * of the source. Positive claims add weight while negative claims subtract it.
 * Dependencies with a final positive score are accepted.
 */
public class WeightedVotingResolver {
    public static Map<String, Set<String>> resolve(List<Claim> claims, Map<String, Double> weights) {
        Map<String, Map<String, Double>> scores = new HashMap<>();
        for (Claim c : claims) {
            double w = weights.getOrDefault(c.source, 1.0) * c.confidence;
            double val = c.exists ? w : -w;
            scores.computeIfAbsent(c.fromApp, k -> new HashMap<>())
                  .merge(c.toApp, val, Double::sum);
        }

        Map<String, Set<String>> result = new HashMap<>();
        for (var fromEntry : scores.entrySet()) {
            for (var toEntry : fromEntry.getValue().entrySet()) {
                if (toEntry.getValue() > 0) {
                    result.computeIfAbsent(fromEntry.getKey(), k -> new HashSet<>()).add(toEntry.getKey());
                }
            }
        }
        return result;
    }
}
