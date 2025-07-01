package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WiresharkAdapterTest {
    @Test
    public void parsesConnections() throws IOException {
        List<Claim> claims = WiresharkAdapter.parse("raw_scanner_data/wireshark.txt");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        Claim first = claims.get(0);
        assertNotNull(first.timestamp, "Timestamp should be set");
        assertNotNull(first.metadata, "Metadata should be set");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("MonitoringAgent") && c.toApp.equals("CRMSystem"));
        assertTrue(found, "Expected MonitoringAgent -> CRMSystem connection");
    }
}
