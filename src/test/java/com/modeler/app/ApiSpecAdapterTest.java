package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApiSpecAdapterTest {
    @Test
    public void parsesYamlCalls() throws IOException {
        List<Claim> claims = ApiSpecAdapter.parse("raw_scanner_data/api_spec.yaml");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("AuthGateway") && c.toApp.equals("CustomerService"));
        assertTrue(found, "Expected AuthGateway -> CustomerService call");
    }
}
