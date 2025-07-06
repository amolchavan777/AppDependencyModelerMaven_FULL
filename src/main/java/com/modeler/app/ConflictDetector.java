package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Utility for detecting conflicting dependency claims.
 * Claims are grouped by application pair and marked as
 * conflicted when both positive and negative evidence exists.
 */
public class ConflictDetector {

    /** Result object describing a set of claims for a pair. */
    public static class ConflictGroup {
        public final InitialAggregator.Pair pair;
        public final List<Claim> claims;
        public final boolean conflicted;

        public ConflictGroup(InitialAggregator.Pair pair, List<Claim> claims, boolean conflicted) {
            this.pair = pair;
            this.claims = claims;
            this.conflicted = conflicted;
        }
    }

    /**
     * Cluster claims by (from,to) pair and indicate whether the group is conflicted.
     * @param claims all dependency claims
     * @return list of grouped claims with conflict marker
     */
    public static List<ConflictGroup> detect(List<Claim> claims) {
        Map<InitialAggregator.Pair, List<Claim>> grouped = new LinkedHashMap<>();
        for (Claim c : claims) {
            InitialAggregator.Pair key = new InitialAggregator.Pair(c.fromApp, c.toApp);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }

        List<ConflictGroup> result = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            boolean anyTrue = false;
            boolean anyFalse = false;
            for (Claim c : entry.getValue()) {
                if (c.exists) anyTrue = true; else anyFalse = true;
            }
            boolean conflicted = anyTrue && anyFalse;
            result.add(new ConflictGroup(entry.getKey(), entry.getValue(), conflicted));
        }
        return result;
    }

    /**
     * Convenience method returning only the conflicted groups.
     */
    public static List<ConflictGroup> findConflicts(List<Claim> claims) {
        List<ConflictGroup> all = detect(claims);
        List<ConflictGroup> conflicts = new ArrayList<>();
        for (ConflictGroup g : all) {
            if (g.conflicted) conflicts.add(g);
        }
        return conflicts;
    }
}
