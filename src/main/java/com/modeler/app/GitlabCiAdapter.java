package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Parses GitLab CI/CD pipeline logs. Lines containing a dependency notation
 * like {@code [Stage] ServiceA -> ServiceB} are converted into claims.
 */
public class GitlabCiAdapter {

    /**
     * Extract dependency claims from a GitLab pipeline log file.
     *
     * @param filePath path to the log
     * @return list of claims from the pipeline output
     * @throws IOException if the file cannot be read
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("->")) {
                    String[] parts = line.split("->");
                    String from = parts[0].split("]")[1].trim();
                    String to = parts[1].trim();
                    String stage = line.substring(line.indexOf('[') + 1, line.indexOf(']')).trim();
                    Claim c = new Claim("GitlabCI", from, to, true, 0.75, null, stage);
                    claims.add(c);
                }
            }
        }
        return claims;
    }
}