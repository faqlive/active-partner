package org.example.activepartner.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RunConfigValidationTest {

    @Test
    void rejectsIntervalBelowMinimum() {
        RunConfig config = new RunConfig();
        Assertions.assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                config.setIntervalMs(RunConfig.MIN_INTERVAL_MS - 1L);
            }
        });
    }

    @Test
    void rejectsJitterAboveMaximum() {
        RunConfig config = new RunConfig();
        Assertions.assertThrows(IllegalArgumentException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                config.setJitterPx(RunConfig.MAX_JITTER_PX + 1);
            }
        });
    }

    @Test
    void acceptsValidConfiguration() {
        RunConfig config = new RunConfig();
        config.setIntervalMs(1000L);
        config.setJitterPx(5);
        config.setStartDelaySec(2);

        Assertions.assertEquals(1000L, config.getIntervalMs());
        Assertions.assertEquals(5, config.getJitterPx());
        Assertions.assertEquals(2, config.getStartDelaySec());
    }
}
