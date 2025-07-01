package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GitlabCiAdapterTest {
    @Test
    public void parsesPipelineLog() throws IOException {
        List<Claim> claims = GitlabCiAdapter.parse("raw_scanner_data/gitlab_pipeline.log");
        assertFalse(claims.isEmpty(), "Claims should not be empty");
        Claim first = claims.get(0);
        assertNotNull(first.metadata, "Metadata(stage) should be set");
        boolean found = claims.stream()
            .anyMatch(c -> c.fromApp.equals("WebPortal") && c.toApp.equals("web-tier"));
        assertTrue(found, "Expected WebPortal -> web-tier deployment");
    }
}
