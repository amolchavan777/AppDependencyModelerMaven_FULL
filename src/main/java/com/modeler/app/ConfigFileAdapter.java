package com.modeler.app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigFileAdapter {
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        String fromApp = "";
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("name")) {
                    fromApp = line.split("=")[1].trim();
                } else if (line.startsWith("dependencies")) {
                    String[] deps = line.split("=")[1].split(",");
                    for (String dep : deps) {
                        claims.add(new Claim("ConfigFile", fromApp, dep.trim(), true, 0.95));
                    }
                }
            }
        }
        return claims;
    }
}