package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke-Test — beweist, dass {@code gradle :train-core:test} läuft.
 * Keine Domänenlogik hier. (P0.2 Step 2)
 */
class SmokeTest {

    @Test
    void harnessRuns() {
        assertTrue(true, "gradle :train-core:test läuft");
    }
}
