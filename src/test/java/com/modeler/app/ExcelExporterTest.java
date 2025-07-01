package com.modeler.app;

import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelExporterTest {
    @Test
    public void rawSheetContainsTimestampAndMetadata() throws Exception {
        List<Claim> raw = List.of(new Claim("test", "A", "B", true, 1.0, "t1", "m1"));
        Map<String,String> normMap = Collections.emptyMap();
        List<Claim> norm = Collections.emptyList();
        List<Claim> neg = Collections.emptyList();
        Map<InitialAggregator.Pair, Double> agg = Collections.emptyMap();
        List<Map<String, Double>> iters = Collections.emptyList();
        Map<String, Set<String>> deps = Collections.emptyMap();
        Map<String, Integer> cov = Collections.emptyMap();

        Path file = Files.createTempFile("export", ".xlsx");
        ExcelExporter.export(raw, normMap, norm, neg, agg, iters, deps, cov, file.toString());

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
}
