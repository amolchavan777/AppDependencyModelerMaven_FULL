package com.modeler.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utility class that exports the dependency model to simple CSV summaries.
 */
public class CsvExporter {

    /**
     * Write summary and edge list CSV files for the provided model.
     *
     * @param model       map of application -> set of dependencies
     * @param summaryPath path of the summary CSV
     * @param edgesPath   path of the edge list CSV
     * @throws IOException if a file cannot be written
     */
    public static void export(Map<String, Set<String>> model,
                              String summaryPath,
                              String edgesPath) throws IOException {
        Map<String, Integer> outgoing = new HashMap<>();
        Map<String, Integer> incoming = new HashMap<>();
        Set<String> allApps = new HashSet<>();

        for (Map.Entry<String, Set<String>> entry : model.entrySet()) {
            String from = entry.getKey();
            Set<String> targets = entry.getValue();
            outgoing.put(from, targets.size());
            allApps.add(from);
            for (String to : targets) {
                incoming.put(to, incoming.getOrDefault(to, 0) + 1);
                allApps.add(to);
            }
        }

        Files.createDirectories(Paths.get(summaryPath).getParent());
        try (PrintWriter pw = new PrintWriter(summaryPath)) {
            pw.println("Application,OutgoingDependencies,IncomingDependencies");
            for (String app : allApps) {
                int out = outgoing.getOrDefault(app, 0);
                int in = incoming.getOrDefault(app, 0);
                pw.printf("%s,%d,%d%n", app, out, in);
            }
        }

        try (PrintWriter pw = new PrintWriter(edgesPath)) {
            pw.println("Source,Target");
            for (Map.Entry<String, Set<String>> entry : model.entrySet()) {
                for (String target : entry.getValue()) {
                    pw.printf("%s,%s%n", entry.getKey(), target);
                }
            }
        }
    }
}
