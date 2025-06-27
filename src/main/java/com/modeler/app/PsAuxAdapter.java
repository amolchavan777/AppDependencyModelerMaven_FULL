package com.modeler.app;

import java.io.*;
import java.util.*;

public class PsAuxAdapter {
    public static List<Claim> parse(String filePath) throws IOException {
        Set<String> apps = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("/usr/bin/")) {
                    String app = line.substring(line.lastIndexOf("/") + 1).trim();
                    apps.add(app);
                }
            }
        }
        List<Claim> claims = new ArrayList<>();
        for (String a : apps) {
            for (String b : apps) {
                if (!a.equals(b)) {
                    claims.add(new Claim("ps_aux", a, b, true, 0.6));
                }
            }
        }
        return claims;
    }
}