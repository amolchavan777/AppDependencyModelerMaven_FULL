package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CsvExporterTest {
    @Test
    public void writesCsvFiles() throws Exception {
        Map<String, Set<String>> model = new HashMap<>();
        model.put("A", new HashSet<>(Set.of("B")));
        model.put("B", Collections.emptySet());

        Path dir = Files.createTempDirectory("csvexp");
        Path summary = dir.resolve("summary.csv");
        Path edges = dir.resolve("edges.csv");

        CsvExporter.export(model, summary.toString(), edges.toString());

        assertTrue(Files.exists(summary), "Summary file should exist");
        assertTrue(Files.exists(edges), "Edges file should exist");

        List<String> sumLines = Files.readAllLines(summary);
        assertFalse(sumLines.isEmpty(), "Summary should not be empty");
        assertTrue(sumLines.get(0).contains("Application"));

        List<String> edgeLines = Files.readAllLines(edges);
        assertTrue(edgeLines.stream().anyMatch(l -> l.equals("A,B")), "A,B edge missing");
    }
}
