package com.example.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.http.Context;

public class AdminController {
    private final DataSource dataSource;
    private final Logger logger;
    private final String pathToBackup;
    private final Dotenv dotenv;

    /**
     * Constructor with dependency injection for testing.
     */
    public AdminController(DataSource dataSource, String backupPath, Dotenv dotenv) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(AdminController.class);
        this.pathToBackup = backupPath;
        this.dotenv = dotenv;
    }

    /**
     * Constructor with DataSource only.
     */
    public AdminController(DataSource dataSource) {
        this(
            dataSource,
            System.getenv("BACKUP_PATH") != null ? System.getenv("BACKUP_PATH") : "/app/backups",
            Dotenv.configure().ignoreIfMissing().load()
        );
    }

    /**
     * Default constructor for production use.
     */
    public AdminController() {
        this(DatabaseConfig.getDataSource());
    }

    public void testAdmin(Context ctx) {
        try (Connection conn = dataSource.getConnection()){
            ctx.status(200).result("Hello from backend, admin");
        } catch (Exception e) {
            logger.error("Couldn't connect to database: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public void getAllBackups(Context ctx) {
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
        
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        ctx.status(200).json(fileNames);
    }

    public void createBackup(Context ctx) {
        String filename = "backup_" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        ) + ".sql";

        String fullPath = pathToBackup + "/" + filename;

        try {
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

    public void restoreBackup(Context ctx){
        String filename = ctx.pathParam("filename");
        
        if (filename == null || filename.isEmpty()) {
            ctx.status(400).json(Map.of("message", "Filename is required"));
            return;
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            ctx.status(400).json(Map.of("message", "Invalid filename"));
            return;
        }

        String fullPath = pathToBackup + "/" + filename;
        File backupFile = new File(fullPath);

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

            ProcessBuilder pb = new ProcessBuilder(
                "pg_restore",
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-c",
                "-v",
                fullPath
            );

            Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", dbPass);
            pb.redirectErrorStream(true);

            System.out.println("Starting restore from: " + fullPath);
            Process process = pb.start();

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

    public void removeBackup(Context ctx) {
        String filename = ctx.pathParam("filename");

        if (filename == null || filename.isEmpty()) {
            ctx.status(400).json(Map.of("message", "Filename is required"));
            return;
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            ctx.status(400).json(Map.of("message", "Invalid filename"));
            return;
        }

        String fullPath = pathToBackup + "/" + filename;
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