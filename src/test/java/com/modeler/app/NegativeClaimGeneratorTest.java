package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NegativeClaimGeneratorTest {
    @Test
    public void generatesMissingNegativesPerSource() {
        List<Claim> claims = List.of(
                new Claim("s1", "A", "B", true, 1.0),
                new Claim("s2", "A", "C", true, 1.0),
                new Claim("s1", "B", "D", true, 1.0)
        );

        List<Claim> negatives = NegativeClaimGenerator.generate(claims);

        assertEquals(2, negatives.size(), "Two negatives should be generated");
        boolean hasS1AC = negatives.stream().anyMatch(c ->
                c.source.equals("s1") && c.fromApp.equals("A") && c.toApp.equals("C") && !c.exists);
        boolean hasS2AB = negatives.stream().anyMatch(c ->
                c.source.equals("s2") && c.fromApp.equals("A") && c.toApp.equals("B") && !c.exists);
        assertTrue(hasS1AC, "Missing negative for s1 A->C");
        assertTrue(hasS2AB, "Missing negative for s2 A->B");
    }
}
