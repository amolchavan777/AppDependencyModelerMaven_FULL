package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

/**
 * Enhanced JSON exporter that includes provenance metadata and source reliability information
 */
public class EnhancedJsonExporter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Export dependency graph with provenance and reliability data
     */
    public static void export(Map<String, Set<String>> dependencies, 
                            List<Claim> claims,
                            SourceReliabilityManager reliabilityManager,
                            String filename) throws IOException {
        
        Map<String, Object> output = new HashMap<>();
        
        // Basic dependency information
        output.put("dependencies", dependencies);
        output.put("metadata", createMetadata());
        
        // Enhanced provenance information
        List<Map<String, Object>> claimsList = new ArrayList<>();
        for (Claim claim : claims) {
            Map<String, Object> claimMap = new HashMap<>();
            claimMap.put("fromApp", claim.fromApp);
            claimMap.put("toApp", claim.toApp);
            claimMap.put("source", claim.source);
            claimMap.put("exists", claim.exists);
            claimMap.put("confidence", claim.confidence);
            claimMap.put("type", claim.type);
            
            // Add provenance if available
            if (claim instanceof EnhancedClaim) {
                EnhancedClaim enhanced = (EnhancedClaim) claim;
                claimMap.putAll(enhanced.getProvenanceMap());
            } else {
                // Fallback for standard claims
                claimMap.put("timestamp", System.currentTimeMillis());
                claimMap.put("extractionMethod", SourceReliabilityManager.ExtractionMethod.fromSource(claim.source).getDisplayName());
            }
            
            claimsList.add(claimMap);
        }
        output.put("claims", claimsList);
        
        // Source reliability information
        Map<String, Object> reliability = new HashMap<>();
        for (Map.Entry<String, SourceReliabilityManager.SourceMetrics> entry : reliabilityManager.getAllMetrics().entrySet()) {
            reliability.put(entry.getKey(), entry.getValue().toMap());
        }
        output.put("sourceReliability", reliability);
        
        // Statistics
        output.put("statistics", createStatistics(dependencies, claims, reliabilityManager));
        
        // Write to file
        try (FileWriter writer = new FileWriter(filename)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, output);
        }
    }
    
    private static Map<String, Object> createMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exportedAt", System.currentTimeMillis());
        metadata.put("exportedAtFormatted", new Date().toString());
        metadata.put("version", "2.0");
        metadata.put("includes", Arrays.asList("dependencies", "claims", "provenance", "reliability"));
        return metadata;
    }
    
    private static Map<String, Object> createStatistics(Map<String, Set<String>> dependencies, 
                                                       List<Claim> claims,
                                                       SourceReliabilityManager reliabilityManager) {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalApplications", dependencies.size());
        stats.put("totalDependencies", dependencies.values().stream().mapToInt(Set::size).sum());
        stats.put("totalClaims", claims.size());
        stats.put("totalSources", reliabilityManager.getAllMetrics().size());
        
        // Source breakdown
        Map<String, Integer> sourceClaimCounts = new HashMap<>();
        Map<String, String> sourceExtractionMethods = new HashMap<>();
        for (Claim claim : claims) {
            sourceClaimCounts.merge(claim.source, 1, Integer::sum);
            sourceExtractionMethods.put(claim.source, 
                SourceReliabilityManager.ExtractionMethod.fromSource(claim.source).getDisplayName());
        }
        stats.put("claimsBySource", sourceClaimCounts);
        stats.put("extractionMethods", sourceExtractionMethods);
        
        // Reliability statistics
        Collection<SourceReliabilityManager.SourceMetrics> metrics = reliabilityManager.getAllMetrics().values();
        if (!metrics.isEmpty()) {
            double avgReliability = metrics.stream().mapToDouble(m -> m.reliabilityScore).average().orElse(0.0);
            double maxReliability = metrics.stream().mapToDouble(m -> m.reliabilityScore).max().orElse(0.0);
            double minReliability = metrics.stream().mapToDouble(m -> m.reliabilityScore).min().orElse(0.0);
            
            stats.put("averageReliability", Math.round(avgReliability * 1000.0) / 1000.0);
            stats.put("maxReliability", Math.round(maxReliability * 1000.0) / 1000.0);
            stats.put("minReliability", Math.round(minReliability * 1000.0) / 1000.0);
        }
        
        return stats;
    }
}
