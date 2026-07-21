package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für {@link Physics#requiredPowerW} — Z3-Vorbereitung, Regel 2 (Plan Step 5 Testliste).
 *
 * <p>Die EINE Physikfunktion. P3 (Planer) und Step 7 (Simulator) rufen dieselbe auf —
 * ein Duplikat wäre ein Regel-2-Verstoß (§9).
 */
class PhysicsTest {

    private static final Consist HEAVY = new Consist(1, 40_000.0, 60_000.0); // 100 t
    private static final double TOLERANCE = 1e-6;

    // --- ebene Strecke (gradient = 0) → Leistung > 0 bei v > 0, Masse > 0 ---

    @Test
    void requiredPowerW_level_positiveAtSpeed() {
        double p = Physics.requiredPowerW(HEAVY, 20.0, 0.0);
        assertTrue(p > 0, "Leistung muss positiv sein auf ebener Strecke bei v>0, Masse>0: " + p);
    }

    // --- steigt mit Steigung (gradient > 0) → mehr Leistung als eben ---

    @Test
    void requiredPowerW_uphill_moreThanLevel() {
        double level = Physics.requiredPowerW(HEAVY, 20.0, 0.0);
        double uphill = Physics.requiredPowerW(HEAVY, 20.0, 0.01);
        assertTrue(uphill > level,
            "Leistung bergauf muss größer sein als eben: " + uphill + " vs " + level);
    }

    // --- sinkt bei Gefälle (gradient < 0) → Rekuperation: Leistung kann negativ werden (Z3) ---

    @Test
    void requiredPowerW_downhill_lessThanLevel() {
        double level = Physics.requiredPowerW(HEAVY, 20.0, 0.0);
        double downhill = Physics.requiredPowerW(HEAVY, 20.0, -0.01);
        assertTrue(downhill < level,
            "Leistung bergab muss kleiner sein als eben: " + downhill + " vs " + level);
    }

    @Test
    void requiredPowerW_steepDownhill_negativeRecuperation() {
        // Steiles Gefälle: F_grade überwiegt F_roll + F_air → P negativ (Rekuperation, Z3)
        double p = Physics.requiredPowerW(HEAVY, 5.0, -0.05);
        assertTrue(p < 0,
            "Bei steilem Gefälle muss Leistung negativ sein (Rekuperation): " + p);
    }

    // --- bei v = 0 → 0 (stehender Zug braucht keine Fahrleistung) ---

    @Test
    void requiredPowerW_zeroSpeed_zeroPower() {
        double p = Physics.requiredPowerW(HEAVY, 0.0, 0.01);
        assertEquals(0.0, p, TOLERANCE);
    }

    // --- bei Masse = 0 → 0 (kein Zug, kein Bedarf) ---

    @Test
    void requiredPowerW_zeroMass_zeroPower() {
        Consist empty = new Consist(1, 0.0, 0.0);
        double p = Physics.requiredPowerW(empty, 20.0, 0.01);
        assertEquals(0.0, p, TOLERANCE);
    }

    // --- Monotonie in Masse (mehr Masse → mehr Leistung, bei sonst gleich) ---

    @Test
    void requiredPowerW_monotonicInMass() {
        Consist light = new Consist(1, 40_000.0, 0.0);
        Consist heavy = new Consist(1, 40_000.0, 60_000.0);
        double pLight = Physics.requiredPowerW(light, 20.0, 0.0);
        double pHeavy = Physics.requiredPowerW(heavy, 20.0, 0.0);
        assertTrue(pHeavy > pLight,
            "Mehr Masse → mehr Leistung: " + pHeavy + " vs " + pLight);
    }

    // --- Monotonie in Steigung (mehr Steigung → mehr Leistung, bei sonst gleich) ---

    @Test
    void requiredPowerW_monotonicInGradient() {
        double p0 = Physics.requiredPowerW(HEAVY, 20.0, 0.0);
        double p1 = Physics.requiredPowerW(HEAVY, 20.0, 0.005);
        double p2 = Physics.requiredPowerW(HEAVY, 20.0, 0.02);
        assertTrue(p1 > p0);
        assertTrue(p2 > p1);
    }

    // --- Endlichkeit (kein NaN, kein Unendlich für endliche Eingaben) ---

    @Test
    void requiredPowerW_finiteForFiniteInputs() {
        double p = Physics.requiredPowerW(HEAVY, 50.0, 0.03);
        assertTrue(Double.isFinite(p), "Leistung muss endlich sein für endliche Eingaben: " + p);
    }

    // --- Eingabe-Validierung ---

    @Test
    void requiredPowerW_nullConsist_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> Physics.requiredPowerW(null, 20.0, 0.0));
    }

    @Test
    void requiredPowerW_negativeSpeed_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> Physics.requiredPowerW(HEAVY, -1.0, 0.0));
    }

    @Test
    void requiredPowerW_nanSpeed_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> Physics.requiredPowerW(HEAVY, Double.NaN, 0.0));
    }

    @Test
    void requiredPowerW_nanGradient_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> Physics.requiredPowerW(HEAVY, 20.0, Double.NaN));
    }

    @Test
    void requiredPowerW_infiniteGradient_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> Physics.requiredPowerW(HEAVY, 20.0, Double.POSITIVE_INFINITY));
    }
}
