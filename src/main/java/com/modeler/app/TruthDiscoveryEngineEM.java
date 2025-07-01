package com.modeler.app;

import java.util.*;

public class TruthDiscoveryEngineEM {

    private static final int MAX_ITERATIONS = 20;
    private static final double CONVERGENCE_THRESHOLD = 0.001;

    private Map<Claim, Double> claimProbabilities = new HashMap<>();
    private Map<String, Double> sourceTrust = new HashMap<>();
    private final List<Map<String, Double>> trustHistory = new ArrayList<>();

    public void runEM(List<Claim> claims) {
        claimProbabilities.clear();
        sourceTrust.clear();
        trustHistory.clear();

        Map<String, List<Claim>> groups = new LinkedHashMap<>();
        for (Claim c : claims) {
            String key = c.fromApp + "|" + c.toApp;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }

        Map<String, Double> trustSum = new HashMap<>();
        Map<String, Integer> trustCount = new HashMap<>();

        for (List<Claim> group : groups.values()) {
            if (group.isEmpty()) continue;
            GroupResult gr = runGroupEM(group);
            claimProbabilities.putAll(gr.claimProbs);
            for (var e : gr.sourceTrust.entrySet()) {
                trustSum.merge(e.getKey(), e.getValue(), Double::sum);
                trustCount.merge(e.getKey(), 1, Integer::sum);
            }
        }

        for (String src : trustSum.keySet()) {
            sourceTrust.put(src, trustSum.get(src) / trustCount.getOrDefault(src, 1));
        }

        buildResult();
        printResults();
    }

    private Map<Claim, Double> estimateTruths(List<Claim> claims, Map<String, Double> trust) {
        Map<Claim, Double> updated = new HashMap<>();
        for (Claim c : claims) {
            double prodTrue = 1.0;
            double prodFalse = 1.0;

            for (Claim other : claims) {
                if (other.fromApp.equals(c.fromApp) && other.toApp.equals(c.toApp)) {
                    double ts = trust.getOrDefault(other.source, 0.5);
                    if (other.exists) {
                        prodTrue  *= Math.pow(ts, other.confidence);
                        prodFalse *= Math.pow(1.0 - ts, other.confidence);
                    } else {
                        // Negative claim: trusted sources should push probability toward false
                        prodTrue  *= Math.pow(1.0 - ts, other.confidence);
                        prodFalse *= Math.pow(ts, other.confidence);
                    }
                }
            }

            double probTrue = prodTrue / (prodTrue + prodFalse + 1e-10);
            updated.put(c, probTrue);
        }
        return updated;
    }

    private Map<String, Double> updateTrustworthiness(List<Claim> claims, Map<Claim, Double> claimProbs) {
        Map<String, Double> trust = new HashMap<>();
        Map<String, Double> counts = new HashMap<>();

        for (Claim c : claims) {
            double p = claimProbs.getOrDefault(c, 0.5);
            double weight = c.confidence;
            double contribution = c.exists ? p : (1.0 - p);
            trust.put(c.source, trust.getOrDefault(c.source, 0.0) + contribution * weight);
            counts.put(c.source, counts.getOrDefault(c.source, 0.0) + weight);
        }

        for (String src : trust.keySet()) {
            double total = counts.getOrDefault(src, 1.0);
            trust.put(src, trust.get(src) / total);
        }
        return trust;
    }

    private boolean hasConverged(Map<Claim, Double> oldProbs,
                                 Map<String, Double> oldTrust,
                                 Map<Claim, Double> newProbs,
                                 Map<String, Double> newTrust) {
        for (Claim c : newProbs.keySet()) {
            if (Math.abs(newProbs.get(c) - oldProbs.getOrDefault(c, 0.0)) > CONVERGENCE_THRESHOLD) {
                return false;
            }
        }
        for (String s : newTrust.keySet()) {
            if (Math.abs(newTrust.get(s) - oldTrust.getOrDefault(s, 0.0)) > CONVERGENCE_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    private static class GroupResult {
        Map<Claim, Double> claimProbs = new HashMap<>();
        Map<String, Double> sourceTrust = new HashMap<>();
    }

    private GroupResult runGroupEM(List<Claim> group) {
        GroupResult res = new GroupResult();
        Map<Claim, Double> claimProbs = new HashMap<>();
        Map<String, Double> trust = new HashMap<>();

        for (Claim c : group) {
            claimProbs.put(c, 0.5);
            trust.put(c.source, 0.9);
        }
        trustHistory.add(new HashMap<>(trust));

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Map<Claim, Double> newProbs = estimateTruths(group, trust);
            Map<String, Double> newTrust = updateTrustworthiness(group, newProbs);

            if (hasConverged(claimProbs, trust, newProbs, newTrust)) {
                claimProbs = newProbs;
                trust = newTrust;
                trustHistory.add(new HashMap<>(trust));
                break;
            }

            claimProbs = newProbs;
            trust = newTrust;
            trustHistory.add(new HashMap<>(trust));
        }

        res.claimProbs.putAll(claimProbs);
        res.sourceTrust.putAll(trust);
        return res;
    }

    private void printResults() {
        System.out.println("Final claim truth scores:");
        for (Map.Entry<Claim, Double> e : claimProbabilities.entrySet()) {
            if (e.getValue() > 0.5) {
                System.out.printf("%s -> %s [%.3f] via %s%n", e.getKey().fromApp, e.getKey().toApp, e.getValue(), e.getKey().source);
            }
        }
        System.out.println("\nFinal source trust scores:");
        for (Map.Entry<String, Double> e : sourceTrust.entrySet()) {
            System.out.printf("%s: %.3f%n", e.getKey(), e.getValue());
        }
    }

    private void buildResult() {
        result.clear();
        for (Map.Entry<Claim, Double> e : claimProbabilities.entrySet()) {
            Claim c = e.getKey();
            if (c.exists && e.getValue() > 0.5) {
                result.computeIfAbsent(c.fromApp, k -> new HashSet<>()).add(c.toApp);
            }
        }
    }

    private Map<String, Set<String>> result = new HashMap<>();

    public Map<String, Set<String>> getResult() {
        return result;
    }

    /**
     * Access the final truth probability computed for each claim.
     *
     * @return map of Claim -> probability that the claim is true
     */
    public Map<Claim, Double> getClaimProbabilities() {
        return claimProbabilities;
    }

    /**
     * Access the trustworthiness score for each source after EM convergence.
     *
     * @return map of source name -> trust score
     */
    public Map<String, Double> getSourceTrust() {
        return sourceTrust;
    }

    /**
     * Access the trustworthiness history over EM iterations.
     */
    public List<Map<String, Double>> getTrustHistory() {
        return trustHistory;
    }
}