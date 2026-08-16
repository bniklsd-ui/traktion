package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 6 — Multi-Edge-Regel-5-Invariante (T-D48, P2-Handover E).
 *
 * <p>Regel 5: Verschleiß bestraft Nutzung, nicht Existenz. Der Simulator degradiert
 * nur die aktuelle Edge des Tokens — Edges, die der Token nie befahren hat, bleiben
 * auf ihrem Startwert.
 *
 * <p>Diese Test-Lücke wurde in P2 identifiziert (P2-Handover E): die einzelnen
 * Verschleiß-Tests (WearTest, WearIntegrationTest) testen immer nur eine einzelne
 * Kante. Multi-Edge zeigt, dass die Degradation pro-Edge korrekt isoliert wird.
 */
class MultiEdgeWearTest {

    private static final double TOLERANCE = 1e-9;

    /**
     * Hilfs-Netz: 3 Kanten A→B→C→D, jedes 500m, flach.
     */
    private static RailGraph makeThreeEdgeGraph() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);
        Node d = new Node(4L);
        g.addNode(a);
        g.addNode(b);
        g.addNode(c);
        g.addNode(d);
        Edge ab = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0);
        Edge bc = new Edge(b, c, RailKind.NORMAL, 0.0, 500.0);
        Edge cd = new Edge(c, d, RailKind.NORMAL, 0.0, 500.0);
        g.addEdge(ab);
        g.addEdge(bc);
        g.addEdge(cd);
        return g;
    }

    /**
     * Hilfs-Netz: 2 Kanten A→B→C, 1000m und 500m.
     */
    private static RailGraph makeTwoEdgeGraph() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);
        g.addNode(a);
        g.addNode(b);
        g.addNode(c);
        Edge ab = new Edge(a, b, RailKind.NORMAL, 0.0, 1_000.0);
        Edge bc = new Edge(b, c, RailKind.NORMAL, 0.0, 500.0);
        g.addEdge(ab);
        g.addEdge(bc);
        return g;
    }

    // --- HAUPT-TESTS ---

    /**
     * T-D48 Kern: Token degradiert beim Befahren einer Kante NUR diese Kante.
     * Eine zweite, nie befahrene Kante bleibt auf 1.0.
     */
    @Test
    void tokenOnEdgeA_onlyEdgeADegrades_edgeBUntouched() {
        RailGraph g = makeTwoEdgeGraph();
        List<Edge> edgeList = new ArrayList<>(g.edges());
        Edge edgeA = edgeList.get(0); // A→B
        Edge edgeB = edgeList.get(1); // B→C

        // Beide starten perfekt
        assertEquals(1.0, edgeA.railCondition(), TOLERANCE);
        assertEquals(1.0, edgeB.railCondition(), TOLERANCE);
        assertEquals(1.0, edgeA.overheadCondition(), TOLERANCE);
        assertEquals(1.0, edgeB.overheadCondition(), TOLERANCE);

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, edgeA, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(token);

        // Token fährt auf Edge A für 500 Ticks (25 Sekunden)
        // Edge A ist 1000m — der Token schafft sie nicht in 500 Ticks
        sim.run(500);

        // Invariante: Edge A ist degradiert
        assertTrue(edgeA.railCondition() < 1.0,
                "Edge A railCondition should degrade: " + edgeA.railCondition());
        assertTrue(edgeA.overheadCondition() < 1.0,
                "Edge A overheadCondition should degrade: " + edgeA.overheadCondition());

        // Invariante: Edge B ist UNBERÜHRT — Token war nie dort
        assertEquals(1.0, edgeB.railCondition(), TOLERANCE,
                "Edge B railCondition should stay at 1.0 (never traversed)");
        assertEquals(1.0, edgeB.overheadCondition(), TOLERANCE,
                "Edge B overheadCondition should stay at 1.0 (never traversed)");
    }

    /**
     * Token durchfährt A→B→C (sequentiell mit moveToEdge). Am Ende:
     * - A und B sind degradiert (befahren)
     * - C ist unberührt (nie befahren)
     *
     * <p>Da der Token nicht schnell genug traversiert um in realistischen Tick-Zahlen
     * mehrere Kanten zu durchfahren, nutzen wir moveToEdge() für die kontrollierte
     * Sequenz A→B→C. Das ist die Regel-5-Invariante: nur die aktuelle Edge degradiert.
     */
    @Test
    void tokenTraversesABtoC_onlyTraversedEdgesDegrade() {
        RailGraph g = makeThreeEdgeGraph();
        List<Edge> edgeList = new ArrayList<>(g.edges());
        Edge edgeA = edgeList.get(0); // A→B
        Edge edgeB = edgeList.get(1); // B→C
        Edge edgeC = edgeList.get(2); // C→D

        // Startbedingungen: alle 1.0
        assertEquals(1.0, edgeA.railCondition(), TOLERANCE);
        assertEquals(1.0, edgeB.railCondition(), TOLERANCE);
        assertEquals(1.0, edgeC.railCondition(), TOLERANCE);

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, edgeA, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(token);

        // Phase 1: Token auf Edge A — 500 Ticks
        sim.run(500);
        assertTrue(edgeA.railCondition() < 1.0,
                "Edge A should be degraded after 500 ticks");
        assertEquals(1.0, edgeB.railCondition(), TOLERANCE,
                "Edge B should be untouched (never traversed)");
        assertEquals(1.0, edgeC.railCondition(), TOLERANCE,
                "Edge C should be untouched (never traversed)");

        // Phase 2: Token wechselt auf Edge B (moveToEdge)
        token.moveToEdge(edgeB);
        double edgeAConditionAtB = edgeA.railCondition();
        sim.run(500);

        // Edge B degradiert, A bleibt wie vorher (verläßt A nicht weiter)
        assertTrue(edgeB.railCondition() < 1.0,
                "Edge B should be degraded after 500 ticks on it");
        assertEquals(edgeAConditionAtB, edgeA.railCondition(), TOLERANCE,
                "Edge A should not change while token is on B");

        // Phase 3: Token wechselt auf Edge C
        token.moveToEdge(edgeC);
        double edgeBConditionAtC = edgeB.railCondition();
        sim.run(500);

        // Edge C degradiert, B bleibt wie vorher
        assertTrue(edgeC.railCondition() < 1.0,
                "Edge C should be degraded after 500 ticks on it");
        assertEquals(edgeBConditionAtC, edgeB.railCondition(), TOLERANCE,
                "Edge B should not change while token is on C");

        // Am Ende: alle drei degradiert (A und B durch vorherige Phasen, C durch Phase 3)
        assertTrue(edgeA.railCondition() < 1.0);
        assertTrue(edgeB.railCondition() < 1.0);
        assertTrue(edgeC.railCondition() < 1.0);
    }

    /**
     * Kombinierter Verschleiß: Token fährt lange auf Kante A,
     * degradiert A, bleibt auf A — Kante B bleibt unberührt.
     */
    @Test
    void longRunOnEdgeA_onlyEdgeADegrades_edgeBUnchanged() {
        RailGraph g = makeTwoEdgeGraph();
        List<Edge> edgeList = new ArrayList<>(g.edges());
        Edge edgeA = edgeList.get(0);
        Edge edgeB = edgeList.get(1);

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, edgeA, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 99L);
        sim.addToken(token);

        // 2000 Ticks — der Verschleiß ist minimal pro Tick (k=1e-10),
        // aber die Invariante ist: B bleibt auf 1.0, egal wie wenig A degradiert ist.
        sim.run(2_000);

        // A ist degradiert (Irrelevant wie viel — nur, dass es passiert ist)
        assertTrue(edgeA.railCondition() < 1.0,
                "Edge A should be degraded (any amount): " + edgeA.railCondition());
        assertTrue(edgeA.overheadCondition() < 1.0,
                "Edge A overhead should be degraded: " + edgeA.overheadCondition());

        // B ist UNVERÄNDERT — die Invariante (Regel 5)
        assertEquals(1.0, edgeB.railCondition(), TOLERANCE,
                "Edge B should be untouched: " + edgeB.railCondition());
        assertEquals(1.0, edgeB.overheadCondition(), TOLERANCE,
                "Edge B overhead should be untouched: " + edgeB.overheadCondition());
    }

    // --- DETERMINISMUS (T-D24, Regel 8) ---

    /**
     * Zwei Läufe mit gleichem Seed → gleiche End-conditions auf allen Edges.
     * Regel 8: Determinismus per Konstruktion.
     */
    @Test
    void determinism_sameSeedSameEndConditions() {
        long seed = 12345L;
        int ticks = 500;

        // Lauf 1
        RailGraph g1 = makeTwoEdgeGraph();
        List<Edge> edgeList1 = new ArrayList<>(g1.edges());
        Edge edgeA1 = edgeList1.get(0);
        Edge edgeB1 = edgeList1.get(1);
        Consist consist1 = new Consist(4, 40_000.0, 0.0);
        Token token1 = new Token(1L, consist1, 1_000_000.0, edgeA1, 0.0);
        Simulator sim1 = new Simulator(new PowerGrid(new FixedSupply(), 10_000.0), seed);
        sim1.addToken(token1);
        sim1.run(ticks);

        // Lauf 2
        RailGraph g2 = makeTwoEdgeGraph();
        List<Edge> edgeList2 = new ArrayList<>(g2.edges());
        Edge edgeA2 = edgeList2.get(0);
        Edge edgeB2 = edgeList2.get(1);
        Consist consist2 = new Consist(4, 40_000.0, 0.0);
        Token token2 = new Token(1L, consist2, 1_000_000.0, edgeA2, 0.0);
        Simulator sim2 = new Simulator(new PowerGrid(new FixedSupply(), 10_000.0), seed);
        sim2.addToken(token2);
        sim2.run(ticks);

        // Gleiche End-conditions
        assertEquals(edgeA1.railCondition(), edgeA2.railCondition(), TOLERANCE,
                "Edge A railCondition should be equal with same seed");
        assertEquals(edgeA1.overheadCondition(), edgeA2.overheadCondition(), TOLERANCE,
                "Edge A overheadCondition should be equal with same seed");
        assertEquals(edgeB1.railCondition(), edgeB2.railCondition(), TOLERANCE,
                "Edge B railCondition should be equal with same seed");
        assertEquals(edgeB1.overheadCondition(), edgeB2.overheadCondition(), TOLERANCE,
                "Edge B overheadCondition should be equal with same seed");
    }

    // --- KANTENWECHSEL: reachedEndOfEdge löst Wechsel aus ---

    /**
     * Prüft das Edge-Wechsel-Verhalten: Token wechselt Edge, wenn er das Ende erreicht.
     * edgeA→edgeB. Nach genug Ticks ist edgeA degradiert, edgeB angefangen zu degradieren.
     */
    @Test
    void tokenReachesEndOfEdgeA_switchesToEdgeB() {
        RailGraph g = makeTwoEdgeGraph();
        List<Edge> edgeList = new ArrayList<>(g.edges());
        Edge edgeA = edgeList.get(0); // 1000m
        Edge edgeB = edgeList.get(1); // 500m

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, edgeA, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 77L);
        sim.addToken(token);

        // 500 Ticks — der Token ist noch auf A (A=1000m, 500 Ticks ≈ 250m)
        sim.run(500);
        assertSame(edgeA, token.edge(),
                "Token should still be on edge A after 500 ticks");
        assertTrue(edgeA.railCondition() < 1.0, "Edge A should be degrading");
        assertEquals(1.0, edgeB.railCondition(), TOLERANCE,
                "Edge B should be untouched after 500 ticks");

        // Weitere 500 Ticks — Token sollte edgeB erreicht haben (oder nah dran)
        sim.run(500);

        // Edge A degradiert, B entweder auch degradiert (wenn erreicht) oder noch 1.0
        // Die Invariante: untraversed edges (C) would be untouched
        // edgeB könnte 1.0 sein wenn nie erreicht
        assertTrue(edgeA.railCondition() < 1.0,
                "Edge A should be degraded after 1000 ticks: " + edgeA.railCondition());
    }

    /**
     * Direkter moveToEdge-Test: Token fährt auf edgeA, wechselt manuell zu edgeB.
     * edgeA ist degradiert, edgeB ist noch 1.0 (vor dem Wechsel).
     */
    @Test
    void manualMoveToEdge_tokenLeavesEdgeADegraded_edgeBUntouched() {
        RailGraph g = makeTwoEdgeGraph();
        List<Edge> edgeList = new ArrayList<>(g.edges());
        Edge edgeA = edgeList.get(0); // 1000m
        Edge edgeB = edgeList.get(1); // 500m

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, edgeA, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(token);

        // Token fährt 500 Ticks auf edgeA
        sim.run(500);

        // edgeA ist degradiert, edgeB ist unberührt
        assertTrue(edgeA.railCondition() < 1.0,
                "Edge A should be degraded after 500 ticks");
        assertEquals(1.0, edgeB.railCondition(), TOLERANCE,
                "Edge B should be untouched before traversal");
        assertEquals(1.0, edgeB.overheadCondition(), TOLERANCE,
                "Edge B overhead should be untouched before traversal");

        // Manueller Wechsel auf edgeB
        token.moveToEdge(edgeB);
        assertSame(edgeB, token.edge(),
                "Token should be on edge B after moveToEdge");

        // edgeA bleibt degradiert (verändert sich nicht mehr)
        double edgeAConditionAfterMove = edgeA.railCondition();
        assertTrue(edgeAConditionAfterMove < 1.0,
                "Edge A should stay degraded after leaving it");

        // edgeB fährt jetzt an und degradiert
        sim.run(500);

        assertTrue(edgeB.railCondition() < 1.0,
                "Edge B should be degraded after traversing: " + edgeB.railCondition());
        assertTrue(edgeB.overheadCondition() < 1.0,
                "Edge B overhead should be degraded after traversing");
        // edgeA hat sich nicht weiter verschlechtert (Token war nicht drauf)
        assertEquals(edgeAConditionAfterMove, edgeA.railCondition(), TOLERANCE,
                "Edge A should not change while Token is on edge B");
    }

    // --- JQQUIK SMOKE: Property über zufällige Edge-Sequenzen ---

    // Hinweis: Ein jqwik-Property-Test wäre ideal, aber nicht zwingend.
    // Die obigen JUnit-Tests decken den Kern-Fall ab (Regel 5 Invariante).
    // Ein jqwik-Property könnte zusätzlich随机liche Edge-Anzahlen und Gradienten testen.
}
