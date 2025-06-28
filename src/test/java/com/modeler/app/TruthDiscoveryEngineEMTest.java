package com.modeler.app;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TruthDiscoveryEngineEMTest {
    @Test
    public void convergesAndReturnsDependencies() throws Exception {
        List<Claim> claims = List.of(
            new Claim("s1", "A", "B", true, 1.0),
            new Claim("s2", "A", "B", true, 0.5),
            new Claim("s3", "A", "B", false, 0.5)
        );

        TruthDiscoveryEngineEM engine = new TruthDiscoveryEngineEM();
        engine.runEM(claims);

        Map<String, Set<String>> result = engine.getResult();
        assertNotNull(result.get("A"), "A should have outgoing deps");
        assertTrue(result.get("A").contains("B"), "A -> B should be inferred");

        Field trustField = TruthDiscoveryEngineEM.class.getDeclaredField("sourceTrust");
        trustField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Double> trust = (Map<String, Double>) trustField.get(engine);
        assertTrue(trust.get("s1") > trust.get("s3"), "Positive source should be trusted more");

        Field probField = TruthDiscoveryEngineEM.class.getDeclaredField("claimProbabilities");
        probField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Claim, Double> probs = (Map<Claim, Double>) probField.get(engine);
        assertTrue(probs.values().stream().anyMatch(p -> p > 0.5), "Some claim probability should exceed 0.5");
    }
}
