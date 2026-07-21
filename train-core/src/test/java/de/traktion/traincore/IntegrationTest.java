package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration-Test — der Durchstich-Beweis (Plan §5/P1, Step 9).
 *
 * <p>Beweist, dass {@link RailGraph} + {@link Consist} + {@link Physics} + {@link PowerGrid} +
 * {@link Simulator} + {@link BlockSystem} zusammenarbeiten. Kein neuer Typ — nur ein Test,
 * der alle Komponenten verbindet.
 *
 * <p>Variante 1: Zug fährt A→B mit ausreichend Strom (Z3).
 * Variante 2: Zug wird bei Stromknappheit langsamer (Z3).
 * Variante 3: zwei Züge kollidieren nicht (BlockSection, Z2).
 * Variante 4: Determinismus (T-D24) — zwei Läufe, gleich Seed → gleich Endzustand.
 */
class IntegrationTest {

    private static final double TOLERANCE = 1e-9;

    private static RailGraph singleEdgeGraph(double length, double gradient) {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        g.addNode(a);
        g.addNode(b);
        g.addEdge(new Edge(a, b, RailKind.NORMAL, gradient, length));
        return g;
    }

    private static Edge firstEdge(RailGraph g) {
        return g.edges().iterator().next();
    }

    // --- Variante 1: Zug fährt A→B mit ausreichend Strom (Z3) ---

    @Test
    void trainReachesB_withSufficientPower() {
        RailGraph g = singleEdgeGraph(50.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);
        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim = new Simulator(grid, 42L);
        Token token = new Token(1L, consist, 1_000_000.0, edge, 0.0);
        sim.addToken(token);

        sim.run(400); // 20 Sekunden

        assertTrue(token.reachedEndOfEdge(),
            "Z3: Zug muss B erreichen mit ausreichend Strom. progress="
                + token.progressMeters() + " length=" + edge.lengthMeters());
        assertTrue(token.speedMps() > 0, "Zug muss mit Geschwindigkeit ankommen");
    }

    // --- Variante 2: Zug wird bei Stromknappheit langsamer (Z3) ---

    @Test
    void trainSlowsUnderPowerScarcity() {
        RailGraph g = singleEdgeGraph(1000.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);

        // Viel Strom
        PowerGrid richGrid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator richSim = new Simulator(richGrid, 42L);
        Token richToken = new Token(1L, consist, 1_000_000.0, edge, 0.0);
        richSim.addToken(richToken);
        richSim.run(100);

        // Wenig Strom (kleine maxPowerW)
        PowerGrid poorGrid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator poorSim = new Simulator(poorGrid, 42L);
        Token poorToken = new Token(1L, consist, 5_000.0, edge, 0.0); // 5 kW
        poorSim.addToken(poorToken);
        poorSim.run(100);

        assertTrue(richToken.progressMeters() > poorToken.progressMeters(),
            "Z3: bei Stromknappheit weniger Fortschritt. rich="
                + richToken.progressMeters() + " poor=" + poorToken.progressMeters());
        assertTrue(richToken.speedMps() > poorToken.speedMps(),
            "Z3: bei Stromknappheit langsamere Geschwindigkeit. rich="
                + richToken.speedMps() + " poor=" + poorToken.speedMps());
    }

    // --- Variante 3: zwei Züge kollidieren nicht (BlockSection, Z2) ---

    @Test
    void twoTrainsDoNotCollide_blockSectionPrevents() {
        RailGraph g = singleEdgeGraph(1000.0, 0.0);
        BlockSystem blocks = BlockSystem.fromGraph(g);
        Edge edge = firstEdge(g);

        // Token A reserviert den Abschnitt
        assertTrue(blocks.reserve(1L, 1L), "A reserviert Abschnitt 1");
        // Token B versucht, denselben Abschnitt zu reservieren → verweigert (Z2)
        assertFalse(blocks.reserve(2L, 1L), "Z2: B darf nicht reservieren, wenn A hält");
        assertFalse(blocks.isFree(1L), "Abschnitt bleibt von A gehalten");
        assertEquals(1L, blocks.section(1L).owner());
    }

    @Test
    void twoTrainsOnDifferentSections_bothReserve() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L), b = new Node(2L), c = new Node(3L);
        g.addNode(a); g.addNode(b); g.addNode(c);
        g.addEdge(new Edge(a, b, RailKind.NORMAL, 0.0, 100.0));
        g.addEdge(new Edge(b, c, RailKind.NORMAL, 0.0, 100.0));
        BlockSystem blocks = BlockSystem.fromGraph(g);

        assertTrue(blocks.reserve(1L, 1L), "A reserviert Abschnitt 1");
        assertTrue(blocks.reserve(2L, 2L), "B reserviert Abschnitt 2 (verschieden)");
        assertFalse(blocks.hasDeadlock(), "kein Deadlock bei verschiedenen Abschnitten");
    }

    // --- Variante 4: Determinismus (T-D24) — zwei Läufe, gleich Seed → gleich Endzustand ---

    @Test
    void determinism_twoRunsSameSeedSameEndState() {
        RailGraph g = singleEdgeGraph(500.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);

        // Lauf 1
        PowerGrid grid1 = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim1 = new Simulator(grid1, 77L);
        Token t1 = new Token(1L, consist, 200_000.0, edge, 0.0);
        sim1.addToken(t1);
        sim1.run(200);

        // Lauf 2 — gleicher Seed, gleicher Graph, gleicher Consist
        PowerGrid grid2 = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim2 = new Simulator(grid2, 77L);
        Token t2 = new Token(1L, consist, 200_000.0, edge, 0.0);
        sim2.addToken(t2);
        sim2.run(200);

        assertEquals(t1.progressMeters(), t2.progressMeters(), TOLERANCE,
            "T-D24: gleicher Seed → gleicher progress");
        assertEquals(t1.speedMps(), t2.speedMps(), TOLERANCE,
            "T-D24: gleicher Seed → gleiche speed");
    }

    // --- Vollständiger Durchstich: alle Komponenten in einem Test ---

    @Test
    void fullThroughput_allComponentsWorkTogether() {
        // Graph A→B
        RailGraph g = singleEdgeGraph(100.0, 0.01); // 1 % Steigung
        Edge edge = firstEdge(g);

        // Consist
        Consist consist = new Consist(2, 80_000.0, 40_000.0); // 2 Wagen, 120 t

        // PowerGrid mit FixedSupply
        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);

        // BlockSystem aus Topologie
        BlockSystem blocks = BlockSystem.fromGraph(g);
        assertEquals(1, blocks.sectionCount());

        // Simulator
        Simulator sim = new Simulator(grid, 123L);

        // Token
        Token token = new Token(1L, consist, 500_000.0, edge, 0.0);
        sim.addToken(token);

        // Reserviere den Abschnitt für den Token
        assertTrue(blocks.reserve(1L, 1L));

        // Lasse den Zug fahren
        sim.run(400); // 20 Sekunden

        // Z3: Zug hat sich bewegt
        assertTrue(token.progressMeters() > 0, "Zug muss sich bewegen");
        assertTrue(token.speedMps() > 0, "Zug muss Geschwindigkeit haben");

        // Z2: Abschnitt immer noch von Token 1 gehalten
        assertEquals(1L, blocks.section(1L).owner());

        // Freigabe
        blocks.release(1L, 1L);
        assertTrue(blocks.isFree(1L));
    }
}
