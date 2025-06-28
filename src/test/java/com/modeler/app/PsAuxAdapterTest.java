package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PsAuxAdapterTest {
    @Test
    public void generatesAllPairs() throws IOException {
        List<Claim> claims = PsAuxAdapter.parse("raw_scanner_data/ps_aux.txt");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("SearchService") && c.toApp.equals("FileStorage"));
        assertTrue(found, "Expected SearchService -> FileStorage pair");
    }
}
