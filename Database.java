package com.example.filedatabaseapphse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class Database {
    public File databaseFile;

    public Database(File file) {
        this.databaseFile = file;
    }

    public void create(String[] headers) throws IOException {
        try (FileWriter writer = new FileWriter(databaseFile)) {
            writer.write(String.join(",", headers) + "\n");
        }
    }

    public void addRecord(String[] record) throws IOException {
        try (FileWriter writer = new FileWriter(databaseFile, true)) {
            writer.write(String.join(",", record) + "\n");
        }
    }

    public List<String[]> search(String field, String value) throws IOException {
        long startTime = System.nanoTime();
        List<String[]> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile))) {
            String line;
            String[] headers = reader.readLine().split(",");
            int fieldIndex = Arrays.asList(headers).indexOf(field);

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[fieldIndex].equals(value)) {
                    results.add(fields);
                }
            }
        }
        long endTime = System.nanoTime();
        System.out.println("Поиск занял: " + (endTime - startTime) / 1_000_000.0 + " мс");
        return results;
    }


    public void deleteRecords(String field, String value) throws IOException {
        File tempFile = new File(databaseFile.getAbsolutePath() + ".tmp");
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile));
             FileWriter writer = new FileWriter(tempFile)) {

            String line;
            String[] headers = reader.readLine().split(",");
            writer.write(String.join(",", headers) + "\n");
            int fieldIndex = Arrays.asList(headers).indexOf(field);

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (!fields[fieldIndex].equals(value)) {
                    writer.write(String.join(",", fields) + "\n");
                }
            }
        }
        if (!databaseFile.delete() || !tempFile.renameTo(databaseFile)) {
            throw new IOException("Ошибка при обновлении файла базы данных.");
        }
    }


    public void editRecord(String keyField, String keyValue, String[] newRecord) throws IOException {
        File tempFile = new File(databaseFile.getAbsolutePath() + ".tmp");
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile));
             FileWriter writer = new FileWriter(tempFile)) {

            String line;
            String[] headers = reader.readLine().split(",");
            writer.write(String.join(",", headers) + "\n");
            int keyIndex = Arrays.asList(headers).indexOf(keyField);

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[keyIndex].equals(keyValue)) {
                    writer.write(String.join(",", newRecord) + "\n");
                } else {
                    writer.write(String.join(",", fields) + "\n");
                }
            }
        }
        if (!databaseFile.delete() || !tempFile.renameTo(databaseFile)) {
            throw new IOException("Ошибка при обновлении файла базы данных.");
        }
    }

    public void exportToExcel(File excelFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile));
             Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Database");

            String line;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < fields.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(fields[i]);
                }
            }

            try (FileOutputStream out = new FileOutputStream(excelFile)) {
                workbook.write(out);
            }
        }
    }
}
