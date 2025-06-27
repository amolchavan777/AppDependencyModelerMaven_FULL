package com.modeler.app;

import java.io.*;
import java.util.*;

public class CodeDependencyAdapter {
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("->")) {
                    String[] parts = line.split("->");
                    String fromApp = parts[0].trim();
                    String toApp = parts[1].trim();
                    claims.add(new Claim("Code", fromApp, toApp, true, 0.9));
                }
            }
        }
        return claims;
    }
}