package com.modeler.app;
import com.enterprise.dependency.model.core.Claim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringClaimMergerTest {
    @Test
    public void defaultFormatterFlattensAllFields() {
        Claim c = new Claim("s1", "A", "B", true, 0.9, "t1", "m1");
        StringClaimMerger merger = StringClaimMerger.defaultMerger();
        String result = merger.merge(c);
        assertEquals("A->B|exists=true|confidence=0.9|source=s1|timestamp=t1|metadata=m1", result);
    }

    @Test
    public void customFormatterIsUsed() {
        Claim c = new Claim("s1", "A", "B", true, 1.0);
        StringClaimMerger merger = new StringClaimMerger(cl -> cl.fromApp + ":" + cl.toApp);
        String result = merger.merge(c);
        assertEquals("A:B", result);
    }
}
