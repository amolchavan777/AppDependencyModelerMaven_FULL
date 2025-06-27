package com.modeler.app;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ArchimateExporter {
    public static void export(Map<String, Set<String>> model, String filePath) throws IOException {
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<model xmlns=\"http://www.opengroup.org/xsd/archimate/3.0/\" identifier=\"model-1\" name=\"Application Dependency Model\">\n");
            fw.write("  <elements>\n");
            Set<String> allApps = new HashSet<>(model.keySet());
            model.values().forEach(allApps::addAll);
            for (String app : allApps) {
                fw.write("    <element identifier=\"" + app + "-id\" name=\"" + app + "\" type=\"application-component\"/>\n");
            }
            fw.write("  </elements>\n  <relationships>\n");
            int relId = 1;
            for (String from : model.keySet()) {
                for (String to : model.get(from)) {
                    fw.write("    <relationship identifier=\"rel" + (relId++) + "\" source=\"" + from + "-id\" target=\"" + to + "-id\" type=\"flow\"/>\n");
                }
            }
            fw.write("  </relationships>\n</model>\n");
        }
    }
}