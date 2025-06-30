package com.modeler.app;

import java.util.*;

/**
 * Utility class for printing simple ASCII histograms of dependency counts.
 */
public class DependencyHistogram {

    /**
     * Print a histogram showing number of outgoing dependencies for each application.
     * @param model the resolved dependency graph
     */
    public static void printOutgoingHistogram(Map<String, Set<String>> model) {
        System.out.println("\n\uD83D\uDCCA Outgoing Dependency Counts:");
        for (var entry : model.entrySet()) {
            int count = entry.getValue().size();
            System.out.printf("%-15s | %s (%d)%n", entry.getKey(), "*".repeat(count), count);
        }
    }

    /**
     * Print a histogram showing number of incoming dependencies (fan-in) for each application.
     * @param model the resolved dependency graph
     */
    public static void printIncomingHistogram(Map<String, Set<String>> model) {
        Map<String, Integer> incoming = new HashMap<>();
        for (var entry : model.entrySet()) {
            for (String to : entry.getValue()) {
                incoming.put(to, incoming.getOrDefault(to, 0) + 1);
            }
        }
        System.out.println("\n\uD83D\uDCCA Incoming Dependency Counts:");
        for (var entry : incoming.entrySet()) {
            int count = entry.getValue();
            System.out.printf("%-15s | %s (%d)%n", entry.getKey(), "*".repeat(count), count);
        }
    }
}
