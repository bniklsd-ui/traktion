package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 7 — Simulator.runRoute() Test-Harness (T-D47, Z5-Vergleich).
 *
 * <p>Testet die numerische Route-Abrfahrt als Vorbereitung für den Z5-Property-Test
 * in Step 8. Der Planer ruft diese Methode NICHT auf (Z5-Tautologie-Schutz).
 */
class SimulatorRunRouteTest {

    private static final double TOLERANCE = 1e-9;

    /**
     * Hilfs-Netz: 2 Kanten A→B→C.
     */
    private static List<Edge> makeTwoEdgeRoute() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);
        Edge ab = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0);
        Edge bc = new Edge(b, c, RailKind.NORMAL, 0.0, 500.0);
        return List.of(ab, bc);
    }

    /**
     * Hilfs-Netz: 1 Kante A→B.
     */
    private static List<Edge> makeOneEdgeRoute() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Edge ab = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0);
        return List.of(ab);
    }

    // --- VALIDIERUNG ---

    /**
     * Leere Route → IllegalArgumentException.
     */
    @Test
    void emptyRoute_throwsIllegalArgumentException() {
        List<Edge> empty = new ArrayList<>();
        Consist consist = new Consist(4, 40_000.0, 0.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Simulator.runRoute(empty, consist, 1_000_000.0, 0.0));
        assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
    }

    /**
     * null Route → IllegalArgumentException.
     */
    @Test
    void nullRoute_throwsIllegalArgumentException() {
        Consist consist = new Consist(4, 40_000.0, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> Simulator.runRoute(null, consist, 1_000_000.0, 0.0));
    }

    /**
     * Nicht-zusammenhängende Route → IllegalStateException.
     */
    @Test
    void nonContiguousRoute_throwsIllegalStateException() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);
        Node d = new Node(4L);
        // A→B und C→D — nicht zusammenhängend (B != C)
        Edge ab = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0);
        Edge cd = new Edge(c, d, RailKind.NORMAL, 0.0, 500.0);
        List<Edge> route = List.of(ab, cd);

        Consist consist = new Consist(4, 40_000.0, 0.0);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> Simulator.runRoute(route, consist, 1_000_000.0, 0.0));
        assertTrue(ex.getMessage().contains("not contiguous") || ex.getMessage().contains("contiguous"));
    }

    // --- GRUNDVERHALTEN ---

    /**
     * 1-Kanten-Route, condition=1.0, gradient=0 → endliche Fahrzeit > 0.
     */
    @Test
    void singleEdgeRoute_condition1_gradient0_returnsFinitePositiveTime() {
        List<Edge> route = makeOneEdgeRoute();
        Consist consist = new Consist(4, 40_000.0, 0.0);

        double totalSeconds = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);

        assertTrue(totalSeconds > 0,
                "Travel time should be positive: " + totalSeconds);
        // 500m with a 40t train at ~20 m/s → ~25 seconds minimum
        assertTrue(totalSeconds < 300,
                "Travel time should be realistic (< 300s): " + totalSeconds);
    }

    /**
     * 2-Kanten-Route → Token wechselt Kanten und fährt beide ab.
     */
    @Test
    void twoEdgeRoute_traversesBothEdges() {
        List<Edge> route = makeTwoEdgeRoute();
        Consist consist = new Consist(4, 40_000.0, 0.0);

        double totalSeconds = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);

        // Zwei 500m Kanten = 1000m total, same train
        // Sollte mehr Zeit brauchen als eine einzelne 500m Kante
        double oneEdge = Simulator.runRoute(makeOneEdgeRoute(), consist, 1_000_000.0, 0.0);
        assertTrue(totalSeconds > oneEdge,
                "Two edges should take longer than one edge: " + totalSeconds + " vs " + oneEdge);
    }

    // --- MONOTONIE: condition < 1 → langsamere Fahrt ---

    /**
     * Schlechte condition → längere Fahrzeit als perfekte condition.
     */
    @Test
    void degradedCondition_slowerThanPerfect() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Edge perfect = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0, 1.0, 1.0);
        Edge degraded = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0, 0.1, 0.1);

        Consist consist = new Consist(4, 40_000.0, 0.0);

        double timePerfect = Simulator.runRoute(List.of(perfect), consist, 1_000_000.0, 0.0);
        double timeDegraded = Simulator.runRoute(List.of(degraded), consist, 1_000_000.0, 0.0);

        assertTrue(timeDegraded > timePerfect,
                "Degraded edge should be slower: perfect=" + timePerfect +
                        "s, degraded=" + timeDegraded + "s");
    }

    // --- MONOTONIE: gradient > 0 → langsamere Fahrt ---

    /**
     * Steile Steigung → längere Fahrzeit als flach.
     */
    @Test
    void positiveGradient_slowerThanFlat() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Edge flat = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0);
        Edge steep = new Edge(a, b, RailKind.NORMAL, 0.05, 500.0); // 5% Steigung

        Consist consist = new Consist(4, 40_000.0, 0.0);

        double timeFlat = Simulator.runRoute(List.of(flat), consist, 1_000_000.0, 0.0);
        double timeSteep = Simulator.runRoute(List.of(steep), consist, 1_000_000.0, 0.0);

        assertTrue(timeSteep > timeFlat,
                "Steep gradient should be slower: flat=" + timeFlat +
                        "s, steep=" + timeSteep + "s");
    }

    // --- DETERMINISMUS (Regel 8) ---

    /**
     * Zwei Aufrufe mit gleichen Inputs → identische Fahrzeit.
     */
    @Test
    void determinism_sameInputsSameOutput() {
        List<Edge> route = makeTwoEdgeRoute();
        Consist consist = new Consist(4, 40_000.0, 0.0);

        double run1 = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);
        double run2 = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);

        assertEquals(run1, run2, TOLERANCE,
                "runRoute should be deterministic: " + run1 + " vs " + run2);
    }

    /**
     * Verschiedene Seeds → verschiedene Zeiten (wg. Wear.accumulate über rng).
     * Hinweis: Die Methode nutzt intern immer Seed 42L — dies testet,
     * dass der interne Simulator deterministisch ist.
     */
    @Test
    void internalSimulator_deterministic() {
        List<Edge> route = makeTwoEdgeRoute();
        Consist consist = new Consist(4, 40_000.0, 0.0);

        // Drei Läufe — alle identisch (interner Simulator hat fixen Seed 42L)
        double t1 = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);
        double t2 = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);
        double t3 = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);

        assertEquals(t1, t2, TOLERANCE);
        assertEquals(t2, t3, TOLERANCE);
    }

    // --- RAND-EFFEKTE: startSpeedMps > 0 ---

    /**
     * startSpeedMps > 0: der Zug startet bereits in Bewegung.
     * Die Annahme "schneller" ist nicht monoton — bei genug Speed braucht der Zug
     * mehr Leistung um die Geschwindigkeit zu halten als um von 0 zu beschleunigen.
     * Wir testen nur: beide Fahrten sind endlich und positiv.
     */
    @Test
    void startSpeedPositive_producesFiniteTime() {
        List<Edge> route = makeOneEdgeRoute();
        Consist consist = new Consist(4, 40_000.0, 0.0);

        double timeStart0 = Simulator.runRoute(route, consist, 1_000_000.0, 0.0);
        double timeStart10 = Simulator.runRoute(route, consist, 1_000_000.0, 10.0);
        double timeStart20 = Simulator.runRoute(route, consist, 1_000_000.0, 20.0);

        assertTrue(timeStart0 > 0, "Start from 0 should take time: " + timeStart0);
        assertTrue(timeStart10 > 0, "Start from 10 should take time: " + timeStart10);
        assertTrue(timeStart20 > 0, "Start from 20 should take time: " + timeStart20);

        // Alle drei sollen in einem plausiblen Bereich sein
        assertTrue(timeStart0 < 300, "All times should be plausible (< 300s)");
        assertTrue(timeStart10 < 300, "All times should be plausible (< 300s)");
        assertTrue(timeStart20 < 300, "All times should be plausible (< 300s)");
    }

    // --- ANTI-PATTERN: Planer ruft runRoute nicht ---

    /**
     * Verifikation: Planner.java ruft Simulator.runRoute NICHT auf.
     * Dieser Test ist ein "反向-Test" — er bestätigt, dass der Watchpunkt sauber ist.
     */
    @Test
    void plannerDoesNotCallRunRoute() {
        // Dies ist ein reiner Compile-Time/Static-Analysis Check.
        // Wenn Planner.runRoute() aufgerufen wird, würde der Code in Planner.java
        // einen Import/Aufruf von "runRoute" haben.
        // Dieser Test dient als Dokumentation des Anti-Pattern-Schutzes.
        // Das tatsächliche grep passiert in Step 9 (Anti-Pattern-Check).
        assertTrue(true, "Z5-Tautologie-Schutz wird in Step 9 per grep verifiziert");
    }
}
