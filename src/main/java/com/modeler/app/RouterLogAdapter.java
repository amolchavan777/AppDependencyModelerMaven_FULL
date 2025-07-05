package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.sources.RouterLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that parses router access logs to extract dependency claims.
 */
public class RouterLogAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterLogAdapter.class);

    /**
     * Parse a router log file.
     *
     * @param filePath path to the log file
     * @return list of normalized claims
     * @throws IOException if reading fails
     */
    public static List<Claim> parse(String filePath) throws IOException {
        List<Claim> claims = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                RouterLogEntry.parse(line).ifPresent(entry -> {
                    Claim c = new Claim(
                            "RouterLog",
                            entry.getSourceIp(),
                            entry.getTargetIp(),
                            "network",
                            true,
                            0.9,
                            entry.getTimestamp().toString(),
                            line
                    );
                    claims.add(c);
                });
            }
        }
        LOGGER.info("Parsed {} router log entries", claims.size());
        return claims;
    }
}
