package com.modeler.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utility to export the dependency model to a simple JSON
 * structure with nodes and edges for use with web based
 * visualization libraries like D3.js or Cytoscape.js.
 */
public class JsonExporter {

    /**
     * Write the dependency graph to a JSON file.
     *
     * @param model    map of application -> dependencies
     * @param filePath destination file path
     * @throws IOException if writing fails
     */
    public static void export(Map<String, Set<String>> model, String filePath) throws IOException {
        List<Map<String, String>> nodes = new ArrayList<>();
        Set<String> allApps = new HashSet<>(model.keySet());
        model.values().forEach(allApps::addAll);
        for (String app : allApps) {
            nodes.add(Collections.singletonMap("id", app));
        }

        List<Map<String, String>> edges = new ArrayList<>();
        for (Map.Entry<String, Set<String>> e : model.entrySet()) {
            String src = e.getKey();
            for (String tgt : e.getValue()) {
                Map<String, String> edge = new HashMap<>();
                edge.put("source", src);
                edge.put("target", tgt);
                edges.add(edge);
            }
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), graph);
    }
}
