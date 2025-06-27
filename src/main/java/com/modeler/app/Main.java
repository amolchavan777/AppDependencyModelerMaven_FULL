package com.modeler.app;

import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        List<Claim> claims = Normalizer.collectAllClaims();
        System.out.println("✅ Normalized Application Dependency Claims:");
        for (Claim c : claims) System.out.println(c);

        LatentCredibilityEngine engine = new LatentCredibilityEngine();
        Map<String, Set<String>> result = engine.run(claims, 10);

        System.out.println("\n✅ Final Application Dependency Model:");
        for (var entry : result.entrySet())
            System.out.println(entry.getKey() + " depends on " + entry.getValue());

        ArchimateExporter.export(result, "output/archimate_model.xml");
        System.out.println("\n📄 Archimate model exported to output/archimate_model.xml");
    }
}