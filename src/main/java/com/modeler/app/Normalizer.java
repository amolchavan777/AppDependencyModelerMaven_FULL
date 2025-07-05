package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import com.modeler.app.RouterLogAdapter;
import java.util.*;
import java.io.*;

/**
 * Utility for invoking every adapter and aggregating their results into a
 * single list of normalized claims.
 */
public class Normalizer {

    /**
     * Collect dependency claims from all available adapters.
     *
     * @return a unified list of claims
     */
    public static List<Claim> collectAllClaims() throws IOException {
        List<Claim> claims = new ArrayList<>();
        claims.addAll(WiresharkAdapter.parse("raw_scanner_data/wireshark.txt"));
        claims.addAll(PsAuxAdapter.parse("raw_scanner_data/ps_aux.txt"));
        claims.addAll(ConfigFileAdapter.parse("raw_scanner_data/config.ini"));
        claims.addAll(ApplicationLogAdapter.parse("raw_scanner_data/application.log"));
        claims.addAll(RouterLogAdapter.parse("raw_scanner_data/router.log"));
        claims.addAll(CodeDependencyAdapter.parse("raw_scanner_data/code_dependencies.txt"));
        // Database mappings provide 1:1 relationships for negative claim testing
        claims.addAll(DatabaseConfigAdapter.parse("raw_scanner_data/db_config.txt"));
        claims.addAll(OpenTelemetryAdapter.parse("raw_scanner_data/otel_traces.json"));
        claims.addAll(GitlabCiAdapter.parse("raw_scanner_data/gitlab_pipeline.log"));
        claims.addAll(ApiSpecAdapter.parse("raw_scanner_data/api_spec.yaml"));
        return claims;
    }

    /** Mapping of alternate names to canonical service names. */
    public static Map<String,String> getAliasMap() {
        Map<String,String> map = new LinkedHashMap<>();
        map.put("web-tier", "WebPortal");
        map.put("edge-api", "AuthGateway");
        map.put("messaging-queue", "NotificationEngine");
        map.put("analytics-tier", "AnalyticsEngine");
        map.put("admin-tier", "AdminConsole");
        return map;
    }

    /**
     * Apply the alias map to produce normalized claims.
     */
    public static List<Claim> normalizeClaims(List<Claim> raw) {
        Map<String,String> map = getAliasMap();
        List<Claim> norm = new ArrayList<>();
        for (Claim c : raw) {
            String from = map.getOrDefault(c.fromApp, c.fromApp);
            String to = map.getOrDefault(c.toApp, c.toApp);
            norm.add(new Claim(c.source, from, to, c.exists, c.confidence));
        }
        return norm;
    }
}