package com.modeler.app;

import java.util.*;
import java.io.*;

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
        // Gather normalized claims from all adapters
        List<Claim> claims = Normalizer.collectAllClaims();
        System.out.println("✅ Normalized Application Dependency Claims:");
        for (Claim c : claims) System.out.println(c);

        // Resolve conflicting claims using the latent credibility engine
        LatentCredibilityEngine engine = new LatentCredibilityEngine();
        Map<String, Set<String>> result = engine.run(claims, 10);

        System.out.println("\n✅ Final Application Dependency Model:");
        for (var entry : result.entrySet())
            System.out.println(entry.getKey() + " depends on " + entry.getValue());

        // Produce output files for visualization
        ArchimateExporter.export(result, "output/archimate_model.xml");
        System.out.println("\n📄 Archimate model exported to output/archimate_model.xml");

        GraphMLExporter.export(result, "output/dependency_graph.graphml");
        System.out.println("📄 GraphML model exported to output/dependency_graph.graphml");
    }
}