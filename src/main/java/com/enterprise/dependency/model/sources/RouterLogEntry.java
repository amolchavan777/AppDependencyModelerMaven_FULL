package com.enterprise.dependency.model.sources;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Represents a single parsed router log entry.
 */
public class RouterLogEntry {
    private final LocalDateTime timestamp;
    private final String sourceIp;
    private final String targetIp;
    private final int targetPort;
    private final String method;
    private final String path;

    private RouterLogEntry(LocalDateTime timestamp, String sourceIp, String targetIp,
                           int targetPort, String method, String path) {
        this.timestamp = timestamp;
        this.sourceIp = sourceIp;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.method = method;
        this.path = path;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSourceIp() { return sourceIp; }
    public String getTargetIp() { return targetIp; }
    public int getTargetPort() { return targetPort; }
    public String getMethod() { return method; }
    public String getPath() { return path; }

    /**
     * Attempt to parse a router log line.
     * Expected formats include:
     * {@code 2024-07-04 10:30:45 [INFO] 192.168.1.100 -> 192.168.1.200:8080 GET /api users 200 125ms}
     *
     * @param line raw log line
     * @return optional entry if the line could be parsed
     */
    public static Optional<RouterLogEntry> parse(String line) {
        try {
            String[] parts = line.split(" ", 6);
            LocalDateTime ts = LocalDateTime.parse(parts[0] + " " + parts[1],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String from = parts[3];
            String to = parts[5];
            if (!to.contains(":")) return Optional.empty();
            String[] targetParts = to.split(":" ,2);
            String ip = targetParts[0];
            int port = Integer.parseInt(targetParts[1]);
            String method = parts.length > 6 ? parts[6] : "";
            String path = parts.length > 7 ? parts[7] : "";
            return Optional.of(new RouterLogEntry(ts, from, ip, port, method, path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
