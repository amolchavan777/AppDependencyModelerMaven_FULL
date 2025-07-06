package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.io.*;
import java.util.*;

/**
 * Enhanced CSV exporter that includes provenance metadata and source reliability information
 */
public class EnhancedCsvExporter {
    
    private EnhancedCsvExporter() {
        // Utility class
    }
    
    /**
     * Export claims with full provenance data to CSV
     */
    public static void exportClaimsWithProvenance(List<Claim> claims, 
                                                SourceReliabilityManager reliabilityManager,
                                                String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            // Write header
            writer.write("FromApp,ToApp,Source,Exists,Confidence,Type,Timestamp,ExtractionMethod,");
            writer.write("SourceReliability,TotalClaims,ValidatedClaims,Accuracy,Consistency,");
            writer.write("FormattedTimestamp,Metadata\n");
            
            for (Claim claim : claims) {
                writer.write(escapeCsv(claim.fromApp) + ",");
                writer.write(escapeCsv(claim.toApp) + ",");
                writer.write(escapeCsv(claim.source) + ",");
                writer.write(claim.exists + ",");
                writer.write(claim.confidence + ",");
                writer.write(escapeCsv(claim.type) + ",");
                
                // Provenance information
                if (claim instanceof EnhancedClaim) {
                    EnhancedClaim enhanced = (EnhancedClaim) claim;
                    writer.write(enhanced.timestampMillis + ",");
                    writer.write(escapeCsv(enhanced.extractionMethod) + ",");
                    
                    // Source reliability metrics
                    SourceReliabilityManager.SourceMetrics metrics = reliabilityManager.getAllMetrics().get(claim.source);
                    if (metrics != null) {
                        writer.write(String.format("%.3f", metrics.reliabilityScore) + ",");
                        writer.write(metrics.totalClaims + ",");
                        writer.write(metrics.validatedClaims + ",");
                        writer.write(String.format("%.3f", metrics.accuracy) + ",");
                        writer.write(String.format("%.3f", metrics.consistency) + ",");
                    } else {
                        writer.write("0.5,0,0,0.0,0.0,");
                    }
                    
                    writer.write(escapeCsv(enhanced.getFormattedTimestamp()) + ",");
                    
                    // Metadata
                    if (!enhanced.additionalMetadata.isEmpty()) {
                        StringBuilder metadataStr = new StringBuilder();
                        for (Map.Entry<String, String> entry : enhanced.additionalMetadata.entrySet()) {
                            metadataStr.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
                        }
                        writer.write(escapeCsv(metadataStr.toString()));
                    }
                } else {
                    // Fallback for standard claims
                    writer.write(System.currentTimeMillis() + ",");
                    writer.write(escapeCsv(SourceReliabilityManager.ExtractionMethod.fromSource(claim.source).getDisplayName()) + ",");
                    
                    SourceReliabilityManager.SourceMetrics metrics = reliabilityManager.getAllMetrics().get(claim.source);
                    if (metrics != null) {
                        writer.write(String.format("%.3f", metrics.reliabilityScore) + ",");
                        writer.write(metrics.totalClaims + ",");
                        writer.write(metrics.validatedClaims + ",");
                        writer.write(String.format("%.3f", metrics.accuracy) + ",");
                        writer.write(String.format("%.3f", metrics.consistency) + ",");
                    } else {
                        writer.write("0.5,0,0,0.0,0.0,");
                    }
                    
                    writer.write(escapeCsv(new Date().toString()) + ",");
                    writer.write("");
                }
                
                writer.write("\n");
            }
        }
    }
    
    /**
     * Export source reliability summary to CSV
     */
    public static void exportSourceReliability(SourceReliabilityManager reliabilityManager,
                                             String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            // Write header
            writer.write("SourceName,ExtractionMethod,ReliabilityScore,TotalClaims,ValidatedClaims,");
            writer.write("AgreementCount,DisagreementCount,Accuracy,Consistency,LastUpdate\n");
            
            for (SourceReliabilityManager.SourceMetrics metrics : reliabilityManager.getAllMetrics().values()) {
                writer.write(escapeCsv(metrics.sourceName) + ",");
                writer.write(escapeCsv(metrics.extractionMethod.getDisplayName()) + ",");
                writer.write(String.format("%.3f", metrics.reliabilityScore) + ",");
                writer.write(metrics.totalClaims + ",");
                writer.write(metrics.validatedClaims + ",");
                writer.write(metrics.agreementCount + ",");
                writer.write(metrics.disagreementCount + ",");
                writer.write(String.format("%.3f", metrics.accuracy) + ",");
                writer.write(String.format("%.3f", metrics.consistency) + ",");
                writer.write(metrics.lastUpdate + "\n");
            }
        }
    }
    
    /**
     * Export aggregated extraction method statistics
     */
    public static void exportExtractionMethodStats(List<Claim> claims,
                                                  SourceReliabilityManager reliabilityManager,
                                                  String filename) throws IOException {
        Map<SourceReliabilityManager.ExtractionMethod, Integer> methodCounts = new HashMap<>();
        Map<SourceReliabilityManager.ExtractionMethod, Double> methodReliability = new HashMap<>();
        Map<SourceReliabilityManager.ExtractionMethod, Integer> methodValidated = new HashMap<>();
        
        // Aggregate data by extraction method
        for (Claim claim : claims) {
            SourceReliabilityManager.ExtractionMethod method = SourceReliabilityManager.ExtractionMethod.fromSource(claim.source);
            methodCounts.merge(method, 1, Integer::sum);
            
            SourceReliabilityManager.SourceMetrics metrics = reliabilityManager.getAllMetrics().get(claim.source);
            if (metrics != null) {
                methodReliability.merge(method, metrics.reliabilityScore, Double::sum);
                methodValidated.merge(method, metrics.validatedClaims, Integer::sum);
            }
        }
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("ExtractionMethod,TotalClaims,AverageReliability,TotalValidated,ValidationRate\n");
            
            for (SourceReliabilityManager.ExtractionMethod method : SourceReliabilityManager.ExtractionMethod.values()) {
                int totalClaims = methodCounts.getOrDefault(method, 0);
                if (totalClaims > 0) {
                    double avgReliability = methodReliability.getOrDefault(method, 0.0) / totalClaims;
                    int totalValidated = methodValidated.getOrDefault(method, 0);
                    double validationRate = totalClaims > 0 ? (double) totalValidated / totalClaims : 0.0;
                    
                    writer.write(escapeCsv(method.getDisplayName()) + ",");
                    writer.write(totalClaims + ",");
                    writer.write(String.format("%.3f", avgReliability) + ",");
                    writer.write(totalValidated + ",");
                    writer.write(String.format("%.3f", validationRate) + "\n");
                }
            }
        }
    }
    
    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
