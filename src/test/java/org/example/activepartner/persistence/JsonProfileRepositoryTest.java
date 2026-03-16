package org.example.activepartner.persistence;

import org.example.activepartner.domain.ActionPoint;
import org.example.activepartner.domain.ActionType;
import org.example.activepartner.domain.AutomationProfile;
import org.example.activepartner.domain.RunConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

class JsonProfileRepositoryTest {

    @Test
    void savesAndLoadsProfile() throws IOException {
        Path tempDir = Files.createTempDirectory("profiles-test");
        JsonProfileRepository repository = new JsonProfileRepository(tempDir);

        RunConfig config = new RunConfig();
        config.setIntervalMs(1000L);
        AutomationProfile profile = new AutomationProfile(
                "My Profile",
                Collections.singletonList(new ActionPoint("P1", 10, 20, ActionType.MOVE_ONLY, true, 0L)),
                config
        );

        repository.save(profile);
        AutomationProfile loaded = repository.load("My Profile");

        Assertions.assertEquals("My Profile", loaded.getName());
        Assertions.assertEquals(1, loaded.getPoints().size());
        Assertions.assertEquals(1000L, loaded.getConfig().getIntervalMs());
    }

    @Test
    void persistsLastUsedProfileName() throws IOException {
        Path tempDir = Files.createTempDirectory("profiles-last-used");
        JsonProfileRepository repository = new JsonProfileRepository(tempDir);

        repository.saveLastUsed("Focus Work");

        Assertions.assertEquals("Focus Work", repository.loadLastUsed());
    }
}
