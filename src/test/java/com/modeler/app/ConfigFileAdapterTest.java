package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigFileAdapterTest {
    @Test
    public void parsesDependencies() throws IOException {
        List<Claim> claims = ConfigFileAdapter.parse("raw_scanner_data/config.ini");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("AuthGateway") && c.toApp.equals("WebPortal"));
        assertTrue(found, "Expected AuthGateway -> WebPortal dependency");
    }
}
