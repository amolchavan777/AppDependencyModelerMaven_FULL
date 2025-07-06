package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Manages source reliability metrics and updates them dynamically based on claim validation
 * and agreement with other sources. Provides weighted scoring for truth discovery algorithms.
 */
public class SourceReliabilityManager {
    
    // Source reliability metrics
    private final Map<String, SourceMetrics> sourceMetrics = new HashMap<>();
    
    // Extraction method classifications
    public enum ExtractionMethod {
        LOG_ANALYSIS("Log Analysis"),
        NETWORK_CAPTURE("Network Capture"), 
        CONFIG_PARSING("Configuration Parsing"),
        MANUAL_INPUT("Manual Input"),
        API_DISCOVERY("API Discovery"),
        CODE_ANALYSIS("Code Analysis"),
        UNKNOWN("Unknown");
        
        private final String displayName;
        
        ExtractionMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static ExtractionMethod fromSource(String source) {
            if (source == null) return UNKNOWN;
            String lower = source.toLowerCase();
            
            if (lower.contains("log") || lower.contains("gitlab") || lower.contains("router")) {
                return LOG_ANALYSIS;
            } else if (lower.contains("wireshark") || lower.contains("network") || lower.contains("capture")) {
                return NETWORK_CAPTURE;
            } else if (lower.contains("config") || lower.contains("ini") || lower.contains("db_config")) {
                return CONFIG_PARSING;
            } else if (lower.equals("manual") || lower.contains("user")) {
                return MANUAL_INPUT;
            } else if (lower.contains("api") || lower.contains("otel") || lower.contains("telemetry")) {
                return API_DISCOVERY;
            } else if (lower.contains("code") || lower.contains("dependency")) {
                return CODE_ANALYSIS;
            }
            return UNKNOWN;
        }
    }
    
    /**
     * Metrics tracked for each source
     */
    public static class SourceMetrics {
        public String sourceName;
        public ExtractionMethod extractionMethod;
        public double reliabilityScore = 0.5; // Initial neutral score
        public int totalClaims = 0;
        public int validatedClaims = 0;
        public int agreementCount = 0;
        public int disagreementCount = 0;
        public long lastUpdate = System.currentTimeMillis();
        public double accuracy = 0.0;
        public double coverage = 0.0;
        public double consistency = 0.0;
        
        public SourceMetrics(String sourceName) {
            this.sourceName = sourceName;
            this.extractionMethod = ExtractionMethod.fromSource(sourceName);
        }
        
        public void updateReliability() {
            // Calculate accuracy (percentage of claims that were validated)
            if (totalClaims > 0) {
                accuracy = (double) validatedClaims / totalClaims;
            }
            
            // Calculate consistency (agreement ratio)
            if (agreementCount + disagreementCount > 0) {
                consistency = (double) agreementCount / (agreementCount + disagreementCount);
            }
            
            // Overall reliability combines accuracy and consistency
            // Weight accuracy more heavily for sources with many claims
            double accuracyWeight = Math.min(0.8, 0.3 + (totalClaims * 0.01));
            double consistencyWeight = 1.0 - accuracyWeight;
            
            reliabilityScore = (accuracy * accuracyWeight) + (consistency * consistencyWeight);
            
            // Apply extraction method modifier
            reliabilityScore *= getExtractionMethodReliability();
            
            // Ensure score stays in valid range
            reliabilityScore = Math.max(0.1, Math.min(1.0, reliabilityScore));
            
            lastUpdate = System.currentTimeMillis();
        }
        
        private double getExtractionMethodReliability() {
            switch (extractionMethod) {
                case NETWORK_CAPTURE: return 0.95;
                case LOG_ANALYSIS: return 0.85;
                case CODE_ANALYSIS: return 0.90;
                case CONFIG_PARSING: return 0.80;
                case API_DISCOVERY: return 0.85;
                case MANUAL_INPUT: return 0.70;
                case UNKNOWN: return 0.60;
                default: return 0.75;
            }
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("sourceName", sourceName);
            map.put("extractionMethod", extractionMethod.getDisplayName());
            map.put("reliabilityScore", Math.round(reliabilityScore * 1000.0) / 1000.0);
            map.put("totalClaims", totalClaims);
            map.put("validatedClaims", validatedClaims);
            map.put("agreementCount", agreementCount);
            map.put("disagreementCount", disagreementCount);
            map.put("accuracy", Math.round(accuracy * 1000.0) / 1000.0);
            map.put("consistency", Math.round(consistency * 1000.0) / 1000.0);
            map.put("lastUpdate", lastUpdate);
            return map;
        }
    }
    
    /**
     * Initialize or update metrics for a source
     */
    public synchronized void initializeSource(String sourceName) {
        if (!sourceMetrics.containsKey(sourceName)) {
            sourceMetrics.put(sourceName, new SourceMetrics(sourceName));
        }
    }
    
    /**
     * Add a new claim and update source metrics
     */
    public synchronized void addClaim(Claim claim) {
        String source = claim.source;
        initializeSource(source);
        
        SourceMetrics metrics = sourceMetrics.get(source);
        metrics.totalClaims++;
        
        // Update reliability after each claim addition
        updateSourceReliability(source);
    }
    
    /**
     * Update source reliability based on claim validation and cross-source agreement
     */
    public synchronized void updateAllSourceReliability(List<Claim> allClaims) {
        // Reset counters
        for (SourceMetrics metrics : sourceMetrics.values()) {
            metrics.agreementCount = 0;
            metrics.disagreementCount = 0;
        }
        
        // Group claims by dependency pair
        Map<String, List<Claim>> claimsByDependency = new HashMap<>();
        for (Claim claim : allClaims) {
            String key = claim.fromApp + "|" + claim.toApp;
            claimsByDependency.computeIfAbsent(key, k -> new ArrayList<>()).add(claim);
        }
        
        // Calculate agreement/disagreement for each source
        for (List<Claim> depClaims : claimsByDependency.values()) {
            if (depClaims.size() <= 1) continue;
            
            for (Claim claim1 : depClaims) {
                initializeSource(claim1.source);
                SourceMetrics metrics1 = sourceMetrics.get(claim1.source);
                
                for (Claim claim2 : depClaims) {
                    if (!claim1.source.equals(claim2.source)) {
                        if (claim1.exists == claim2.exists) {
                            metrics1.agreementCount++;
                        } else {
                            metrics1.disagreementCount++;
                        }
                    }
                }
            }
        }
        
        // Update reliability scores for all sources
        for (SourceMetrics metrics : sourceMetrics.values()) {
            metrics.updateReliability();
        }
    }
    
    /**
     * Update reliability for a specific source
     */
    public synchronized void updateSourceReliability(String sourceName) {
        SourceMetrics metrics = sourceMetrics.get(sourceName);
        if (metrics != null) {
            metrics.updateReliability();
        }
    }
    
    /**
     * Get reliability score for a source
     */
    public double getReliabilityScore(String sourceName) {
        SourceMetrics metrics = sourceMetrics.get(sourceName);
        return metrics != null ? metrics.reliabilityScore : 0.5;
    }
    
    /**
     * Get all source metrics
     */
    public Map<String, SourceMetrics> getAllMetrics() {
        return new HashMap<>(sourceMetrics);
    }
    
    /**
     * Get reliability scores as weights for truth discovery
     */
    public Map<String, Double> getReliabilityWeights() {
        Map<String, Double> weights = new HashMap<>();
        for (Map.Entry<String, SourceMetrics> entry : sourceMetrics.entrySet()) {
            weights.put(entry.getKey(), entry.getValue().reliabilityScore);
        }
        return weights;
    }
    
    /**
     * Mark claims as validated (used by truth discovery results)
     */
    public synchronized void markClaimsValidated(List<Claim> validatedClaims) {
        for (Claim claim : validatedClaims) {
            SourceMetrics metrics = sourceMetrics.get(claim.source);
            if (metrics != null) {
                metrics.validatedClaims++;
                metrics.updateReliability();
            }
        }
    }
    
    /**
     * Print detailed reliability report
     */
    public void printReliabilityReport() {
        System.out.println("\n📊 Source Reliability Report");
        System.out.println("========================================");
        
        List<SourceMetrics> sortedMetrics = new ArrayList<>(sourceMetrics.values());
        sortedMetrics.sort((a, b) -> Double.compare(b.reliabilityScore, a.reliabilityScore));
        
        for (SourceMetrics metrics : sortedMetrics) {
            System.out.printf("🔍 %s (%s)%n", metrics.sourceName, metrics.extractionMethod.getDisplayName());
            System.out.printf("   Reliability: %.3f | Claims: %d | Validated: %d%n", 
                            metrics.reliabilityScore, metrics.totalClaims, metrics.validatedClaims);
            System.out.printf("   Accuracy: %.3f | Consistency: %.3f%n", 
                            metrics.accuracy, metrics.consistency);
            System.out.printf("   Agreements: %d | Disagreements: %d%n", 
                            metrics.agreementCount, metrics.disagreementCount);
            System.out.println();
        }
    }
}
