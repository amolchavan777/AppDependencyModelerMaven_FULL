package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClaimIdentityResolverTest {
    @Test
    public void aliasesShareCanonicalId() {
        List<Claim> raw = List.of(
            new Claim("s1", "web-tier", "DB", true, 1.0),
            new Claim("s2", "WebPortal", "DB", true, 1.0),
            new Claim("s3", "Other", "DB", true, 1.0)
        );
        Map<String,String> alias = new HashMap<>();
        alias.put("web-tier", "WebPortal");

        ClaimIdentityResolver resolver = new ClaimIdentityResolver(alias);
        List<ClaimIdentityResolver.ResolvedClaim> resolved = resolver.resolve(raw);

        Map<String,String> idBySource = new HashMap<>();
        for (ClaimIdentityResolver.ResolvedClaim rc : resolved) {
            idBySource.put(rc.claim().source, rc.id());
            if (rc.claim().source.equals("s1")) {
                assertEquals("WebPortal", rc.from(), "Alias should be normalized");
            }
        }

        assertEquals(idBySource.get("s1"), idBySource.get("s2"), "Aliases must share ID");
        assertNotEquals(idBySource.get("s1"), idBySource.get("s3"), "Different pair => different ID");
    }
}
