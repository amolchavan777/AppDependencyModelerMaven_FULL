package com.modeler.app;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// additional model helpers
import com.modeler.app.ClaimIdentityResolver;
import com.modeler.app.ConflictDetector;

/**
 * Export various stages of the dependency modeling process to
 * a single multi-sheet Excel workbook for auditing purposes.
 */
public class ExcelExporter {

    /**
     * Export the full modeling pipeline as a nine-sheet workbook.
     */
    public static void export(List<Claim> rawClaims,
                              Map<String,String> normalizationMap,
                              List<Claim> normalizedClaims,
                              List<ClaimIdentityResolver.ResolvedClaim> resolvedClaims,
                              List<Claim> negativeClaims,
                              List<ConflictDetector.ConflictGroup> conflictGroups,
                              Map<InitialAggregator.Pair, Double> initialAgg,
                              List<Map<String, Double>> ltmIterations,
                              Map<String, Set<String>> finalDeps,
                              Map<String, Integer> dataCoverage,
                              String filePath) throws IOException {
        Workbook wb = new XSSFWorkbook();

        // Process flow sheet (1)
        Sheet flowSheet = wb.createSheet("Process Flow");
        Row fHead = flowSheet.createRow(0);
        fHead.createCell(0).setCellValue("step");
        int fRow = 1;
        String[] steps = new String[] {
                "Raw Claims",
                "Normalization Mapping",
                "Alias Groups",
                "Normalized Claims",
                "Claim Identities",
                "Negative Claims",
                "Conflict Groups",
                "Initial Aggregation",
                "LTM Iterations",
                "Final Dependencies",
                "Data Coverage"
        };
        for (String s : steps) {
            Row fr = flowSheet.createRow(fRow++);
            fr.createCell(0).setCellValue(s);
        }

        // Raw claims sheet (2)
        Sheet rawSheet = wb.createSheet("Raw Claims");
        Row rHead = rawSheet.createRow(0);
        rHead.createCell(0).setCellValue("source");
        rHead.createCell(1).setCellValue("fromApp");
        rHead.createCell(2).setCellValue("toApp");
        rHead.createCell(3).setCellValue("exists");
        rHead.createCell(4).setCellValue("confidence");
        rHead.createCell(5).setCellValue("timestamp");
        rHead.createCell(6).setCellValue("metadata");
        int row = 1;
        for (Claim c : rawClaims) {
            Row r = rawSheet.createRow(row++);
            r.createCell(0).setCellValue(c.source);
            r.createCell(1).setCellValue(c.fromApp);
            r.createCell(2).setCellValue(c.toApp);
            r.createCell(3).setCellValue(c.exists);
            r.createCell(4).setCellValue(c.confidence);
            if (c.timestamp != null) r.createCell(5).setCellValue(c.timestamp);
            if (c.metadata != null) r.createCell(6).setCellValue(c.metadata);
        }

        // Normalization mapping sheet (3)
        Sheet mapSheet = wb.createSheet("Normalization Mapping");
        Row mHead = mapSheet.createRow(0);
        mHead.createCell(0).setCellValue("alias");
        mHead.createCell(1).setCellValue("canonical");
        row = 1;
        for (var e : normalizationMap.entrySet()) {
            Row mr = mapSheet.createRow(row++);
            mr.createCell(0).setCellValue(e.getKey());
            mr.createCell(1).setCellValue(e.getValue());
        }

        // Alias/group resolution sheet (4)
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (var e : normalizationMap.entrySet()) {
            groups.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }
        Sheet aliasSheet = wb.createSheet("Alias Groups");
        Row aHead = aliasSheet.createRow(0);
        aHead.createCell(0).setCellValue("canonical");
        aHead.createCell(1).setCellValue("aliases");
        row = 1;
        for (var e : groups.entrySet()) {
            Row ar = aliasSheet.createRow(row++);
            ar.createCell(0).setCellValue(e.getKey());
            ar.createCell(1).setCellValue(String.join(", ", e.getValue()));
        }

        // Normalized claims sheet (5)
        Sheet normSheet = wb.createSheet("Normalized Claims");
        Row nHead = normSheet.createRow(0);
        nHead.createCell(0).setCellValue("source");
        nHead.createCell(1).setCellValue("fromApp");
        nHead.createCell(2).setCellValue("toApp");
        nHead.createCell(3).setCellValue("exists");
        nHead.createCell(4).setCellValue("confidence");
        row = 1;
        for (Claim c : normalizedClaims) {
            Row nr = normSheet.createRow(row++);
            nr.createCell(0).setCellValue(c.source);
            nr.createCell(1).setCellValue(c.fromApp);
            nr.createCell(2).setCellValue(c.toApp);
            nr.createCell(3).setCellValue(c.exists);
            nr.createCell(4).setCellValue(c.confidence);
        }

        // Claim identity sheet (6)
        Sheet idSheet = wb.createSheet("Claim Identities");
        Row idHead = idSheet.createRow(0);
        idHead.createCell(0).setCellValue("id");
        idHead.createCell(1).setCellValue("fromApp");
        idHead.createCell(2).setCellValue("toApp");
        idHead.createCell(3).setCellValue("source");
        idHead.createCell(4).setCellValue("exists");
        idHead.createCell(5).setCellValue("confidence");
        row = 1;
        for (ClaimIdentityResolver.ResolvedClaim rc : resolvedClaims) {
            Row ir = idSheet.createRow(row++);
            ir.createCell(0).setCellValue(rc.id());
            ir.createCell(1).setCellValue(rc.from());
            ir.createCell(2).setCellValue(rc.to());
            ir.createCell(3).setCellValue(rc.claim().source);
            ir.createCell(4).setCellValue(rc.claim().exists);
            ir.createCell(5).setCellValue(rc.claim().confidence);
        }

        // Negative claims sheet (7)
        Sheet negSheet = wb.createSheet("Negative Claims");
        Row negHead = negSheet.createRow(0);
        negHead.createCell(0).setCellValue("source");
        negHead.createCell(1).setCellValue("fromApp");
        negHead.createCell(2).setCellValue("toApp");
        row = 1;
        for (Claim c : negativeClaims) {
            Row nr = negSheet.createRow(row++);
            nr.createCell(0).setCellValue(c.source);
            nr.createCell(1).setCellValue(c.fromApp);
            nr.createCell(2).setCellValue(c.toApp);
        }

        // Conflict groups sheet (8)
        Sheet confSheet = wb.createSheet("Conflict Groups");
        Row confHead = confSheet.createRow(0);
        confHead.createCell(0).setCellValue("fromApp");
        confHead.createCell(1).setCellValue("toApp");
        confHead.createCell(2).setCellValue("conflicted");
        confHead.createCell(3).setCellValue("sources");
        row = 1;
        for (ConflictDetector.ConflictGroup g : conflictGroups) {
            Row cr = confSheet.createRow(row++);
            cr.createCell(0).setCellValue(g.pair.from);
            cr.createCell(1).setCellValue(g.pair.to);
            cr.createCell(2).setCellValue(g.conflicted);
            List<String> srcs = new ArrayList<>();
            for (Claim c : g.claims) {
                String label = c.source + (c.exists ? "" : "(neg)");
                srcs.add(label);
            }
            cr.createCell(3).setCellValue(String.join(", ", srcs));
        }

        // Initial aggregation sheet (9)
        Sheet aggSheet = wb.createSheet("Initial Aggregation");
        Row agHead = aggSheet.createRow(0);
        agHead.createCell(0).setCellValue("fromApp");
        agHead.createCell(1).setCellValue("toApp");
        agHead.createCell(2).setCellValue("score");
        row = 1;
        for (var e : initialAgg.entrySet()) {
            Row ar = aggSheet.createRow(row++);
            ar.createCell(0).setCellValue(e.getKey().from);
            ar.createCell(1).setCellValue(e.getKey().to);
            ar.createCell(2).setCellValue(e.getValue());
        }

        // LTM iterations sheet (10)
        Sheet iterSheet = wb.createSheet("LTM Iterations");
        // Determine all sources across iterations
        java.util.Set<String> sourceSet = new java.util.TreeSet<>();
        for (Map<String, Double> m : ltmIterations) sourceSet.addAll(m.keySet());
        java.util.List<String> sources = new java.util.ArrayList<>(sourceSet);
        Row iHead = iterSheet.createRow(0);
        iHead.createCell(0).setCellValue("iteration");
        int col = 1;
        for (String s : sources) iHead.createCell(col++).setCellValue(s);
        int irow = 1;
        int it = 0;
        for (Map<String, Double> m : ltmIterations) {
            Row ir = iterSheet.createRow(irow++);
            ir.createCell(0).setCellValue(it++);
            col = 1;
            for (String s : sources) {
                ir.createCell(col++).setCellValue(m.getOrDefault(s, 0.0));
            }
        }

        // Final dependency graph sheet (11)
        Sheet depSheet = wb.createSheet("Final Dependencies");
        Row dHead = depSheet.createRow(0);
        dHead.createCell(0).setCellValue("fromApp");
        dHead.createCell(1).setCellValue("toApp");
        row = 1;
        for (Map.Entry<String, Set<String>> e : finalDeps.entrySet()) {
            String from = e.getKey();
            for (String to : e.getValue()) {
                Row dr = depSheet.createRow(row++);
                dr.createCell(0).setCellValue(from);
                dr.createCell(1).setCellValue(to);
            }
        }

        // Data coverage sheet (12)
        Sheet covSheet = wb.createSheet("Data Coverage");
        Row covHead = covSheet.createRow(0);
        covHead.createCell(0).setCellValue("application");
        covHead.createCell(1).setCellValue("sources");
        row = 1;
        for (var e : dataCoverage.entrySet()) {
            Row cr = covSheet.createRow(row++);
            cr.createCell(0).setCellValue(e.getKey());
            cr.createCell(1).setCellValue(e.getValue());
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        }
        wb.close();
    }
}
