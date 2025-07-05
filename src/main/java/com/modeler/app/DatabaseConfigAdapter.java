package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Parses a simple mapping of applications to their primary databases.
 * Each line should be of the form {@code App -> Database}.
 * The relationship type is recorded as {@code "default_db"} so that
 * {@link NegativeClaimGenerator} can generate absence claims when
 * other sources do not mention the same database dependency.
 */
public class DatabaseConfigAdapter {

    /**
     * Parse a database mapping file into {@link Claim} objects of type
     * {@code "default_db"}.
     *
     * @param filePath path to the mapping file
     * @return list of database dependency claims
     * @throws IOException if reading fails
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("->")) {
                    String[] parts = line.split("->");
                    String app = parts[0].trim();
                    String db = parts[1].trim();
                    claims.add(new Claim("DbConfig", app, db, "default_db", true, 0.99));
                }
            }
        }
        return claims;
    }
}
