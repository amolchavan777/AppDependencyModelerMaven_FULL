package com.modeler.app;

import com.enterprise.dependency.model.core.Claim;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import static spark.Spark.*;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Entry point for the Application Dependency Modeler demo. It collects data
 * from all adapters, runs the truth discovery engine and exports the results
 * to ArchiMate, GraphML and JSON formats.
 */

public class Main {

    private static final String SEPARATOR = "\n============================================================ \n";
    
    // Global state for dashboard
    private static TruthDiscoveryEngineEM globalEngine;
    private static List<Claim> globalClaims;
    private static Map<String, Set<String>> globalResult;
    private static SourceReliabilityManager reliabilityManager = new SourceReliabilityManager();
    private static String currentStrategy = "em";

    // Status tracking for real-time updates
    private static volatile String currentStatus = "Idle";
    private static volatile String lastOperation = "System initialized";
    private static volatile long lastUpdateTime = System.currentTimeMillis();
    
    public static void updateStatus(String status, String operation) {
        currentStatus = status;
        lastOperation = operation;
        lastUpdateTime = System.currentTimeMillis();
        System.out.println("[STATUS] " + status + ": " + operation);
    }
    
    public static Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", currentStatus);
        status.put("lastOperation", lastOperation);
        status.put("lastUpdate", lastUpdateTime);
        return status;
    }

    /**
     * Command-line execution.
     */
    public static void main(String[] args) throws IOException {
        // Initialize status
        updateStatus("Initializing", "Starting Application Dependency Modeler");
        
        // Start Spark server in a separate thread
        startDashboardServer();
        
        try {
            updateStatus("Processing", "Gathering raw claims from all adapters");
            // Gather raw claims from all adapters
            List<Claim> rawClaims = Normalizer.collectAllClaims();
            
            // Convert raw claims to enhanced claims with provenance
            List<Claim> enhancedClaims = new ArrayList<>();
            for (Claim claim : rawClaims) {
                EnhancedClaim enhanced = new EnhancedClaim(claim.source, claim.fromApp, claim.toApp, 
                    claim.exists, claim.confidence, "AUTOMATED_PARSING");
                enhancedClaims.add(enhanced);
                reliabilityManager.addClaim(enhanced);
            }
            
            Map<String,String> aliasMap = Normalizer.getAliasMap();
            List<Claim> allClaims = Normalizer.normalizeClaims(enhancedClaims);
            
            // Update reliability based on normalized claims
            reliabilityManager.updateAllSourceReliability(allClaims);
            
            ClaimIdentityResolver resolver = new ClaimIdentityResolver(aliasMap);
            List<ClaimIdentityResolver.ResolvedClaim> resolved = resolver.resolve(rawClaims);
            List<Claim> negativeClaims = NegativeClaimGenerator.generate(allClaims);
            
            System.out.println(SEPARATOR);
            System.out.println("✅ Normalized Application Dependency Claims:");
            for (Claim c : allClaims) System.out.println(c);
            System.out.println(SEPARATOR);
            System.out.println("Collected " + allClaims.size() + " claims.");
            System.out.println(SEPARATOR);
            
            updateStatus("Processing", "Running Latent Truth Model with EM");
            System.out.println("Running Latent Truth Model with EM...\n");
            System.out.println(SEPARATOR);
            
            Map<InitialAggregator.Pair, Double> initialAgg = InitialAggregator.aggregate(allClaims);
            Map<String, Integer> coverage = CoverageUtil.computeCoverage(allClaims);

            // Detect conflicting claim groups before EM
            List<Claim> combined = new ArrayList<>(allClaims);
            combined.addAll(negativeClaims);
            List<ConflictDetector.ConflictGroup> conflictGroups = ConflictDetector.detect(combined);
            long conflictCount = conflictGroups.stream().filter(g -> g.conflicted).count();
            
            System.out.println(SEPARATOR);
            System.out.println("Detected " + conflictCount + " conflicted claim groups.");
            for (ConflictDetector.ConflictGroup g : conflictGroups) {
                if (!g.conflicted) continue;
                System.out.println("❗ Conflict for " + g.pair.from + " -> " + g.pair.to);
                for (Claim c : g.claims) {
                    System.out.println("   " + c.source + " says exists=" + c.exists);
                }
            }
            System.out.println(SEPARATOR);
            
            // Resolve conflicting claims using the latent credibility engine
            TruthDiscoveryEngineEM engine = new TruthDiscoveryEngineEM();
            
            // Apply reliability-weighted voting
            Map<String, Double> sourceWeights = new HashMap<>();
            for (String source : reliabilityManager.getAllMetrics().keySet()) {
                sourceWeights.put(source, reliabilityManager.getReliabilityScore(source));
            }
            engine.setSourceWeights(sourceWeights);
            
            engine.runEM(combined);
            Map<String, Set<String>> result = engine.getResult();
            
            // Mark validated claims in reliability manager
            List<Claim> validatedClaims = new ArrayList<>();
            for (String fromApp : result.keySet()) {
                for (String toApp : result.get(fromApp)) {
                    for (Claim claim : combined) {
                        if (claim.fromApp.equals(fromApp) && claim.toApp.equals(toApp) && claim.exists) {
                            validatedClaims.add(claim);
                        }
                    }
                }
            }
            reliabilityManager.markClaimsValidated(validatedClaims);
            
            // Print reliability report
            reliabilityManager.printReliabilityReport();
            
            // Store results globally for dashboard
            globalEngine = engine;
            globalClaims = combined;
            globalResult = result;

            System.out.println("\n=============================printDependencySummary=============================== \n");
            printDependencySummary(result);

            // Print dependency analytics before exporting
            DependencyMetrics.printMetrics(result);
            System.out.println("\n===============================printMetrics============================= \n");
            
            // Display simple histograms of dependency fan-out and fan-in
            DependencyHistogram.printOutgoingHistogram(result);
            System.out.println(SEPARATOR);
            DependencyHistogram.printIncomingHistogram(result);
            System.out.println(SEPARATOR);

            // Persist raw claims and final dependencies for later analysis
            try (PersistenceManager pm = new PersistenceManager("jdbc:h2:./output/dependencyDB")) {
                pm.saveClaims(allClaims);
                pm.saveDependencies(result);
                System.out.println("📄 Results stored in output/dependencyDB.mv.db");
            } catch (SQLException se) {
                System.err.println("Failed to persist results: " + se.getMessage());
            }
            
            // Export only the dependencies that survived truth discovery
            ArchimateExporter.export(result, "output/archimate_model.xml");
            System.out.println("📄 Archimate model exported to output/archimate_model.xml");

            GraphMLExporter.export(result, "output/dependency_graph.graphml");
            System.out.println("📄 GraphML model exported to output/dependency_graph.graphml");

            JsonExporter.export(result, "output/dependency_graph.json");
            System.out.println("📄 JSON graph exported to output/dependency_graph.json");

            // Export enhanced JSON with provenance and reliability data
            try {
                EnhancedJsonExporter.export(result, combined, reliabilityManager, "output/enhanced_dependency_graph.json");
                System.out.println("📄 Enhanced JSON with provenance exported to output/enhanced_dependency_graph.json");
            } catch (Exception e) {
                System.err.println("Failed to export enhanced JSON: " + e.getMessage());
            }

            DashboardExporter.export("output");
            System.out.println("📄 Dashboard available at output/index.html");

            // CSV export for dependency summaries
            CsvExporter.export(result, "output/dependency_summary.csv", "output/dependency_edges.csv");
            System.out.println("📄 CSV summaries exported to output/dependency_*.csv");

            // Export enhanced CSV files with provenance and reliability data
            try {
                EnhancedCsvExporter.exportClaimsWithProvenance(combined, reliabilityManager, "output/claims_with_provenance.csv");
                EnhancedCsvExporter.exportSourceReliability(reliabilityManager, "output/source_reliability.csv");
                EnhancedCsvExporter.exportExtractionMethodStats(combined, reliabilityManager, "output/extraction_method_stats.csv");
                System.out.println("📄 Enhanced CSV files exported with provenance and reliability data");
            } catch (Exception e) {
                System.err.println("Failed to export enhanced CSV files: " + e.getMessage());
            }

            updateStatus("Ready", "System ready - Dashboard available at http://localhost:4567");

            // Export a multi-sheet Excel workbook for auditing
            ExcelExporter.export(rawClaims,
                    aliasMap,
                    allClaims,
                    resolved,
                    negativeClaims,
                    conflictGroups,
                    initialAgg,
                    engine.getTrustHistory(),
                    result,
                    coverage,
                    "output/application_dependency_audit.xlsx");
            System.out.println("📄 Excel audit workbook exported to output/application_dependency_audit.xlsx");

            System.out.println(SEPARATOR);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occurred: " + e.getMessage());
            updateStatus("Error", "Application failed: " + e.getMessage());
        }
        System.out.println(SEPARATOR);
    }

    private static void startDashboardServer() {
        // Start Spark server in background thread to avoid blocking
        new Thread(() -> {
            port(4567); // default Spark port
            staticFiles.location("/dashboard"); // serve static files from resources/dashboard
            ObjectMapper mapper = new ObjectMapper();

            // Status endpoint
            get("/api/status", (req, res) -> {
                res.type("application/json");
                return mapper.writeValueAsString(getStatus());
            });

            // Change truth discovery strategy
            post("/api/strategy", (req, res) -> {
                Map<String, String> body = mapper.readValue(req.body(), Map.class);
                String strategy = body.getOrDefault("strategy", "em");
                recomputeModel(strategy);
                res.type("application/json");
                return mapper.writeValueAsString(Map.of("status", "ok"));
            });

            // Add a new claim via dashboard
            post("/api/add", (req, res) -> {
                Map<String, String> body = mapper.readValue(req.body(), Map.class);
                String source = body.getOrDefault("source", "manual");
                String claimStr = body.getOrDefault("claim", "");
                boolean exists = Boolean.parseBoolean(body.getOrDefault("exists", "true"));

                String[] parts = claimStr.split("->");
                if (parts.length != 2) {
                    res.status(400);
                    return "Invalid claim";
                }
                Claim claim = new Claim(source.trim(), parts[0].trim(), parts[1].trim(), exists, 1.0);
                if (globalClaims == null) globalClaims = new ArrayList<>();
                globalClaims.add(claim);
                reliabilityManager.addClaim(claim);
                recomputeModel(currentStrategy);
                res.type("application/json");
                return mapper.writeValueAsString(Map.of("status", "added"));
            });

            // Enhanced analytics endpoint with reliability and provenance data
            get("/api/analytics", (req, res) -> {
                res.type("application/json");
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> reliability = new ArrayList<>();
                List<Map<String, Object>> claims = new ArrayList<>();
                
                // Enhanced reliability information from reliability manager
                if (reliabilityManager != null) {
                    for (Map.Entry<String, SourceReliabilityManager.SourceMetrics> entry : reliabilityManager.getAllMetrics().entrySet()) {
                        Map<String, Object> sourceInfo = entry.getValue().toMap();
                        reliability.add(sourceInfo);
                    }
                }
                
                if (globalEngine != null) {
                    // Get actual source reliability scores from EM engine
                    Map<String, Double> sourceScores = globalEngine.getSourceTrust();
                    for (var entry : sourceScores.entrySet()) {
                        // Find existing entry and update with EM score
                        reliability.stream()
                            .filter(r -> entry.getKey().equals(r.get("sourceName")))
                            .findFirst()
                            .ifPresent(r -> r.put("emScore", entry.getValue()));
                    }
                    
                    // Get actual claim confidences (show top 20 for display)
                    if (globalClaims != null) {
                        int count = 0;
                        for (Claim claim : globalClaims) {
                            if (count >= 20) break; // Limit for dashboard display
                            String claimStr = claim.fromApp + " -> " + claim.toApp;
                            Map<String, Object> claimInfo = new HashMap<>();
                            claimInfo.put("claim", claimStr);
                            claimInfo.put("confidence", claim.confidence);
                            claimInfo.put("source", claim.source);
                            
                            // Add provenance if enhanced claim
                            if (claim instanceof EnhancedClaim) {
                                EnhancedClaim enhanced = (EnhancedClaim) claim;
                                claimInfo.put("timestamp", enhanced.getFormattedTimestamp());
                                claimInfo.put("extractionMethod", enhanced.extractionMethod);
                            }
                            
                            claims.add(claimInfo);
                            count++;
                        }
                    }
                }
                
                result.put("reliability", reliability);
                result.put("claims", claims);
                result.put("strategy", currentStrategy);
                return mapper.writeValueAsString(result);
            });

            // Enhanced graph data endpoint with reliability metadata
            get("/api/graph", (req, res) -> {
                res.type("application/json");
                Map<String, Object> graphData = new HashMap<>();
                List<Map<String, Object>> nodes = new ArrayList<>();
                List<Map<String, Object>> edges = new ArrayList<>();
                
                if (globalResult != null) {
                    Set<String> allApps = new HashSet<>();
                    
                    // Collect all applications
                    for (String fromApp : globalResult.keySet()) {
                        allApps.add(fromApp);
                        allApps.addAll(globalResult.get(fromApp));
                    }
                    
                    // Create nodes with reliability metadata
                    for (String app : allApps) {
                        Map<String, Object> node = new HashMap<>();
                        node.put("id", app);
                        node.put("label", app);
                        
                        // Add reliability score if available
                        if (reliabilityManager != null && globalClaims != null) {
                            double avgReliability = globalClaims.stream()
                                .filter(c -> c.fromApp.equals(app) || c.toApp.equals(app))
                                .mapToDouble(c -> reliabilityManager.getReliabilityScore(c.source))
                                .average().orElse(0.5);
                            node.put("reliability", avgReliability);
                        }
                        
                        nodes.add(node);
                    }
                    
                    // Create edges with provenance metadata
                    for (String fromApp : globalResult.keySet()) {
                        for (String toApp : globalResult.get(fromApp)) {
                            Map<String, Object> edge = new HashMap<>();
                            edge.put("from", fromApp);
                            edge.put("to", toApp);
                            
                            // Find supporting claims for provenance
                            if (globalClaims != null) {
                                List<String> sources = new ArrayList<>();
                                double maxConfidence = 0.0;
                                for (Claim claim : globalClaims) {
                                    if (claim.fromApp.equals(fromApp) && claim.toApp.equals(toApp) && claim.exists) {
                                        sources.add(claim.source);
                                        maxConfidence = Math.max(maxConfidence, claim.confidence);
                                    }
                                }
                                edge.put("sources", sources);
                                edge.put("confidence", maxConfidence);
                            }
                            
                            edges.add(edge);
                        }
                    }
                }
                
                graphData.put("nodes", nodes);
                graphData.put("edges", edges);
                return mapper.writeValueAsString(graphData);
            });

            // Reliability report endpoint
            get("/api/reliability", (req, res) -> {
                res.type("application/json");
                Map<String, Object> report = new HashMap<>();
                
                if (reliabilityManager != null) {
                    Map<String, Object> metrics = new HashMap<>();
                    for (Map.Entry<String, SourceReliabilityManager.SourceMetrics> entry : reliabilityManager.getAllMetrics().entrySet()) {
                        metrics.put(entry.getKey(), entry.getValue().toMap());
                    }
                    report.put("sourceMetrics", metrics);
                }
                
                return mapper.writeValueAsString(report);
            });

            // Provenance endpoint for detailed claim tracking
            get("/api/provenance", (req, res) -> {
                res.type("application/json");
                List<Map<String, Object>> provenance = new ArrayList<>();
                
                if (globalClaims != null) {
                    for (Claim claim : globalClaims) {
                        if (claim instanceof EnhancedClaim) {
                            EnhancedClaim enhanced = (EnhancedClaim) claim;
                            Map<String, Object> entry = new HashMap<>();
                            entry.put("fromApp", enhanced.fromApp);
                            entry.put("toApp", enhanced.toApp);
                            entry.put("exists", enhanced.exists);
                            entry.put("source", enhanced.source);
                            entry.put("confidence", enhanced.confidence);
                            entry.put("timestamp", enhanced.getFormattedTimestamp());
                            entry.put("extractionMethod", enhanced.extractionMethod);
                            provenance.add(entry);
                        }
                    }
                }
                
                return mapper.writeValueAsString(provenance);
            });

            // Metrics endpoint for dashboard compatibility
            get("/api/metrics", (req, res) -> {
                res.type("application/json");
                Map<String, Object> response = new HashMap<>();
                List<Map<String, Object>> metrics = new ArrayList<>();
                
                if (globalResult != null) {
                    // Calculate fan-in and fan-out for each application
                    Map<String, Integer> fanOut = new HashMap<>();
                    Map<String, Integer> fanIn = new HashMap<>();
                    
                    // Calculate fan-out (outgoing dependencies)
                    for (String fromApp : globalResult.keySet()) {
                        fanOut.put(fromApp, globalResult.get(fromApp).size());
                    }
                    
                    // Calculate fan-in (incoming dependencies)
                    for (String fromApp : globalResult.keySet()) {
                        for (String toApp : globalResult.get(fromApp)) {
                            fanIn.put(toApp, fanIn.getOrDefault(toApp, 0) + 1);
                        }
                    }
                    
                    // Collect all unique applications
                    Set<String> allApps = new HashSet<>();
                    allApps.addAll(fanOut.keySet());
                    allApps.addAll(fanIn.keySet());
                    
                    // Create metrics entries
                    for (String app : allApps) {
                        Map<String, Object> metric = new HashMap<>();
                        metric.put("application", app);
                        metric.put("fanOut", fanOut.getOrDefault(app, 0));
                        metric.put("fanIn", fanIn.getOrDefault(app, 0));
                        metrics.add(metric);
                    }
                }
                
                response.put("metrics", metrics);
                return mapper.writeValueAsString(response);
            });

            System.out.println("🌐 Dashboard server started on http://localhost:4567");
        }).start();
    }

    private static void recomputeModel(String strategy) {
        if (globalClaims == null) return;

        updateStatus("Processing", "Recomputing model using " + strategy);
        Map<String, Set<String>> result;

        if ("majority".equalsIgnoreCase(strategy)) {
            result = MajorityVotingResolver.resolve(globalClaims);
            globalEngine = null;
        } else if ("weighted".equalsIgnoreCase(strategy)) {
            Map<String, Double> weights = reliabilityManager.getReliabilityWeights();
            result = WeightedVotingResolver.resolve(globalClaims, weights);
            globalEngine = null;
        } else { // default to EM
            TruthDiscoveryEngineEM engine = new TruthDiscoveryEngineEM();
            engine.setSourceWeights(reliabilityManager.getReliabilityWeights());
            engine.runEM(globalClaims);
            globalEngine = engine;
            result = engine.getResult();
        }

        globalResult = result;
        currentStrategy = strategy;
        updateStatus("Ready", "Model recomputed using " + strategy);
    }

    private static void printDependencySummary(Map<String, Set<String>> result) {
        System.out.println("📊 Final Dependency Summary:");
        System.out.println("============================");
        
        int totalDependencies = 0;
        for (String fromApp : result.keySet()) {
            Set<String> dependencies = result.get(fromApp);
            totalDependencies += dependencies.size();
            System.out.println(fromApp + " depends on: " + String.join(", ", dependencies));
        }
        
        System.out.println("Total applications: " + result.keySet().size());
        System.out.println("Total dependencies: " + totalDependencies);
        
        // Print reliability summary
        if (reliabilityManager != null) {
            System.out.println("\n📈 Source Reliability Summary:");
            System.out.println("==============================");
            for (Map.Entry<String, SourceReliabilityManager.SourceMetrics> entry : reliabilityManager.getAllMetrics().entrySet()) {
                SourceReliabilityManager.SourceMetrics metrics = entry.getValue();
                System.out.println(String.format("%s: %.2f (validated: %d/%d)", 
                    entry.getKey(), metrics.reliabilityScore, metrics.validatedClaims, metrics.totalClaims));
            }
        }
    }
}
