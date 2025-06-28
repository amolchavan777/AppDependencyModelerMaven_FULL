package com.modeler.app;

import java.io.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

public class ApiSpecAdapter {
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(options));
        try (InputStream input = new FileInputStream(new File(filePath))) {
            Map<String, Object> data = yaml.load(input);
            String fromApp = ((Map<String, String>) data.get("info")).get("title");
            Map<String, Object> paths = (Map<String, Object>) data.get("paths");
            for (Object op : paths.values()) {
                Map<String, Object> methods = (Map<String, Object>) op;
                for (Object methodBlock : methods.values()) {
                    if (methodBlock instanceof Map<?, ?> methodMap && methodMap.containsKey("x-calls")) {
                        List<String> calls = (List<String>) methodMap.get("x-calls");
                        for (String dep : calls) {
                            claims.add(new Claim("ApiSpec", fromApp, dep, true, 0.9));
                        }
                    }
                }
            }
        }
        return claims;
    }
}