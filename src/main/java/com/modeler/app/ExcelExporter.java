package com.modeler.app;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Export various stages of the dependency modeling process to
 * a single multi-sheet Excel workbook for auditing purposes.
 */
public class ExcelExporter {

    /**
     * Write raw claims, claim probabilities, source trust and final
     * dependencies to an Excel workbook.
     *
     * @param rawClaims      list of claims from all adapters
     * @param claimProbs     probability of each claim being true
     * @param sourceTrust    final trustworthiness per source
     * @param finalDeps      resolved dependency graph
     * @param filePath       destination XLSX file
     * @throws IOException if writing fails
     */
    public static void export(List<Claim> rawClaims,
                              Map<Claim, Double> claimProbs,
                              Map<String, Double> sourceTrust,
                              Map<String, Set<String>> finalDeps,
                              String filePath) throws IOException {
        Workbook wb = new XSSFWorkbook();

        // Raw claims sheet
        Sheet rawSheet = wb.createSheet("Raw Claims");
        Row rHead = rawSheet.createRow(0);
        rHead.createCell(0).setCellValue("source");
        rHead.createCell(1).setCellValue("fromApp");
        rHead.createCell(2).setCellValue("toApp");
        rHead.createCell(3).setCellValue("exists");
        rHead.createCell(4).setCellValue("confidence");
        int row = 1;
        for (Claim c : rawClaims) {
            Row r = rawSheet.createRow(row++);
            r.createCell(0).setCellValue(c.source);
            r.createCell(1).setCellValue(c.fromApp);
            r.createCell(2).setCellValue(c.toApp);
            r.createCell(3).setCellValue(c.exists);
            r.createCell(4).setCellValue(c.confidence);
        }

        // Claim probability sheet
        Sheet probSheet = wb.createSheet("Claim Probabilities");
        Row pHead = probSheet.createRow(0);
        pHead.createCell(0).setCellValue("fromApp");
        pHead.createCell(1).setCellValue("toApp");
        pHead.createCell(2).setCellValue("source");
        pHead.createCell(3).setCellValue("truthProb");
        row = 1;
        for (Map.Entry<Claim, Double> e : claimProbs.entrySet()) {
            Claim c = e.getKey();
            Row pr = probSheet.createRow(row++);
            pr.createCell(0).setCellValue(c.fromApp);
            pr.createCell(1).setCellValue(c.toApp);
            pr.createCell(2).setCellValue(c.source);
            pr.createCell(3).setCellValue(e.getValue());
        }

        // Source trust sheet
        Sheet trustSheet = wb.createSheet("Source Trust");
        Row tHead = trustSheet.createRow(0);
        tHead.createCell(0).setCellValue("source");
        tHead.createCell(1).setCellValue("trust");
        row = 1;
        for (Map.Entry<String, Double> e : sourceTrust.entrySet()) {
            Row tr = trustSheet.createRow(row++);
            tr.createCell(0).setCellValue(e.getKey());
            tr.createCell(1).setCellValue(e.getValue());
        }

        // Final dependency graph sheet
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

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        }
        wb.close();
    }
}
