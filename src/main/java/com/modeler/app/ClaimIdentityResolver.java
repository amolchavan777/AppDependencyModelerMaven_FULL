package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Resolves claim identities by normalizing aliases and grouping
 * claims that refer to the same dependency.
 * Each group receives a canonical claim ID.
 */
public class ClaimIdentityResolver {
    /** mapping of alias -> canonical name */
    private final Map<String,String> aliasMap;

    public ClaimIdentityResolver(Map<String,String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    /**
     * Result record combining the normalized claim with its assigned ID.
     * @param id canonical claim identifier
     * @param from normalized source application name
     * @param to normalized target application name
     * @param claim the normalized claim instance
     */
    public record ResolvedClaim(String id, String from, String to, Claim claim) {}

    /**
     * Normalize all claims and assign canonical claim IDs. Claims that resolve
     * to the same (from,to) pair share the same ID.
     *
     * @param rawClaims claims before normalization
     * @return list of resolved claims with IDs
     */
    public List<ResolvedClaim> resolve(List<Claim> rawClaims) {
        Map<String,List<Claim>> groups = new TreeMap<>();
        for (Claim c : rawClaims) {
            String from = aliasMap.getOrDefault(c.fromApp, c.fromApp);
            String to = aliasMap.getOrDefault(c.toApp, c.toApp);
            Claim norm = new Claim(c.source, from, to, c.exists, c.confidence, c.timestamp, c.metadata);
            String key = from + "|" + to;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(norm);
        }

        List<ResolvedClaim> result = new ArrayList<>();
        int counter = 1;
        for (var entry : groups.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String id = "C" + counter++;
            for (Claim c : entry.getValue()) {
                result.add(new ResolvedClaim(id, parts[0], parts[1], c));
            }
        }
        return result;
    }
}
