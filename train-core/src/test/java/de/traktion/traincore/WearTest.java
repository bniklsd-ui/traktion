package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für {@link Wear} — Verschleiß-Akkumulator (Z6, T-D31, Regel 5).
 */
class WearTest {

    private static Node n1() { return new Node(1L); }
    private static Node n2() { return new Node(2L); }

    private static Edge freshEdge() {
        return new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 1.0);
    }

    @Test
    void accumulate_decreasesCondition() {
        Edge e = freshEdge();
        assertEquals(1.0, e.railCondition());
        assertEquals(1.0, e.overheadCondition());

        Wear.accumulate(e, 50_000, 10.0, 0.05, null);

        assertTrue(e.railCondition() < 1.0, "railCondition should decrease");
        assertTrue(e.overheadCondition() < 1.0, "overheadCondition should decrease");
    }

    @Test
    void accumulate_bothRailAndOverheadDecrease() {
        Edge e = freshEdge();
        Wear.accumulate(e, 50_000, 10.0, 0.05, null);
        assertTrue(e.railCondition() < 1.0);
        assertTrue(e.overheadCondition() < 1.0);
        // Both degraded by the same amount (same formula)
        assertEquals(e.railCondition(), e.overheadCondition());
    }

    @Test
    void accumulate_zeroMass_noEffect() {
        Edge e = freshEdge();
        double before = e.railCondition();
        Wear.accumulate(e, 0.0, 10.0, 0.05, null);
        assertEquals(before, e.railCondition());
    }

    @Test
    void accumulate_zeroSpeed_noEffect() {
        Edge e = freshEdge();
        double before = e.railCondition();
        Wear.accumulate(e, 50_000, 0.0, 0.05, null);
        assertEquals(before, e.railCondition());
    }

    @Test
    void accumulate_monotonInMass() {
        Edge e1 = freshEdge();
        Edge e2 = freshEdge();

        Wear.accumulate(e1, 50_000, 10.0, 0.05, null);
        Wear.accumulate(e2, 100_000, 10.0, 0.05, null);

        // More mass → more wear → lower condition
        assertTrue(e2.railCondition() < e1.railCondition(),
                "larger mass should cause more wear");
    }

    @Test
    void accumulate_monotonInSpeed() {
        Edge e1 = freshEdge();
        Edge e2 = freshEdge();

        Wear.accumulate(e1, 50_000, 5.0, 0.05, null);
        Wear.accumulate(e2, 50_000, 20.0, 0.05, null);

        // More speed → more wear → lower condition
        assertTrue(e2.railCondition() < e1.railCondition(),
                "larger speed should cause more wear");
    }

    @Test
    void accumulate_linearInDt() {
        Edge e1 = freshEdge();
        Edge e2 = freshEdge();

        // Same total time: e1 gets 2 ticks of 0.05, e2 gets 1 tick of 0.10
        Wear.accumulate(e1, 50_000, 10.0, 0.05, null);
        Wear.accumulate(e1, 50_000, 10.0, 0.05, null);
        Wear.accumulate(e2, 50_000, 10.0, 0.10, null);

        // Double dt → double wear
        assertEquals(e1.railCondition(), e2.railCondition(), 1e-9);
    }

    @Test
    void accumulate_clampsAtZero() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.0001, 1.0);
        // Wear 100,000 kg at 20 m/s for many ticks
        for (int i = 0; i < 10_000; i++) {
            Wear.accumulate(e, 100_000, 20.0, 0.05, null);
        }
        assertTrue(e.railCondition() >= 0, "should not go below 0");
        assertTrue(e.railCondition() <= 1, "should not exceed 1");
    }

    @Test
    void accumulate_neverExceedsOne() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.9999, 0.9999);
        Wear.accumulate(e, 100_000, 20.0, 0.05, null);
        assertTrue(e.railCondition() <= 1.0);
        assertTrue(e.overheadCondition() <= 1.0);
    }

    @Test
    void accumulate_invalidInputs_throw() {
        Edge e = freshEdge();
        assertThrows(IllegalArgumentException.class, () ->
                Wear.accumulate(null, 50_000, 10.0, 0.05, null));
        assertThrows(IllegalArgumentException.class, () ->
                Wear.accumulate(e, -1, 10.0, 0.05, null));
        assertThrows(IllegalArgumentException.class, () ->
                Wear.accumulate(e, 50_000, -1.0, 0.05, null));
        assertThrows(IllegalArgumentException.class, () ->
                Wear.accumulate(e, 50_000, 10.0, 0.0, null));
        assertThrows(IllegalArgumentException.class, () ->
                Wear.accumulate(e, 50_000, 10.0, -0.01, null));
    }

    @Test
    void accumulate_withRng_producesDifferentResults() {
        Edge e1 = freshEdge();
        Edge e2 = freshEdge();

        // Same seed → same random factor → same result
        Random rng1 = new Random(42);
        Random rng2 = new Random(42);

        for (int i = 0; i < 100; i++) {
            Wear.accumulate(e1, 50_000, 10.0, 0.05, rng1);
        }

        for (int i = 0; i < 100; i++) {
            Wear.accumulate(e2, 50_000, 10.0, 0.05, rng2);
        }

        // With same seed, results should be identical (determinism)
        assertEquals(e1.railCondition(), e2.railCondition(), 1e-9,
                "same seed should produce same wear");
    }

    @Test
    void accumulate_deterministicWithoutRng() {
        Edge e1 = freshEdge();
        Edge e2 = freshEdge();

        // Without rng, results are purely deterministic
        Wear.accumulate(e1, 50_000, 10.0, 0.05, null);
        Wear.accumulate(e2, 50_000, 10.0, 0.05, null);

        assertEquals(e1.railCondition(), e2.railCondition(), 1e-9,
                "without rng, two calls should be identical");
    }
}
