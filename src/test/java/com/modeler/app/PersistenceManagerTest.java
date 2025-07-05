package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.util.Collections;

/** Simple test to exercise database initialization. */
public class PersistenceManagerTest {
    @Test
    public void initAndSave() throws Exception {
        try (PersistenceManager pm = new PersistenceManager("jdbc:h2:mem:testdb")) {
            pm.saveClaims(Collections.emptyList());
            pm.saveDependencies(Collections.emptyMap());
        }
    }
}
