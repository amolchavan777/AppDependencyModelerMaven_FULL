package com.modeler.app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.*;

/**
 * Adapter for OpenTelemetry trace exports. Each span entry is expected to
 * contain a {@code service} and a {@code targetService} field.
 */
public class OpenTelemetryAdapter {

    /**
     * Read a JSON array of spans and convert them into {@link Claim}s.
     *
     * @param filePath path to a JSON trace dump
     * @return claims derived from the spans
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        JSONArray spans = new JSONArray(content);
        for (int i = 0; i < spans.length(); i++) {
            JSONObject span = spans.getJSONObject(i);
            String from = span.getString("service");
            String to = span.getString("targetService");
            String meta = span.optString("name", null);
            Claim c = new Claim("OpenTelemetry", from, to, true, 0.88, null, meta);
            claims.add(c);
        }
        return claims;
    }
}