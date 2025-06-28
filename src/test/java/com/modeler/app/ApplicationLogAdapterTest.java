package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationLogAdapterTest {
    @Test
    public void parsesConnections() throws IOException {
        List<Claim> claims = ApplicationLogAdapter.parse("raw_scanner_data/application.log");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("MobileClient") && c.toApp.equals("FileStorage"));
        assertTrue(found, "Expected MobileClient -> FileStorage connection");
    }
}
