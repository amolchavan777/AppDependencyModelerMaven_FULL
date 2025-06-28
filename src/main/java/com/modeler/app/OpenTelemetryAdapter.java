package com.modeler.app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.*;

public class OpenTelemetryAdapter {
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        JSONArray spans = new JSONArray(content);
        for (int i = 0; i < spans.length(); i++) {
            JSONObject span = spans.getJSONObject(i);
            String from = span.getString("service");
            String to = span.getString("targetService");
            claims.add(new Claim("OpenTelemetry", from, to, true, 0.88));
        }
        return claims;
    }
}