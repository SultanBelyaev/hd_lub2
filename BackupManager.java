package com.example.filedatabaseapphse;

import java.io.*;

public class BackupManager {
    private final File databaseFile;

    public BackupManager(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    public File createBackup() throws IOException {
        File backupFile = new File(databaseFile.getParent(), databaseFile.getName() + ".bak");
        try (InputStream in = new FileInputStream(databaseFile);
             OutputStream out = new FileOutputStream(backupFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
        return backupFile;
    }

    public void restoreFromBackup() throws IOException {
        File backupFile = new File(databaseFile.getParent(), databaseFile.getName() + ".bak");
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Резервная копия не найдена.");
        }
        try (InputStream in = new FileInputStream(backupFile);
             OutputStream out = new FileOutputStream(databaseFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}
