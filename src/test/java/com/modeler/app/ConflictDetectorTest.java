package com.modeler.app;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConflictDetectorTest {
    @Test
    public void identifiesConflictedGroups() {
        List<Claim> claims = List.of(
            new Claim("s1", "A", "B", true, 1.0),
            new Claim("s2", "A", "B", false, 1.0),
            new Claim("s3", "A", "C", true, 1.0)
        );

        var groups = ConflictDetector.detect(claims);
        assertEquals(2, groups.size(), "Two claim groups expected");

        var conflicts = ConflictDetector.findConflicts(claims);
        assertEquals(1, conflicts.size(), "One conflict expected");
        ConflictDetector.ConflictGroup g = conflicts.get(0);
        assertEquals("A", g.pair.from);
        assertEquals("B", g.pair.to);
        assertTrue(g.conflicted, "Group should be marked conflicted");
    }
}
