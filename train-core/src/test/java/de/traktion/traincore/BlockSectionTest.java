package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für {@link BlockSection} und {@link BlockSystem} — Z2, T-D23 (Plan Step 8 Testliste).
 *
 * <p>T-D9: Blockabschnitte aus Topologie abgeleitet. Z2: zwei Züge nie im selben Abschnitt.
 * T-D23: triviale Deadlock-Erkennung (Zyklus, nicht aufgelöst).
 */
class BlockSectionTest {

    private static RailGraph twoEdgeGraph() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);
        g.addNode(a);
        g.addNode(b);
        g.addNode(c);
        g.addEdge(new Edge(a, b, RailKind.NORMAL, 0.0, 100.0));
        g.addEdge(new Edge(b, c, RailKind.NORMAL, 0.0, 200.0));
        return g;
    }

    // --- Blockabschnitte aus Topologie ableiten (T-D9) ---

    @Test
    void fromGraph_derivesSectionsFromEdges() {
        RailGraph g = twoEdgeGraph();
        BlockSystem system = BlockSystem.fromGraph(g);
        assertEquals(2, system.sectionCount());
    }

    @Test
    void fromGraph_emptyGraph_zeroSections() {
        RailGraph g = new RailGraph();
        BlockSystem system = BlockSystem.fromGraph(g);
        assertEquals(0, system.sectionCount());
    }

    @Test
    void fromGraph_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> BlockSystem.fromGraph(null));
    }

    @Test
    void sectionFor_findsSectionByEdge() {
        RailGraph g = twoEdgeGraph();
        BlockSystem system = BlockSystem.fromGraph(g);
        Edge first = g.edges().iterator().next();
        BlockSection s = system.sectionFor(first);
        assertEquals(first, s.edge());
    }

    // --- Zwei Züge im selben Abschnitt → Kollision verhindert (Z2) ---

    @Test
    void reserve_firstTokenSucceeds() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        assertTrue(system.reserve(100L, 1L));
        assertFalse(system.isFree(1L));
    }

    @Test
    void reserve_secondTokenFails() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        assertTrue(system.reserve(100L, 1L));
        assertFalse(system.reserve(200L, 1L), "Z2: zweiter Token darf nicht reservieren");
    }

    @Test
    void reserve_sameTokenReReserves() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        assertTrue(system.reserve(100L, 1L));
        assertTrue(system.reserve(100L, 1L), "gleicher Token darf re-reservieren");
    }

    // --- Reservierung freigeben → Abschnitt wieder verfügbar ---

    @Test
    void release_makesSectionAvailable() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        system.reserve(100L, 1L);
        system.release(100L, 1L);
        assertTrue(system.isFree(1L));
        assertTrue(system.reserve(200L, 1L), "nach Freigabe kann anderer Token reservieren");
    }

    @Test
    void release_wrongToken_throws() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        system.reserve(100L, 1L);
        assertThrows(IllegalStateException.class, () -> system.release(200L, 1L));
    }

    @Test
    void release_freeSection_throws() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        assertThrows(IllegalStateException.class, () -> system.release(100L, 1L));
    }

    // --- Deadlock-Erkennung (T-D23, trivial) ---

    @Test
    void noDeadlock_whenNoWaiting() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        assertFalse(system.hasDeadlock());
    }

    @Test
    void noDeadlock_whenSingleWaiter() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        system.reserve(100L, 1L);
        system.reserve(200L, 2L);
        // Token 300 wartet auf 1 (gehalten von 100), aber 100 wartet nicht → kein Zyklus
        system.reserve(300L, 1L);
        assertFalse(system.hasDeadlock());
    }

    @Test
    void deadlock_detectedWhenCycle() {
        // Zwei Abschnitte, zwei Tokens:
        // Token A hält 1, wartet auf 2 (gehalten von B)
        // Token B hält 2, wartet auf 1 (gehalten von A) → Zyklus → Deadlock
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        system.reserve(1L, 1L); // A hält 1
        system.reserve(2L, 2L); // B hält 2
        system.reserve(1L, 2L); // A wartet auf 2 (gehalten von B)
        system.reserve(2L, 1L); // B wartet auf 1 (gehalten von A)
        assertTrue(system.hasDeadlock(), "T-D23: Zyklus muss als Deadlock erkannt werden");
    }

    @Test
    void deadlock_notResolved() {
        // T-D23: Deadlock wird erkannt, NICHT aufgelöst. Auflösung kommt in P5.
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        system.reserve(1L, 1L);
        system.reserve(2L, 2L);
        system.reserve(1L, 2L);
        system.reserve(2L, 1L);
        assertTrue(system.hasDeadlock());
        // Nach Erkennung bleibt der Zustand — kein automatisches Auflösen
        assertTrue(system.hasDeadlock(), "T-D23: Deadlock bleibt erkannt, nicht aufgelöst");
    }

    @Test
    void deadlock_clearedWhenCycleBreaks() {
        BlockSystem system = BlockSystem.fromGraph(twoEdgeGraph());
        system.reserve(1L, 1L);
        system.reserve(2L, 2L);
        system.reserve(1L, 2L);
        system.reserve(2L, 1L);
        assertTrue(system.hasDeadlock());
        // B gibt Abschnitt 2 frei → Zyklus bricht
        system.release(2L, 2L);
        assertFalse(system.hasDeadlock(), "nach Brechen des Zyklus kein Deadlock");
    }

    @Test
    void deadlock_threeTokenCycle() {
        // Drei-Token-Zyklus: A→B→C→A
        RailGraph g = new RailGraph();
        Node n1 = new Node(1L), n2 = new Node(2L), n3 = new Node(3L), n4 = new Node(4L);
        g.addNode(n1); g.addNode(n2); g.addNode(n3); g.addNode(n4);
        g.addEdge(new Edge(n1, n2, RailKind.NORMAL, 0.0, 100.0));
        g.addEdge(new Edge(n2, n3, RailKind.NORMAL, 0.0, 100.0));
        g.addEdge(new Edge(n3, n4, RailKind.NORMAL, 0.0, 100.0));
        BlockSystem system = BlockSystem.fromGraph(g);

        system.reserve(10L, 1L); // A hält 1
        system.reserve(20L, 2L); // B hält 2
        system.reserve(30L, 3L); // C hält 3
        system.reserve(10L, 2L); // A wartet auf 2 (B)
        system.reserve(20L, 3L); // B wartet auf 3 (C)
        system.reserve(30L, 1L); // C wartet auf 1 (A) → Zyklus
        assertTrue(system.hasDeadlock(), "Drei-Token-Zyklus muss erkannt werden");
    }

    // --- BlockSection direkt ---

    @Test
    void blockSection_nullEdge_throws() {
        assertThrows(IllegalArgumentException.class, () -> new BlockSection(1L, null));
    }

    @Test
    void blockSection_initiallyFree() {
        BlockSection s = new BlockSection(1L,
            new Edge(new Node(1L), new Node(2L), RailKind.NORMAL, 0.0, 100.0));
        assertTrue(s.isFree());
        assertEquals(1L, s.id());
    }
}
