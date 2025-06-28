package com.modeler.app;

import java.io.*;
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
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("connected to")) {
                    // Example: "INFO: AdminConsole connected to CustomerService"
                    String fromApp = line.split("INFO: ")[1].split(" connected")[0].trim();
                    String toApp = line.split("connected to")[1].split("on")[0].trim();
                    claims.add(new Claim("AppLog", fromApp, toApp, true, 0.8));
                }
            }
        }
        return claims;
    }
}