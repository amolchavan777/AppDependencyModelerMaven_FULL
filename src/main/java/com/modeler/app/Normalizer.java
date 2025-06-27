package com.modeler.app;

import java.util.*;
import java.io.*;

public class Normalizer {
    public static List<Claim> collectAllClaims() throws IOException {
        List<Claim> claims = new ArrayList<>();
        claims.addAll(WiresharkAdapter.parse("raw_scanner_data/wireshark.txt"));
        claims.addAll(PsAuxAdapter.parse("raw_scanner_data/ps_aux.txt"));
        claims.addAll(ConfigFileAdapter.parse("raw_scanner_data/config.ini"));
        claims.addAll(ApplicationLogAdapter.parse("raw_scanner_data/application.log"));
        claims.addAll(CodeDependencyAdapter.parse("raw_scanner_data/code_dependencies.txt"));
        claims.addAll(OpenTelemetryAdapter.parse("raw_scanner_data/otel_traces.json"));
        claims.addAll(GitlabCiAdapter.parse("raw_scanner_data/gitlab_pipeline.log"));
        claims.addAll(ApiSpecAdapter.parse("raw_scanner_data/api_spec.yaml"));
        return claims;
    }
}