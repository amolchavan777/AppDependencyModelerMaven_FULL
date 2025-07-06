package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Simple majority voting strategy for dependency resolution.
 * Each positive claim counts as +1 and each negative claim as -1.
 * Dependencies with a final positive vote count are accepted.
 */
public class MajorityVotingResolver {
    public static Map<String, Set<String>> resolve(List<Claim> claims) {
        Map<String, Map<String, Integer>> votes = new HashMap<>();
        for (Claim c : claims) {
            int vote = c.exists ? 1 : -1;
            votes.computeIfAbsent(c.fromApp, k -> new HashMap<>())
                 .merge(c.toApp, vote, Integer::sum);
        }

        Map<String, Set<String>> result = new HashMap<>();
        for (var fromEntry : votes.entrySet()) {
            for (var toEntry : fromEntry.getValue().entrySet()) {
                if (toEntry.getValue() > 0) {
                    result.computeIfAbsent(fromEntry.getKey(), k -> new HashSet<>()).add(toEntry.getKey());
                }
            }
        }
        return result;
    }
}
