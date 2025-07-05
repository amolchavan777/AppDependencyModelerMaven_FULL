package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouterLogAdapterTest {
    @Test
    public void parsesRouterLogs() throws IOException {
        List<Claim> claims = RouterLogAdapter.parse("raw_scanner_data/router.log");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        Claim first = claims.get(0);
        assertEquals("192.168.1.100", first.fromApp);
        assertEquals("192.168.1.200", first.toApp);
    }
}
