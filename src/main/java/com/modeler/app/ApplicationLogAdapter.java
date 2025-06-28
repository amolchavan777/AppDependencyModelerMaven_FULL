package com.modeler.app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ApplicationLogAdapter {
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("connected to")) {
                    String fromApp = line.split("INFO: ")[1].split(" connected")[0].trim();
                    String toApp = line.split("connected to")[1].split("on")[0].trim();
                    claims.add(new Claim("AppLog", fromApp, toApp, true, 0.8));
                }
            }
        }
        return claims;
    }
}