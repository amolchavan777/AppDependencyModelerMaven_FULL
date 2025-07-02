package com.modeler.app;

import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileOutputStream;
import java.util.*;

// Additional model classes for new export parameters
import com.modeler.app.ClaimIdentityResolver;
import com.modeler.app.ConflictDetector;
import com.modeler.app.TruthDiscoveryEngineEM;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelExporterTest {
    @Test
    public void rawSheetContainsTimestampAndMetadata() throws Exception {
        List<Claim> raw = List.of(new Claim("test", "A", "B", true, 1.0, "t1", "m1"));
        Map<String,String> normMap = Collections.emptyMap();
        List<Claim> norm = Collections.emptyList();
        List<ClaimIdentityResolver.ResolvedClaim> ids = Collections.emptyList();
        List<Claim> neg = Collections.emptyList();
        List<ConflictDetector.ConflictGroup> conflicts = Collections.emptyList();
        Map<InitialAggregator.Pair, Double> agg = Collections.emptyMap();
        List<Map<String, Double>> iters = Collections.emptyList();
        Map<String, Set<String>> deps = Collections.emptyMap();
        Map<String, Integer> cov = Collections.emptyMap();

        Path file = Files.createTempFile("export", ".xlsx");
        ExcelExporter.export(raw, normMap, norm, ids, neg, conflicts, agg, iters, deps, cov, file.toString());

        try (XSSFWorkbook wb = new XSSFWorkbook(file.toFile())) {
            Sheet sheet = wb.getSheet("Raw Claims");
            Row head = sheet.getRow(0);
            assertEquals("timestamp", head.getCell(5).getStringCellValue());
            assertEquals("metadata", head.getCell(6).getStringCellValue());
            Row r = sheet.getRow(1);
            assertEquals("t1", r.getCell(5).getStringCellValue());
            assertEquals("m1", r.getCell(6).getStringCellValue());
        }
    }

    @Test
    public void negativeSheetIncludesType() throws Exception {
        Claim dbClaim = new Claim("db", "WebPortal", "WebsiteDB", "default_db", true, 1.0);
        Claim otherClaim = new Claim("log", "WebPortal", "AuthGateway", true, 1.0);
        List<Claim> raw = List.of(dbClaim, otherClaim);

        List<Claim> negative = NegativeClaimGenerator.generate(raw);

        Map<String,String> normMap = Collections.emptyMap();
        List<Claim> norm = raw;
        List<ClaimIdentityResolver.ResolvedClaim> ids = Collections.emptyList();
        List<ConflictDetector.ConflictGroup> conflicts = Collections.emptyList();
        Map<InitialAggregator.Pair, Double> agg = Collections.emptyMap();
        List<Map<String, Double>> iters = Collections.emptyList();
        Map<String, Set<String>> deps = Collections.emptyMap();
        Map<String, Integer> cov = Collections.emptyMap();

        Path file = Files.createTempFile("exportNeg", ".xlsx");
        ExcelExporter.export(raw, normMap, norm, ids, negative, conflicts, agg, iters, deps, cov, file.toString());

        try (XSSFWorkbook wb = new XSSFWorkbook(file.toFile())) {
            Sheet sheet = wb.getSheet("Negative Claims");
            Row head = sheet.getRow(0);
            assertEquals("type", head.getCell(3).getStringCellValue());
            Row r = sheet.getRow(1);
            assertEquals("log", r.getCell(0).getStringCellValue());
            assertEquals("WebPortal", r.getCell(1).getStringCellValue());
            assertEquals("WebsiteDB", r.getCell(2).getStringCellValue());
            assertEquals("default_db", r.getCell(3).getStringCellValue());
        }
    }

    @Test
    public void rejectedClaimsSheetWritten() throws Exception {
        List<Claim> claims = List.of(
                new Claim("s1", "A", "B", true, 1.0),
                new Claim("s2", "A", "B", false, 1.0)
        );

        TruthDiscoveryEngineEM engine = new TruthDiscoveryEngineEM();
        engine.runEM(claims);

        Path file = Files.createTempFile("rejected", ".xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Rejected Claims");
            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("FromApp");
            head.createCell(1).setCellValue("ToApp");
            head.createCell(2).setCellValue("Source");
            head.createCell(3).setCellValue("Confidence");

            int row = 1;
            for (Claim c : engine.getRejectedClaims()) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(c.fromApp);
                r.createCell(1).setCellValue(c.toApp);
                r.createCell(2).setCellValue(c.source);
                double score = engine.getClaimProbabilities().getOrDefault(c, 0.0);
                r.createCell(3).setCellValue(String.format("%.3f", score));
            }

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        try (XSSFWorkbook wb = new XSSFWorkbook(file.toFile())) {
            Sheet sheet = wb.getSheet("Rejected Claims");
            assertNotNull(sheet, "Sheet should exist");
            Row header = sheet.getRow(0);
            assertEquals("FromApp", header.getCell(0).getStringCellValue());
            assertEquals("Confidence", header.getCell(3).getStringCellValue());
        }
    }
}
