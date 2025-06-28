package com.modeler.app;

import java.io.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * Adapter that extracts application dependencies from an OpenAPI/Swagger
 * specification. Each HTTP method may declare a custom {@code x-calls}
 * array describing which downstream services it invokes.
 */
public class ApiSpecAdapter {

    /**
     * Parse an OpenAPI specification file and convert any {@code x-calls}
     * entries into {@link Claim} objects.
     *
     * @param filePath path to a YAML API specification
     * @return list of claims representing service-to-service calls
     * @throws IOException if the file cannot be read
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();

        // Use SnakeYAML in safe mode to parse the YAML document
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(options));

        try (InputStream input = new FileInputStream(new File(filePath))) {
            Map<String, Object> data = yaml.load(input);
            String fromApp = ((Map<String, String>) data.get("info")).get("title");

            // Iterate through each path/method block to find custom calls
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