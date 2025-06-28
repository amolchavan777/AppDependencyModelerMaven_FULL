package com.modeler.app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GitlabCiAdapter {
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("->")) {
                    String[] parts = line.split("->");
                    String from = parts[0].split("]")[1].trim();
                    String to = parts[1].trim();
                    claims.add(new Claim("GitlabCI", from, to, true, 0.75));
                }
            }
        }
        return claims;
    }
}