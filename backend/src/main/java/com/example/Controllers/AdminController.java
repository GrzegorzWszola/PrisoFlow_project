package com.example.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.http.Context;

public class AdminController {
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    public static void testAdmin(Context ctx) {
        try (Connection conn = ds.getConnection()){
            ctx.status(200).result("Hello from backend, admin");
        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    private static final String pathToBackup = 
            System.getenv("BACKUP_PATH") != null 
                ? System.getenv("BACKUP_PATH") 
                : "/app/backups";

    public static void getAllBackups(Context ctx) {
        File backupDir = new File(pathToBackup);

        System.out.println("Looking for backups in: " + backupDir.getAbsolutePath());
        System.out.println("Directory exists: " + backupDir.exists());
        System.out.println("Is directory: " + backupDir.isDirectory());

        if (!backupDir.exists() || !backupDir.isDirectory()) {
            ctx.status(400).json(Map.of(
                "message", "backup dir does not exist",
                "path", backupDir.getAbsolutePath()
            ));
            return;
        }

        File[] files = backupDir.listFiles();
        if (files == null) files = new File[0];

        String[] fileNames = Arrays.stream(files)
            .map(File::getName)
            .toArray(String[]::new);
        
        // Sortuj od najnowszego
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        ctx.status(200).json(fileNames);
    }

    public static void createBackup(Context ctx) {
        String filename = "backup_" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        ) + ".sql";
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

        String fullPath = "/app/backups/" + filename;

        try {
            // Komenda pg_dump przez Runtime
            String dbHost = "db";
            String dbPort = "5432";
            String dbName = dotenv.get("POSTGRES_DB") != null ? dotenv.get("POSTGRES_DB") : "prisonflow";
            String dbUser = dotenv.get("DB_USER") != null ? dotenv.get("DB_USER") : "postgres";
            String dbPass = dotenv.get("DB_PASS") != null ? dotenv.get("DB_PASS") : "postgres";

            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-F", "c",
                "-f", fullPath
            );

            // Ustaw hasło
            pb.environment().put("PGPASSWORD", dbPass);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                ctx.status(200).json(Map.of(
                    "message", "Backup created successfully",
                    "file", filename
                ));
            } else {
                ctx.status(500).json(Map.of(
                    "message", "Error creating backup",
                    "exitCode", exitCode,
                    "output", output.toString()
                ));
            }

        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "message", "Exception: " + e.getMessage()
            ));
            e.printStackTrace();
        }
    }

    public static void restoreBackup(Context ctx){
        String filename = ctx.pathParam("filename");
        
        if (filename == null || filename.isEmpty()) {
            ctx.status(400).json(Map.of("message", "Filename is required"));
            return;
        }

        // Walidacja nazwy pliku (bezpieczeństwo!)
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            ctx.status(400).json(Map.of("message", "Invalid filename"));
            return;
        }

        String fullPath = "/app/backups/" + filename;
        File backupFile = new File(fullPath);
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

        if (!backupFile.exists()) {
            ctx.status(404).json(Map.of("message", "Backup file not found"));
            return;
        }

        try {
            String dbHost = "db";
            String dbPort = "5432";
            String dbName = dotenv.get("POSTGRES_DB") != null ? dotenv.get("POSTGRES_DB") : "prisonflow";
            String dbUser = dotenv.get("DB_USER") != null ? dotenv.get("DB_USER") : "postgres";
            String dbPass = dotenv.get("DB_PASS") != null ? dotenv.get("DB_PASS") : "postgres";

            // UWAGA: pg_restore wymaga flagi -c (clean) lub -d (database)
            ProcessBuilder pb = new ProcessBuilder(
                "pg_restore",
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-c",  // drop existing objects before restoring
                "-v",  // verbose
                fullPath
            );

            Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", dbPass);
            pb.redirectErrorStream(true);

            System.out.println("Starting restore from: " + fullPath);
            Process process = pb.start();

            // Przeczytaj output
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println("pg_restore: " + line);
            }

            int exitCode = process.waitFor();
            System.out.println("pg_restore exit code: " + exitCode);

            if (exitCode == 0) {
                ctx.status(200).json(Map.of(
                    "message", "Database restored successfully",
                    "file", filename
                ));
            } else {
                ctx.status(500).json(Map.of(
                    "message", "Error restoring database",
                    "exitCode", exitCode,
                    "output", output.toString()
                ));
            }

        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "message", "Exception: " + e.getMessage()
            ));
            e.printStackTrace();
        }
    }

    public static void removeBackup(Context ctx) {
        String filename = ctx.pathParam("filename");

        if (filename == null || filename.isEmpty()) {
            ctx.status(400).json(Map.of("message", "Filename is required"));
            return;
        }

        // Bezpieczeństwo – blokada traversalu
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            ctx.status(400).json(Map.of("message", "Invalid filename"));
            return;
        }

        String fullPath = "/app/backups/" + filename;
        File file = new File(fullPath);

        if (!file.exists()) {
            ctx.status(404).json(Map.of("message", "Backup file not found"));
            return;
        }

        boolean deleted = file.delete();

        if (deleted) {
            ctx.status(200).json(Map.of(
                "message", "Backup deleted successfully",
                "file", filename
            ));
        } else {
            ctx.status(500).json(Map.of(
                "message", "Failed to delete backup"
            ));
        }
    }
}
