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


        System.out.println("\n✅ Final Application Dependency Model:");
        for (var entry : result.entrySet())
            System.out.println(entry.getKey() + " depends on " + entry.getValue());

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
}