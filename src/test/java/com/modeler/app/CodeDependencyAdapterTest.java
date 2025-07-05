package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodeDependencyAdapterTest {
    @Test
    public void parsesCodeDependencies() throws IOException {
        List<Claim> claims = CodeDependencyAdapter.parse("raw_scanner_data/code_dependencies.txt");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("AdminConsole") && c.toApp.equals("CustomerService"));
        assertTrue(found, "Expected AdminConsole -> CustomerService dependency");
    }
}
