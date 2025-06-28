package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LatentCredibilityEngineTest {
    @Test
    public void simpleRunProducesModel() {
        List<Claim> claims = List.of(
            new Claim("s1", "A", "B", true, 1.0),
            new Claim("s2", "A", "B", true, 1.0),
            new Claim("s1", "B", "C", true, 1.0)
        );
        LatentCredibilityEngine engine = new LatentCredibilityEngine();
        Map<String, Set<String>> result = engine.run(claims, 1);
        assertEquals(Set.of("B"), result.get("A"));
        assertEquals(Set.of("C"), result.get("B"));
    }

    @Test
    public void credibilityScoresMatchExpectation() {
        List<Claim> claims = List.of(
            new Claim("s1", "A", "B", true, 1.0),
            new Claim("s2", "A", "B", true, 0.5),
            new Claim("s3", "A", "B", false, 0.5)
        );
        LatentCredibilityEngine engine = new LatentCredibilityEngine();
        Map<LatentCredibilityEngine.ClaimKey, Double> cred = engine.computeCredibility(claims, 1);
        Double val = cred.get(new LatentCredibilityEngine.ClaimKey("A", "B"));
        assertNotNull(val, "Credibility should be calculated");
        assertEquals(1.3, val, 1e-6);
    }
}
