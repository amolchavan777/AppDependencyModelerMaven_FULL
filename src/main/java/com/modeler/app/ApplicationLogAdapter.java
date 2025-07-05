package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Adapter that reads structured application logs to discover runtime
 * service interactions. Log lines containing "connected to" are parsed
 * to infer which applications communicate with each other.
 */
public class ApplicationLogAdapter {

    /**
     * Parse an application log file and return discovered dependency claims.
     * Lines must contain the substring {@code "connected to"} to be considered.
     *
     * @param filePath path to the log file
     * @return list of claims extracted from log messages
     * @throws IOException if the file cannot be read
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("connected to")) {
                    String timestamp = null;
                    if (line.startsWith("[")) {
                        timestamp = line.substring(1, line.indexOf(']')).trim();
                    }
                    String remainder = line.substring(line.indexOf(']') + 2);
                    String severity = remainder.split(":")[0].trim();
                    String rest = remainder.substring(remainder.indexOf(':') + 1).trim();
                    String fromApp = rest.split(" connected")[0].trim();
                    String toApp = rest.split("connected to")[1].split("on")[0].trim();
                    Claim c = new Claim("AppLog", fromApp, toApp, true, 0.8, timestamp, severity);
                    claims.add(c);
                }
            }
        }
        return claims;
    }
}