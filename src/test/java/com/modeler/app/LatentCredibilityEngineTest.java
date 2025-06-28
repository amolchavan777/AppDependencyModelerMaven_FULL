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
}
