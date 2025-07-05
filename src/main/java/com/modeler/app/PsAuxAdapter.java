package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Adapter that infers potential interactions by scanning the output of
 * {@code ps aux}. Every executable found under {@code /usr/bin/} is assumed to
 * potentially communicate with every other discovered executable.
 */
public class PsAuxAdapter {

    /**
     * Parse a ps aux snapshot and generate fully connected claims.
     */
    public static List<Claim> parse(String filePath) throws IOException {
        Set<String> apps = new HashSet<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("/usr/bin/")) {
                    String app = line.substring(line.lastIndexOf("/") + 1).trim();
                    apps.add(app);
                }
            }
        }
        List<Claim> claims = new ArrayList<>();
        for (String a : apps) {
            for (String b : apps) {
                if (!a.equals(b)) {
                    claims.add(new Claim("ps_aux", a, b, true, 0.6));
                }
            }
        }
        return claims;
    }
}