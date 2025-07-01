package com.modeler.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Exports only the high confidence dependencies discovered by the EM engine.
 * Provides traceability by including claim ids, sources and probabilities.
 */
public class FilteredModelExporter {

    /**
     * Build a dependency graph containing only claims with probability above
     * the provided threshold.
     */
    public static Map<String, Set<String>> buildGraph(Map<Claim, Double> claimProbs,
                                                      double threshold) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (var entry : claimProbs.entrySet()) {
            Claim c = entry.getKey();
            double p = entry.getValue();
            if (c.exists && p > threshold) {
                graph.computeIfAbsent(c.fromApp, k -> new LinkedHashSet<>()).add(c.toApp);
            }
        }
        return graph;
    }

    /**
     * Export the filtered dependency model to multiple formats. A CSV file of
     * the resolved claims is always produced in addition to GraphML and summary
     * CSVs.
     */
    public static void export(Map<Claim, Double> claimProbs,
                              double threshold,
                              String graphmlPath,
                              String summaryCsv,
                              String edgesCsv,
                              String claimsCsv) throws IOException {
        Map<String, Set<String>> graph = buildGraph(claimProbs, threshold);

        GraphMLExporter.export(graph, graphmlPath);
        CsvExporter.export(graph, summaryCsv, edgesCsv);

        Files.createDirectories(Paths.get(claimsCsv).getParent());
        try (PrintWriter pw = new PrintWriter(claimsCsv)) {
            pw.println("id,source,fromApp,toApp,probability");
            int id = 1;
            for (var entry : claimProbs.entrySet()) {
                Claim c = entry.getKey();
                double p = entry.getValue();
                if (c.exists && p > threshold) {
                    pw.printf("%d,%s,%s,%s,%.3f%n", id++, c.source, c.fromApp, c.toApp, p);
                }
            }
        }
    }
}
