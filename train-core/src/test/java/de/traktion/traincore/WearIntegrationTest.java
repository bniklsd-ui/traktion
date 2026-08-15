package de.traktion.traincore;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Z6-Langlauf-Simulation — 10.000 Ticks Dauerbetrieb (T-D33, Plan §5/P2 Step 8).
 *
 * <p>Die Z6-Akzeptanz: nach 10.000 Ticks Dauerbetrieb ist {@code condition < 1.0} messbar
 * degradiert, aber {@code speedMps > 0} immer noch (nicht total blockiert — T-D5).
 *
 * <p>Der Test ist deterministisch (T-D24, T-D13): zwei Läufe mit gleichem Seed liefern
 * gleiche End-conditions und gleiche End-Geschwindigkeit.
 */
class WearIntegrationTest {

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
        g.addEdge(new Edge(a, b, RailKind.NORMAL, 0.0, 500.0));
        g.addEdge(new Edge(b, c, RailKind.NORMAL, 0.0, 500.0));
        g.addEdge(new Edge(c, d, RailKind.NORMAL, 0.0, 500.0));
        return g;
    }

    // --- HAUPT-TEST: 10.000 Ticks Degradation ---

    /**
     * Z6-Akzeptanz (T-D33): nach 10.000 Ticks ist condition messbar degradiert,
     * aber speedMps > 0 (nie total blockiert).
     *
     * <p>Setup: ein schwerer Zug (40t) mit hoher Maximalleistung (1 MW) auf einem kleinen
     * Netz. Die Maximalleistung stellt sicher, dass der Zug nicht durch Strommangel
     * verlangsamt wird — Verschleiß, nicht Elektrik, soll die Bremse sein.
     */
    @Test
    void tenThousandTicks_degradationMeasurable_speedStillPositive() {
        RailGraph g = makeThreeEdgeGraph();
        Edge firstEdge = g.edges().iterator().next();

        // Schwerer Zug: 40t, 1 MW max power (Strom ist nicht der limitierende Faktor)
        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, firstEdge, 0.0);

        // FixedSupply mit großem Reach (kein Strom-Engpass)
        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(token);

        // Zustand vor dem Lauf
        double railBefore = firstEdge.railCondition();
        double overheadBefore = firstEdge.overheadCondition();

        // 10.000 Ticks — 500 Sekunden = ~8.3 Minuten
        sim.run(10_000);

        // Behauptung 1: messbare Degradation auf mindestens einer Kante
        double railAfter = firstEdge.railCondition();
        double overheadAfter = firstEdge.overheadCondition();

        assertTrue(railAfter < railBefore,
                "railCondition should degrade: before=" + railBefore + ", after=" + railAfter);
        assertTrue(railAfter < 1.0,
                "railCondition should be < 1.0 after 10k ticks: " + railAfter);

        // Behauptung 2: token fährt noch (speedMps > 0)
        assertTrue(token.speedMps() > 0,
                "speedMps should be > 0 after 10k ticks (not blocked): " + token.speedMps());

        // Der Zug sollte auch noch auf einer Kante sein (nicht aus dem Netz gefallen)
        assertNotNull(token.edge(), "token should still be on an edge after 10k ticks");

        System.out.println("10k tick result: rail " + railBefore + " → " + railAfter +
                ", speed " + token.speedMps() + " m/s");
    }

    /**
     * Z6: auch overheadCondition degradiert (T-D25 — beide werden degradiert).
     */
    @Test
    void tenThousandTicks_overheadAlsoDegrades() {
        RailGraph g = makeThreeEdgeGraph();
        Edge firstEdge = g.edges().iterator().next();

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, firstEdge, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(token);

        double overheadBefore = firstEdge.overheadCondition();
        sim.run(10_000);
        double overheadAfter = firstEdge.overheadCondition();

        assertTrue(overheadAfter < overheadBefore,
                "overheadCondition should also degrade: before=" + overheadBefore + ", after=" + overheadAfter);
    }

    /**
     * T-D5: kontinuierlich, nie blockierend. Selbst nach 10k Ticks ist speed > 0.
     */
    @Test
    void tenThousandTicks_neverBlocked() {
        RailGraph g = makeThreeEdgeGraph();
        Edge firstEdge = g.edges().iterator().next();

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, firstEdge, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(token);

        // Prüfe speed alle 1000 Ticks
        for (int check = 1000; check <= 10_000; check += 1000) {
            sim.run(1000);
            assertTrue(token.speedMps() > 0,
                    "speedMps should be > 0 at tick " + check + ": " + token.speedMps());
        }
    }

    // --- DETERMINISMUS (T-D24) ---

    /**
     * Zwei Läufe mit gleichem Seed → gleiche End-conditions und End-Geschwindigkeit.
     */
    @Test
    void determinism_sameSeedSameEndState() {
        RailGraph g1 = makeThreeEdgeGraph();
        Edge e1 = g1.edges().iterator().next();

        RailGraph g2 = makeThreeEdgeGraph();
        Edge e2 = g2.edges().iterator().next();

        long seed = 12345L;

        // Lauf 1
        Consist c1 = new Consist(4, 40_000.0, 0.0);
        Token t1 = new Token(1L, c1, 1_000_000.0, e1, 0.0);
        Simulator s1 = new Simulator(new PowerGrid(new FixedSupply(), 10_000.0), seed);
        s1.addToken(t1);
        s1.run(10_000);

        // Lauf 2
        Consist c2 = new Consist(4, 40_000.0, 0.0);
        Token t2 = new Token(1L, c2, 1_000_000.0, e2, 0.0);
        Simulator s2 = new Simulator(new PowerGrid(new FixedSupply(), 10_000.0), seed);
        s2.addToken(t2);
        s2.run(10_000);

        // Gleiche End-conditions
        assertEquals(e1.railCondition(), e2.railCondition(), TOLERANCE,
                "railCondition should be equal with same seed");
        assertEquals(e1.overheadCondition(), e2.overheadCondition(), TOLERANCE,
                "overheadCondition should be equal with same seed");

        // Gleiche End-Geschwindigkeit
        assertEquals(t1.speedMps(), t2.speedMps(), TOLERANCE,
                "speedMps should be equal with same seed");

        // Gleiche Position
        assertEquals(t1.progressMeters(), t2.progressMeters(), TOLERANCE,
                "progressMeters should be equal with same seed");
    }

    // --- VERGLEICH: Degradiertes Netz vs. frisches Netz ---

    /**
     * Ein Zug auf einem degradierten Netz ist langsamer als auf einem frischen Netz
     * (T-D5: "Fahrzeit ↑" — Geschwindigkeit ↓ bei gleichem Stromangebot).
     *
     * <p>Dies ist die vollständige Wirkungs-Kette: Verschleiß → condition ↓ → availW ↓ → speed ↓
     */
    @Test
    void degradedNetwork_slowerThanFresh() {
        // Frisches Netz
        RailGraph freshG = makeThreeEdgeGraph();
        Edge freshEdge = freshG.edges().iterator().next();

        Consist freshConsist = new Consist(4, 40_000.0, 0.0);
        Token freshToken = new Token(1L, freshConsist, 1_000_000.0, freshEdge, 0.0);
        Simulator freshSim = new Simulator(new PowerGrid(new FixedSupply(), 10_000.0), 99L);
        freshSim.addToken(freshToken);
        freshSim.run(1_000); // 1000 Ticks

        // Degradiertes Netz: vorher 5000 Ticks Verschleiß
        RailGraph degG = makeThreeEdgeGraph();
        Edge degEdge = degG.edges().iterator().next();

        // Künstlich degradiert auf 50%
        degEdge.repairRail(-0.5); // -0.5 von 1.0 = 0.5
        degEdge.repairOverhead(-0.5);

        Consist degConsist = new Consist(4, 40_000.0, 0.0);
        Token degToken = new Token(1L, degConsist, 1_000_000.0, degEdge, 0.0);
        Simulator degSim = new Simulator(new PowerGrid(new FixedSupply(), 10_000.0), 99L);
        degSim.addToken(degToken);
        degSim.run(1_000); // gleiche Ticks, gleicher Seed

        // Auf frischem Netz ist der Zug schneller
        assertTrue(freshToken.speedMps() > degToken.speedMps(),
                "fresh should be faster: fresh=" + freshToken.speedMps() +
                        " m/s, degraded=" + degToken.speedMps() + " m/s");
    }

    // --- EDGE-FALL: Einzelne Kante, langer Token ---

    /**
     * Auf einer einzelnen langen Kante (10km): der Token fährt lange, degradiert die Kante.
     */
    @Test
    void longSingleEdge_degradesOverTime() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        g.addNode(a);
        g.addNode(b);
        Edge edge = new Edge(a, b, RailKind.NORMAL, 0.0, 10_000.0);
        g.addEdge(edge);

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, edge, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 20_000.0);
        Simulator sim = new Simulator(grid, 77L);
        sim.addToken(token);

        double before = edge.railCondition();
        sim.run(5_000);
        double after = edge.railCondition();

        assertTrue(after < before,
                "railCondition should degrade on long edge: " + before + " → " + after);
        assertTrue(token.speedMps() > 0,
                "speed still positive: " + token.speedMps());
    }

    // --- 10.000 TICKS: Speed-Nachweis ---

    /**
     * Beweis, dass nach 10.000 Ticks auf einer einzelnen Kante (500m) der Zug
     * immer noch fährt. Er hat die Kante evtl. mehrfach durchfahren.
     */
    @Test
    void tenThousandTicks_singleEdge_tokenStillMoving() {
        RailGraph g = new RailGraph();
        Node a = new Node(1L);
        Node b = new Node(2L);
        g.addNode(a);
        g.addNode(b);
        Edge edge = new Edge(a, b, RailKind.NORMAL, 0.0, 500.0);
        g.addEdge(edge);

        Consist consist = new Consist(4, 40_000.0, 0.0);
        Token token = new Token(1L, consist, 1_000_000.0, edge, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 10_000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(token);

        sim.run(10_000);

        assertTrue(token.speedMps() > 0,
                "token should still be moving after 10k ticks: " + token.speedMps() + " m/s");
        // Der Token hat die Kante in dieser Zeit mehrmals durchfahren können
        assertTrue(token.progressMeters() > 500.0,
                "token should have traversed the edge: " + token.progressMeters() + "m");
    }
}
