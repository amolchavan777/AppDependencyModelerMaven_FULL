package com.modeler.app;

import java.util.*;

public class TruthDiscoveryEngineEM {

    private static final int MAX_ITERATIONS = 20;
    private static final double CONVERGENCE_THRESHOLD = 0.001;

    private Map<Claim, Double> claimProbabilities = new HashMap<>();
    private Map<String, Double> sourceTrust = new HashMap<>();
    private final List<Map<String, Double>> trustHistory = new ArrayList<>();

    public void runEM(List<Claim> claims) {
        initialize(claims);
        trustHistory.clear();
        trustHistory.add(new HashMap<>(sourceTrust));

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Map<Claim, Double> newClaimProbs = estimateTruths(claims);
            Map<String, Double> newSourceTrust = updateTrustworthiness(claims, newClaimProbs);

            claimProbabilities = newClaimProbs;
            sourceTrust = newSourceTrust;
            trustHistory.add(new HashMap<>(sourceTrust));

            if (hasConverged(newClaimProbs, newSourceTrust)) {
                break;
            }
        }

        buildResult();
        printResults();
    }

    private void initialize(List<Claim> claims) {
        for (Claim c : claims) {
            claimProbabilities.put(c, 0.5);  // initial belief
            sourceTrust.put(c.source, 0.9);  // optimistic start
        }
    }

    private Map<Claim, Double> estimateTruths(List<Claim> claims) {
        Map<Claim, Double> updated = new HashMap<>();
        for (Claim c : claims) {
            double prodTrue = 1.0;
            double prodFalse = 1.0;

            for (Claim other : claims) {
                if (other.fromApp.equals(c.fromApp) && other.toApp.equals(c.toApp)) {
                    double ts = sourceTrust.getOrDefault(other.source, 0.5);
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

    private boolean hasConverged(Map<Claim, Double> newProbs, Map<String, Double> newTrust) {
        for (Claim c : newProbs.keySet()) {
            if (Math.abs(newProbs.get(c) - claimProbabilities.getOrDefault(c, 0.0)) > CONVERGENCE_THRESHOLD) {
                return false;
            }
        }
        for (String s : newTrust.keySet()) {
            if (Math.abs(newTrust.get(s) - sourceTrust.getOrDefault(s, 0.0)) > CONVERGENCE_THRESHOLD) {
                return false;
            }
        }
        return true;
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