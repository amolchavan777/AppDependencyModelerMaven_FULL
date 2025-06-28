package com.modeler.app;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphMLExporter {
    public static void export(Map<String, Set<String>> model, String filePath) throws IOException {
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
            fw.write("  <graph id=\"G\" edgedefault=\"directed\">\n");
            Set<String> allApps = new HashSet<>(model.keySet());
            model.values().forEach(allApps::addAll);
            for (String app : allApps) {
                fw.write("    <node id=\"" + app + "\"/>\n");
            }
            int edgeId = 1;
            for (String from : model.keySet()) {
                for (String to : model.get(from)) {
                    fw.write("    <edge id=\"e" + (edgeId++) + "\" source=\"" + from + "\" target=\"" + to + "\"/>\n");
                }
            }
            fw.write("  </graph>\n</graphml>\n");
        }
    }
}
