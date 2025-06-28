package com.modeler.app;

import java.io.*;
import java.util.*;

/**
 * Parses a simple textual representation of source code dependencies.
 * Each line in the file is expected to be of the form
 * {@code FromApp -> ToApp}.
 */
public class CodeDependencyAdapter {

    /**
     * Convert a dependency text file into {@link Claim} objects.
     *
     * @param filePath path to the dependency listing
     * @return list of discovered dependencies
     * @throws IOException if reading fails
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("->")) {
                    String[] parts = line.split("->");
                    String fromApp = parts[0].trim();
                    String toApp = parts[1].trim();
                    claims.add(new Claim("Code", fromApp, toApp, true, 0.9));
                }
            }
        }
        return claims;
    }
}