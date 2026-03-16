package org.example.activepartner.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class AppLogServiceTest {

    @Test
    void appendsLogLinesToFile() throws IOException {
        Path tempDir = Files.createTempDirectory("app-log-test");
        AppLogService service = new AppLogService(tempDir, 1024 * 1024);

        service.append("first line");
        service.append("second line");

        Path logPath = tempDir.resolve("app.log");
        String content = new String(Files.readAllBytes(logPath), StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("first line"));
        Assertions.assertTrue(content.contains("second line"));
    }

    @Test
    void rotatesLogWhenSizeLimitReached() throws IOException {
        Path tempDir = Files.createTempDirectory("app-log-rotate");
        AppLogService service = new AppLogService(tempDir, 120);

        service.append("line 1 with more content for size growth");
        service.append("line 2 with more content for size growth");
        service.append("line 3 with more content for size growth");
        service.append("line 4 with more content for size growth");

        Path rotated = tempDir.resolve("app.log.1");
        Path current = tempDir.resolve("app.log");
        Assertions.assertTrue(Files.exists(rotated));
        Assertions.assertTrue(Files.exists(current));
    }
}
