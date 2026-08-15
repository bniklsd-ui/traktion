package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für {@link PowerGrid} und {@link PowerSupply} — Z4 mit condition (T-D5, T-D27, Step 4).
 *
 * <p>Regel 3 (zwei Implementierungen): {@link FixedSupply} (hier, Test) und
 * {@code ManualGenerator} (P2, benennbar). Der Test nutzt {@code FixedSupply}.
 */
class PowerGridTest {

    private static final double TOLERANCE = 1e-9;

    // --- P1-Rückwärtskompatibilität: condition = 1.0 reproduziert das P1-Verhalten ---

    @Test
    void availableW_conditionOne_reproducesP1Behavior() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        // Bei condition=1.0: volle Reichweite (P1)
        double at0 = g.availableW(1000.0, 0.0, 1.0, 1.0);
        double at250 = g.availableW(1000.0, 250.0, 1.0, 1.0);
        double at500 = g.availableW(1000.0, 500.0, 1.0, 1.0);
        double at1000 = g.availableW(1000.0, 1000.0, 1.0, 1.0);
        assertTrue(at0 > at250, "at0 > at250");
        assertTrue(at250 > at500, "at250 > at500");
        assertTrue(at500 > at1000, "at500 > at1000");
        assertEquals(0.0, at1000, TOLERANCE);
    }

    @Test
    void availableW_atSource_fullPower() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        double w = g.availableW(1000.0, 0.0, 1.0, 1.0);
        assertEquals(1000.0, w, TOLERANCE);
    }

    @Test
    void availableW_zeroRequest_zeroDelivered() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        double w = g.availableW(0.0, 0.0, 1.0, 1.0);
        assertEquals(0.0, w, TOLERANCE);
    }

    @Test
    void availableW_decreasesWithDistance() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        double at0 = g.availableW(1000.0, 0.0, 1.0, 1.0);
        double at250 = g.availableW(1000.0, 250.0, 1.0, 1.0);
        double at500 = g.availableW(1000.0, 500.0, 1.0, 1.0);
        double at1000 = g.availableW(1000.0, 1000.0, 1.0, 1.0);
        assertTrue(at0 > at250, "at0 > at250: " + at0 + " vs " + at250);
        assertTrue(at250 > at500, "at250 > at500: " + at250 + " vs " + at500);
        assertTrue(at500 > at1000, "at500 > at1000: " + at500 + " vs " + at1000);
        assertEquals(0.0, at1000, TOLERANCE, "bei maxReach: 0");
    }

    @Test
    void availableW_beyondMaxReach_zero() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        double w = g.availableW(1000.0, 1500.0, 1.0, 1.0);
        assertEquals(0.0, w, TOLERANCE);
    }

    @Test
    void availableW_linearDecay() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        // Bei 250 m: reachFactor = 0.75 → effectiveRequest = 750 → FixedSupply liefert 750
        double w = g.availableW(1000.0, 250.0, 1.0, 1.0);
        assertEquals(750.0, w, TOLERANCE);
    }

    @Test
    void resetSubstation_doesNotBreakSupply() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        g.resetSubstation();
        double w = g.availableW(1000.0, 0.0, 1.0, 1.0);
        assertEquals(1000.0, w, TOLERANCE, "nach Reset: volle Leistung");
    }

    // --- condition-Wirkung: lower condition → less power (T-D5, T-D27) ---

    @Test
    void availableW_conditionZero_zero() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        double w = g.availableW(1000.0, 0.0, 0.0, 1.0);
        assertEquals(0.0, w, TOLERANCE, "condition=0 → keine Leitfähigkeit");
    }

    @Test
    void availableW_conditionHalf_lessThanConditionOne() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        double at500c1 = g.availableW(1000.0, 500.0, 1.0, 1.0);
        double at500c05 = g.availableW(1000.0, 500.0, 0.5, 1.0);
        assertTrue(at500c05 < at500c1,
                "condition=0.5 liefert weniger als condition=1.0: " + at500c05 + " < " + at500c1);
    }

    @Test
    void availableW_conditionMonotonIncreasing() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        // Gleiche Distanz (100m), verschiedene Conditions
        // 100m ist innerhalb effectiveReach für alle Conditions ≥ 0.1 (effectiveReach ≥ 100m)
        double c08 = g.availableW(1000.0, 100.0, 0.8, 1.0);
        double c05 = g.availableW(1000.0, 100.0, 0.5, 1.0);
        double c02 = g.availableW(1000.0, 100.0, 0.2, 1.0);
        assertTrue(c08 > c05, "c0.8 > c0.5: " + c08 + " > " + c05);
        assertTrue(c05 > c02, "c0.5 > c0.2: " + c05 + " > " + c02);
    }

    @Test
    void availableW_conditionAtDistanceZero_fullPower() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        // Distanz 0 = am Unterwerk: volle Leistung auch bei degraded condition
        double w = g.availableW(1000.0, 0.0, 0.5, 1.0);
        assertEquals(1000.0, w, TOLERANCE, "am Unterwerk: volle Leistung (kein Spannungsabfall)");
    }

    // --- PowerSupply.supply(requestedW, dtSeconds) liefert höchstens requestedW ---

    @Test
    void fixedSupply_returnsAtMostRequested() {
        FixedSupply s = new FixedSupply();
        assertEquals(500.0, s.supply(500.0, 1.0), TOLERANCE);
        assertEquals(0.0, s.supply(0.0, 1.0), TOLERANCE);
    }

    @Test
    void fixedSupply_rejectsNegativeOrInvalid() {
        FixedSupply s = new FixedSupply();
        assertEquals(0.0, s.supply(-100.0, 1.0), TOLERANCE);
        assertEquals(0.0, s.supply(100.0, 0.0), TOLERANCE);
        assertEquals(0.0, s.supply(100.0, -1.0), TOLERANCE);
        assertEquals(0.0, s.supply(Double.NaN, 1.0), TOLERANCE);
    }

    // --- Eingabe-Validierung PowerGrid ---

    @Test
    void powerGrid_nullSupply_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PowerGrid(null, 1000.0));
    }

    @Test
    void powerGrid_zeroMaxReach_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new PowerGrid(new FixedSupply(), 0.0));
    }

    @Test
    void powerGrid_negativeMaxReach_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new PowerGrid(new FixedSupply(), -1.0));
    }

    @Test
    void availableW_negativeRequest_throws() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        assertThrows(IllegalArgumentException.class,
            () -> g.availableW(-1.0, 0.0, 1.0, 1.0));
    }

    @Test
    void availableW_negativeDistance_throws() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        assertThrows(IllegalArgumentException.class,
            () -> g.availableW(100.0, -1.0, 1.0, 1.0));
    }

    @Test
    void availableW_zeroDt_throws() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        assertThrows(IllegalArgumentException.class,
            () -> g.availableW(100.0, 0.0, 1.0, 0.0));
    }

    @Test
    void availableW_nanDistance_throws() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        assertThrows(IllegalArgumentException.class,
            () -> g.availableW(100.0, Double.NaN, 1.0, 1.0));
    }

    @Test
    void availableW_conditionOutOfRange_throws() {
        PowerGrid g = new PowerGrid(new FixedSupply(), 1000.0);
        assertThrows(IllegalArgumentException.class,
            () -> g.availableW(100.0, 0.0, -0.1, 1.0));
        assertThrows(IllegalArgumentException.class,
            () -> g.availableW(100.0, 0.0, 1.1, 1.0));
        assertThrows(IllegalArgumentException.class,
            () -> g.availableW(100.0, 0.0, Double.NaN, 1.0));
    }
}
