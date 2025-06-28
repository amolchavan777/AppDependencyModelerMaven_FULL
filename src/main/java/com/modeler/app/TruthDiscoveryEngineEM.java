package com.modeler.app;

import java.util.*;

public class TruthDiscoveryEngineEM {

    private static final int MAX_ITERATIONS = 20;
    private static final double CONVERGENCE_THRESHOLD = 0.001;

    private Map<Claim, Double> claimProbabilities = new HashMap<>();
    private Map<String, Double> sourceTrust = new HashMap<>();

    public void runEM(List<Claim> claims) {
        initialize(claims);

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Map<Claim, Double> newClaimProbs = estimateTruths(claims);
            Map<String, Double> newSourceTrust = updateTrustworthiness(claims, newClaimProbs);

            if (hasConverged(newClaimProbs, newSourceTrust)) {
                break;
            }

            claimProbabilities = newClaimProbs;
            sourceTrust = newSourceTrust;
        }

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
                    prodTrue *= ts;
                    prodFalse *= (1.0 - ts);
                }
            }

            double probTrue = prodTrue / (prodTrue + prodFalse + 1e-10);
            updated.put(c, probTrue);
        }
        return updated;
    }

    private Map<String, Double> updateTrustworthiness(List<Claim> claims, Map<Claim, Double> claimProbs) {
        Map<String, Double> trust = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (Claim c : claims) {
            double p = claimProbs.getOrDefault(c, 0.5);
            trust.put(c.source, trust.getOrDefault(c.source, 0.0) + p);
            counts.put(c.source, counts.getOrDefault(c.source, 0) + 1);
        }

        for (String src : trust.keySet()) {
            trust.put(src, trust.get(src) / counts.get(src));
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
       // Add this field to store the result if not already present
    private Map<String, Set<String>> result = new HashMap<>();

    // After running EM, populate the result map accordingly in your runEM method

    // Add this method to allow access to the result
    public Map<String, Set<String>> getResult() {
        return result;
    }
}