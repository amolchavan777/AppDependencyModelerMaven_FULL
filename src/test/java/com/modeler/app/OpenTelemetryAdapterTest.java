package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OpenTelemetryAdapterTest {
    @Test
    public void parsesTraceSpans() throws IOException {
        List<Claim> claims = OpenTelemetryAdapter.parse("raw_scanner_data/otel_traces.json");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        assertNotNull(claims.get(0).metadata, "Metadata(span name) should be set");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("WebPortal") && c.toApp.equals("CustomerService"));
        assertTrue(found, "Expected WebPortal -> CustomerService span");
    }
}
