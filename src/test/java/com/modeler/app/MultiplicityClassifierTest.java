package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MultiplicityClassifierTest {
    @Test
    public void classifiesConfiguredTypes() {
        MultiplicityClassifier classifier = new MultiplicityClassifier(
                Map.of("calls", Multiplicity.ONE_TO_MANY,
                       "default_db", Multiplicity.ONE_TO_ONE));

        assertEquals(Multiplicity.ONE_TO_ONE, classifier.classify("default_db"));
        assertEquals(Multiplicity.ONE_TO_MANY, classifier.classify("calls"));
        // Unknown types fall back to ONE_TO_MANY
        assertEquals(Multiplicity.ONE_TO_MANY, classifier.classify("other"));
    }
}
