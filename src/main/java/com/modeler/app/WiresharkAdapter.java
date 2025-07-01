package com.modeler.app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Parses simplified Wireshark packet logs to infer network-based
 * application interactions.
 */
public class WiresharkAdapter {

    /**
     * Convert packet capture text output into dependency claims.
     *
     * @param filePath path to the packet capture file
     * @return list of claims representing observed network flows
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("IP")) {
                    String[] parts = line.split(" ");
                    String timestamp = parts[0];
                    String fromApp = parts[2].split("\\.|:")[0];
                    String toApp = parts[4].split("\\.|:")[0];
                    StringBuilder meta = new StringBuilder();
                    for (int i = 5; i < parts.length; i++) meta.append(parts[i]).append(" ");
                    String metadata = meta.toString().trim();
                    Claim c = new Claim("Wireshark", fromApp, toApp, true, 0.85, timestamp, metadata);
                    claims.add(c);
                }
            }
        }
        return claims;
    }
}