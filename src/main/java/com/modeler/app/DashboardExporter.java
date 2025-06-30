package com.modeler.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Copies the bundled dashboard HTML page to the output directory so
 * developers can open it in a browser to explore the dependency graph.
 */
public class DashboardExporter {

    /**
     * Copy the dashboard index.html into the specified directory.
     *
     * @param outputDir directory to copy the dashboard into
     * @throws IOException if the resource cannot be written
     */
    public static void export(String outputDir) throws IOException {
        Path outPath = Paths.get(outputDir, "index.html");
        Files.createDirectories(outPath.getParent());
        try (InputStream in = DashboardExporter.class.getResourceAsStream("/dashboard/index.html")) {
            if (in == null) {
                throw new IOException("Dashboard resource not found");
            }
            Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
