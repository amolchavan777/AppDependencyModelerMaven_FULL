package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NegativeClaimGeneratorTest {

    @Test
    public void respectsMultiplicityRules() {
        List<Claim> claims = List.of(
                new Claim("s1", "A", "B", "default_db", true, 1.0),
                new Claim("s2", "A", "D", "calls", true, 1.0)
        );

        MultiplicityClassifier classifier = new MultiplicityClassifier(MultiplicityClassifier.defaultRules());
        List<Claim> negatives = NegativeClaimGenerator.generate(claims, classifier);

        assertEquals(1, negatives.size(), "Only default_db should produce negatives");
        Claim neg = negatives.get(0);
        assertEquals("s2", neg.source);
        assertEquals("A", neg.fromApp);
        assertEquals("B", neg.toApp);
        assertFalse(neg.exists);
        assertEquals("default_db", neg.type);

    }
}
