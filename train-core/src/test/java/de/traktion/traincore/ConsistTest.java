package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für {@link Consist} — T-D7 (Plan Step 4 Testliste).
 *
 * <p>Kein {@code ItemStack}, kein Item-Typ — nur {@code double}/{@code int} (Regel 7).
 */
class ConsistTest {

    @Test
    void construct_validConsist() {
        Consist c = new Consist(1, 40_000.0, 0.0);
        assertEquals(1, c.carCount());
        assertEquals(40_000.0, c.tareMassKg());
        assertEquals(0.0, c.payloadMassKg());
    }

    @Test
    void totalMassKg_isTarePlusPayload() {
        Consist c = new Consist(3, 120_000.0, 80_000.0);
        assertEquals(200_000.0, c.totalMassKg());
    }

    @Test
    void totalMassKg_zeroPayload() {
        Consist c = new Consist(1, 40_000.0, 0.0);
        assertEquals(40_000.0, c.totalMassKg());
    }

    // --- Negative Massen abgelehnt (Invarianten: Masse ≥ 0) ---

    @Test
    void negativeTareMass_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Consist(1, -1.0, 0.0));
    }

    @Test
    void negativePayloadMass_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Consist(1, 40_000.0, -1.0));
    }

    @Test
    void nanTareMass_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Consist(1, Double.NaN, 0.0));
    }

    @Test
    void infinitePayloadMass_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Consist(1, 40_000.0, Double.POSITIVE_INFINITY));
    }

    // --- carCount ≥ 1 (ein leerer Zugverband ist kein Zugverband) ---

    @Test
    void carCountZero_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Consist(0, 40_000.0, 0.0));
    }

    @Test
    void carCountNegative_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Consist(-1, 40_000.0, 0.0));
    }

    @Test
    void carCountLarge_accepted() {
        Consist c = new Consist(100, 4_000_000.0, 0.0);
        assertEquals(100, c.carCount());
        assertTrue(c.totalMassKg() > 0);
    }
}
