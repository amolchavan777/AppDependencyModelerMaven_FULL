package com.modeler.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Writes the dependency model to GraphML format which can be used with
 * tools such as yEd or Gephi for graph visualization.
 */
public class GraphMLExporter {

    /**
     * Export the dependency graph to a GraphML file.
     *
     * @param model    map of application -> dependencies
     * @param filePath destination of the GraphML document
     * @throws IOException if the file cannot be written
     */
    public static void export(Map<String, Set<String>> model, String filePath) throws IOException {
        try (BufferedWriter fw = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8)) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
            fw.write("  <graph id=\"G\" edgedefault=\"directed\">\n");

            // Emit all unique applications as nodes
            Set<String> allApps = new HashSet<>(model.keySet());
            model.values().forEach(allApps::addAll);
            for (String app : allApps) {
                fw.write("    <node id=\"" + app + "\"/>\n");
            }

            int edgeId = 1;
            // Emit edges for each dependency
            for (String from : model.keySet()) {
                for (String to : model.get(from)) {
                    fw.write("    <edge id=\"e" + (edgeId++) + "\" source=\"" + from + "\" target=\"" + to + "\"/>\n");
                }
            }
            fw.write("  </graph>\n</graphml>\n");
        }
    }
}
