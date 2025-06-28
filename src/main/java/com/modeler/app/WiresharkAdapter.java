package com.modeler.app;

import java.io.*;
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
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("IP")) {
                    String[] parts = line.split(" ");
                    String fromApp = parts[2].split("\\.|:")[0];
                    String toApp = parts[4].split("\\.|:")[0];
                    claims.add(new Claim("Wireshark", fromApp, toApp, true, 0.85));
                }
            }
        }
        return claims;
    }
}