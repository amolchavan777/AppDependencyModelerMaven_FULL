package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class WeightedVotingResolverTest {
    @Test
    public void resolvesUsingWeights() {
        List<Claim> claims = List.of(
            new Claim("s1", "A", "B", true, 1.0),
            new Claim("s2", "A", "B", false, 0.8),
            new Claim("s3", "A", "B", true, 0.5)
        );

        Map<String, Double> weights = new HashMap<>();
        weights.put("s1", 1.0);
        weights.put("s2", 0.2); // lower reliability
        weights.put("s3", 0.5);

        Map<String, Set<String>> result = WeightedVotingResolver.resolve(claims, weights);
        assertNotNull(result.get("A"));
        assertTrue(result.get("A").contains("B"), "A->B should be accepted");
    }
}
