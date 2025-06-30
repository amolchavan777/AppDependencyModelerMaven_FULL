package com.modeler.app;

import java.util.*;
import java.io.*;

// For calculating and printing dependency graph metrics
import com.modeler.app.DependencyMetrics;

/**
 * Entry point for the Application Dependency Modeler demo. It collects data
 * from all adapters, runs the truth discovery engine and exports the results
 * to ArchiMate and GraphML formats.
 */

public class Main {

    /**
     * Command-line execution.
     */
    public static void main(String[] args) throws IOException {
        try{
            // Gather normalized claims from all adapters
        List<Claim> allClaims = Normalizer.collectAllClaims();
        System.out.println("✅ Normalized Application Dependency Claims:");
        for (Claim c : allClaims) System.out.println(c);
        System.out.println("Collected " + allClaims.size() + " claims.");
        System.out.println("Running Latent Truth Model with EM...\n");

        // Resolve conflicting claims using the latent credibility engine
            TruthDiscoveryEngineEM engine = new TruthDiscoveryEngineEM();
            engine.runEM(allClaims);

       Map<String, Set<String>> result = engine.getResult();
        // If you want to use a run method with a specific number of iterations, you can add:
         //engine.run(allClaims, 10); // for example, 10 EM iterations

        // Or, if you want to call a run method instead of runEM:
        //engine.run(allClaims);


        printDependencySummary(result);

        // Print dependency analytics before exporting
        DependencyMetrics.printMetrics(result);

            // Export only the dependencies that survived truth discovery
            ArchimateExporter.export(result, "output/archimate_model.xml");
            System.out.println("\n📄 Archimate model exported to output/archimate_model.xml");

            GraphMLExporter.export(result, "output/dependency_graph.graphml");
            System.out.println("📄 GraphML model exported to output/dependency_graph.graphml");

        } catch (Exception e) {
            System.err.println("Error initializing Normalizer: " + e.getMessage());
            e.printStackTrace();

        }

    }

    /**
     * Print dependencies in an adjacency-list style with a summary line.
     */
    private static void printDependencySummary(Map<String, Set<String>> deps) {
        System.out.println("\n✅ Final Application Dependency Model:");
        List<String> apps = new ArrayList<>(deps.keySet());
        Collections.sort(apps);

        int total = 0;
        for (String app : apps) {
            System.out.println(app);
            Set<String> targets = deps.getOrDefault(app, Collections.emptySet());
            if (targets.isEmpty()) {
                System.out.println("  -> (no outgoing dependencies)");
            } else {
                List<String> sorted = new ArrayList<>(targets);
                Collections.sort(sorted);
                for (String t : sorted) {
                    System.out.println("  -> " + t);
                    total++;
                }
            }
        }

        System.out.printf("Summary: %d apps, %d total dependency links%n", apps.size(), total);
    }
}