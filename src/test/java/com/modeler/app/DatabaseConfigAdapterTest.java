package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConfigAdapterTest {
    @Test
    public void parsesDatabaseMappings() throws IOException {
        List<Claim> claims = DatabaseConfigAdapter.parse("raw_scanner_data/db_config.txt");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        boolean found = claims.stream()
                .anyMatch(c -> c.fromApp.equals("WebPortal") && c.toApp.equals("WebsiteDB") && c.type.equals("default_db"));
        assertTrue(found, "Expected WebPortal -> WebsiteDB default_db claim");
    }
}
