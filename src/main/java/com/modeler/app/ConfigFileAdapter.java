package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Reads a simple INI-style configuration file. The file is expected to contain
 * a {@code name=} field for the application and a comma-separated
 * {@code dependencies=} list.
 */
public class ConfigFileAdapter {

    /**
     * Parse the configuration file and produce dependency claims.
     *
     * @param filePath path to the INI file
     * @return list of claims defined in the config
     * @throws IOException if the file is not readable
     */
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