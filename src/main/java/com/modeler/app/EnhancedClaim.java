package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;

/**
 * Enhanced claim with provenance metadata for tracking source information,
 * timestamps, and extraction methods for full traceability.
 */
public class EnhancedClaim extends Claim {
    
    // Provenance metadata
    public final long timestampMillis;
    public final String extractionMethod;
    public final String originalData;
    public final Map<String, String> additionalMetadata;
    
    /**
     * Create enhanced claim with automatic timestamp
     */
    public EnhancedClaim(String source, String fromApp, String toApp, boolean exists, 
                        double confidence, String extractionMethod) {
        super(source, fromApp, toApp, exists, confidence);
        this.timestampMillis = System.currentTimeMillis();
        this.extractionMethod = extractionMethod != null ? extractionMethod : "Unknown";
        this.originalData = null;
        this.additionalMetadata = new HashMap<>();
    }
    
    /**
     * Create enhanced claim with explicit timestamp
     */
    public EnhancedClaim(String source, String fromApp, String toApp, boolean exists, 
                        double confidence, String extractionMethod, long timestampMillis) {
        super(source, fromApp, toApp, exists, confidence);
        this.timestampMillis = timestampMillis;
        this.extractionMethod = extractionMethod != null ? extractionMethod : "Unknown";
        this.originalData = null;
        this.additionalMetadata = new HashMap<>();
    }
    
    /**
     * Create enhanced claim with full provenance data
     */
    public EnhancedClaim(String source, String fromApp, String toApp, boolean exists, 
                        double confidence, String extractionMethod, long timestampMillis,
                        String originalData, Map<String, String> additionalMetadata) {
        super(source, fromApp, toApp, exists, confidence);
        this.timestampMillis = timestampMillis;
        this.extractionMethod = extractionMethod != null ? extractionMethod : "Unknown";
        this.originalData = originalData;
        this.additionalMetadata = additionalMetadata != null ? new HashMap<>(additionalMetadata) : new HashMap<>();
    }
    
    /**
     * Create enhanced claim from existing claim
     */
    public static EnhancedClaim fromClaim(Claim claim, String extractionMethod) {
        return new EnhancedClaim(claim.source, claim.fromApp, claim.toApp, 
                               claim.exists, claim.confidence, extractionMethod);
    }
    
    /**
     * Get formatted timestamp string
     */
    public String getFormattedTimestamp() {
        return new Date(timestampMillis).toString();
    }
    
    /**
     * Add metadata entry
     */
    public void addMetadata(String key, String value) {
        additionalMetadata.put(key, value);
    }
    
    /**
     * Get provenance information as a map
     */
    public Map<String, Object> getProvenanceMap() {
        Map<String, Object> provenance = new HashMap<>();
        provenance.put("source", source);
        provenance.put("timestamp", timestampMillis);
        provenance.put("formattedTimestamp", getFormattedTimestamp());
        provenance.put("extractionMethod", extractionMethod);
        if (originalData != null) {
            provenance.put("originalData", originalData);
        }
        if (!additionalMetadata.isEmpty()) {
            provenance.put("metadata", new HashMap<>(additionalMetadata));
        }
        return provenance;
    }
    
    /**
     * Convert to map including all claim and provenance data
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fromApp", fromApp);
        map.put("toApp", toApp);
        map.put("exists", exists);
        map.put("confidence", confidence);
        map.put("type", type);
        map.putAll(getProvenanceMap());
        return map;
    }
    
    @Override
    public String toString() {
        String base = super.toString();
        return base + " | timestamp=" + getFormattedTimestamp() + 
               ", method=" + extractionMethod;
    }
}
