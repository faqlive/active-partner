package org.example.activepartner.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogService {
    private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 512 * 1024;
    private static final String ROOT_DIR = ".active-partner";
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "app.log";
    private static final String LOG_FILE_ROTATED = "app.log.1";

    private final Path logPath;
    private final Path rotatedLogPath;
    private final DateTimeFormatter formatter;
    private final long maxFileSizeBytes;

    public AppLogService() {
        this(Paths.get(System.getProperty("user.home"), ROOT_DIR, LOG_DIR), DEFAULT_MAX_FILE_SIZE_BYTES);
    }

    AppLogService(Path baseDir, long maxFileSizeBytes) {
        this.logPath = baseDir.resolve(LOG_FILE);
        this.rotatedLogPath = baseDir.resolve(LOG_FILE_ROTATED);
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.maxFileSizeBytes = maxFileSizeBytes;
        ensureDirectory();
    }

    public synchronized void append(String message) {
        try {
            rotateIfNeeded();
            String line = "[" + LocalDateTime.now().format(formatter) + "] " + message + System.lineSeparator();
            Files.write(
                    logPath,
                    line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
        }
    }

    private void rotateIfNeeded() throws IOException {
        if (Files.exists(logPath) && Files.size(logPath) >= maxFileSizeBytes) {
            Files.deleteIfExists(rotatedLogPath);
            Files.move(logPath, rotatedLogPath);
        }
    }

    private void ensureDirectory() {
        try {
            Files.createDirectories(logPath.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create log directory", exception);
        }
    }
}
