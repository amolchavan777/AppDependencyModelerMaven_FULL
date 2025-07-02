package com.modeler.app;

import java.util.*;
import java.io.*;

// JSON export
import com.modeler.app.JsonExporter;
import com.modeler.app.DashboardExporter;
import com.modeler.app.ExcelExporter;
import com.modeler.app.InitialAggregator;
import com.modeler.app.CoverageUtil;
import com.modeler.app.NegativeClaimGenerator;


// Histogram utilities
import com.modeler.app.DependencyHistogram;

// For calculating and printing dependency graph metrics
import com.modeler.app.DependencyMetrics;


/**
 * Entry point for the Application Dependency Modeler demo. It collects data
 * from all adapters, runs the truth discovery engine and exports the results
 * to ArchiMate, GraphML and JSON formats.
 */

public class Main {

    /**
     * Command-line execution.
     */
    public static void main(String[] args) throws IOException {
        try{
            // Gather raw claims from all adapters
        List<Claim> rawClaims = Normalizer.collectAllClaims();
        Map<String,String> aliasMap = Normalizer.getAliasMap();
        List<Claim> allClaims = Normalizer.normalizeClaims(rawClaims);
        List<Claim> negativeClaims = NegativeClaimGenerator.generate(allClaims);
        System.out.println("✅ Normalized Application Dependency Claims:");
        for (Claim c : allClaims) System.out.println(c);
        System.out.println("Collected " + allClaims.size() + " claims.");
        System.out.println("Running Latent Truth Model with EM...\n");

        Map<InitialAggregator.Pair, Double> initialAgg = InitialAggregator.aggregate(allClaims);
        Map<String, Integer> coverage = CoverageUtil.computeCoverage(allClaims);

        // Detect conflicting claim groups before EM
        List<Claim> combined = new ArrayList<>(allClaims);
        combined.addAll(negativeClaims);
        List<ConflictDetector.ConflictGroup> conflictGroups = ConflictDetector.detect(combined);
        long conflictCount = conflictGroups.stream().filter(g -> g.conflicted).count();
        System.out.println("Detected " + conflictCount + " conflicted claim groups.");
        for (ConflictDetector.ConflictGroup g : conflictGroups) {
            if (!g.conflicted) continue;
            System.out.println("❗ Conflict for " + g.pair.from + " -> " + g.pair.to);
            for (Claim c : g.claims) {
                System.out.println("   " + c.source + " says exists=" + c.exists);
            }
        }

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

        // Display simple histograms of dependency fan-out and fan-in
        DependencyHistogram.printOutgoingHistogram(result);
        DependencyHistogram.printIncomingHistogram(result);

            // Export only the dependencies that survived truth discovery
            ArchimateExporter.export(result, "output/archimate_model.xml");
            System.out.println("\n📄 Archimate model exported to output/archimate_model.xml");

            GraphMLExporter.export(result, "output/dependency_graph.graphml");
            System.out.println("📄 GraphML model exported to output/dependency_graph.graphml");


            JsonExporter.export(result, "output/dependency_graph.json");
            System.out.println("📄 JSON graph exported to output/dependency_graph.json");

            DashboardExporter.export("output");
            System.out.println("📄 Dashboard available at output/index.html");

//codex/add-csv-export-for-dependency-summaries
            CsvExporter.export(result, "output/dependency_summary.csv", "output/dependency_edges.csv");
            System.out.println("📄 CSV summaries exported to output/dependency_*.csv");

            // Export a multi-sheet Excel workbook for auditing
            ExcelExporter.export(rawClaims,
                    aliasMap,
                    allClaims,
                    negativeClaims,
                    initialAgg,
                    engine.getTrustHistory(),
                    engine.getClaimProbabilities(),
                    result,
                    coverage,
                    "output/application_dependency_audit.xlsx");
            System.out.println("📄 Excel audit workbook exported to output/application_dependency_audit.xlsx");

        

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred: " + e.getMessage());
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