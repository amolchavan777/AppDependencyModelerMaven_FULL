package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MajorityVotingResolverTest {
    @Test
    public void acceptsWhenPositivesWin() {
        List<Claim> claims = List.of(
            new Claim("s1", "A", "B", true, 1.0),
            new Claim("s2", "A", "B", false, 1.0),
            new Claim("s3", "A", "B", true, 1.0)
        );
        Map<String, Set<String>> result = MajorityVotingResolver.resolve(claims);
        assertNotNull(result.get("A"));
        assertTrue(result.get("A").contains("B"));
    }

    @Test
    public void rejectsWhenNegativesWin() {
        List<Claim> claims = List.of(
            new Claim("s1", "A", "B", false, 1.0),
            new Claim("s2", "A", "B", false, 1.0),
            new Claim("s3", "A", "B", true, 1.0)
        );
        Map<String, Set<String>> result = MajorityVotingResolver.resolve(claims);
        assertTrue(result.getOrDefault("A", Collections.emptySet()).isEmpty());
    }
}
