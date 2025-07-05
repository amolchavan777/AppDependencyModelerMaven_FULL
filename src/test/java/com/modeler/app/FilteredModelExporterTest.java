package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FilteredModelExporterTest {
    @Test
    public void exportsHighConfidenceModel() throws Exception {
        Claim c1 = new Claim("s1", "A", "B", true, 1.0);
        Claim c2 = new Claim("s2", "A", "C", true, 1.0);

        Map<Claim, Double> probs = new LinkedHashMap<>();
        probs.put(c1, 0.6);
        probs.put(c2, 0.4);

        Path dir = Files.createTempDirectory("fme");

        FilteredModelExporter.export(probs, 0.5,
                dir.resolve("graph.graphml").toString(),
                dir.resolve("summary.csv").toString(),
                dir.resolve("edges.csv").toString(),
                dir.resolve("claims.csv").toString());

        assertTrue(Files.exists(dir.resolve("graph.graphml")), "GraphML missing");
        assertTrue(Files.exists(dir.resolve("claims.csv")), "Claims CSV missing");

        List<String> lines = Files.readAllLines(dir.resolve("claims.csv"));
        assertEquals("id,source,fromApp,toApp,probability", lines.get(0));
        assertTrue(lines.stream().anyMatch(l -> l.contains(",A,B,")), "A->B missing");
        assertFalse(lines.stream().anyMatch(l -> l.contains(",A,C,")), "A->C should be filtered");
    }
}
