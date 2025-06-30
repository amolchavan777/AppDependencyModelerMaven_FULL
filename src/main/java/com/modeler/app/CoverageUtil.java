package com.modeler.app;

import java.util.*;

/** Utility for computing data coverage per application. */
public class CoverageUtil {
    /**
     * Count how many unique sources reference each application
     * either as a caller or callee.
     */
    public static Map<String,Integer> computeCoverage(List<Claim> claims) {
        Map<String, Set<String>> byApp = new HashMap<>();
        for (Claim c : claims) {
            byApp.computeIfAbsent(c.fromApp, k -> new HashSet<>()).add(c.source);
            byApp.computeIfAbsent(c.toApp, k -> new HashSet<>()).add(c.source);
        }
        Map<String,Integer> counts = new HashMap<>();
        for (var e : byApp.entrySet()) {
            counts.put(e.getKey(), e.getValue().size());
        }
        return counts;
    }
}
