package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für {@link Simulator} — Z3, T-D13, T-D24 (Plan Step 7 Testliste).
 *
 * <p>Regel 2: der Simulator ruft {@link Physics#requiredPowerW} auf (kein Formel-Duplikat).
 * Determinismus (T-D24): zwei Läufe mit gleichem Seed → gleicher Endzustand.
 */
class SimulatorTest {

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

    // --- Token bewegt sich von A nach B über ein festes Netz (Z3) ---

    @Test
    void tokenMovesAlongEdge() {
        RailGraph g = singleEdgeGraph(100.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);
        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim = new Simulator(grid, 42L);
        Token token = new Token(1L, consist, 200_000.0, edge, 0.0);
        sim.addToken(token);

        sim.run(20); // 20 Ticks = 1 Sekunde

        assertTrue(token.progressMeters() > 0, "Token muss sich bewegen: " + token.progressMeters());
        assertTrue(token.speedMps() > 0, "Token muss Geschwindigkeit haben: " + token.speedMps());
    }

    @Test
    void tokenReachesEndOfEdge() {
        RailGraph g = singleEdgeGraph(10.0, 0.0); // kurze Kante
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);
        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim = new Simulator(grid, 42L);
        Token token = new Token(1L, consist, 500_000.0, edge, 0.0); // viel Leistung
        sim.addToken(token);

        sim.run(200); // 10 Sekunden

        assertTrue(token.reachedEndOfEdge(),
            "Token muss End erreichen: progress=" + token.progressMeters()
                + " length=" + edge.lengthMeters());
    }

    // --- Bei ausreichend Strom: Token erreicht B mit erwarteter Geschwindigkeit ---

    @Test
    void sufficientPower_tokenAccelerates() {
        RailGraph g = singleEdgeGraph(1000.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);
        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim = new Simulator(grid, 42L);
        Token token = new Token(1L, consist, 1_000_000.0, edge, 0.0); // 1 MW
        sim.addToken(token);

        sim.run(100); // 5 Sekunden

        assertTrue(token.speedMps() > 1.0,
            "Bei ausreichend Strom muss der Zug schnell sein: " + token.speedMps());
    }

    // --- Bei Unterversorgung: Token wird langsamer (Z3) ---

    @Test
    void underPower_tokenSlower() {
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
        Token poorToken = new Token(1L, consist, 1_000.0, edge, 0.0); // nur 1 kW
        poorSim.addToken(poorToken);
        poorSim.run(100);

        assertTrue(richToken.speedMps() > poorToken.speedMps(),
            "Viel Strom → schneller als wenig Strom: rich=" + richToken.speedMps()
                + " poor=" + poorToken.speedMps());
    }

    @Test
    void zeroPower_tokenDoesNotMove() {
        RailGraph g = singleEdgeGraph(100.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);
        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim = new Simulator(grid, 42L);
        // maxPowerW muss > 0 sein (Token-Constructor), also nehmen wir minimal
        Token token = new Token(1L, consist, 0.001, edge, 0.0);
        sim.addToken(token);
        sim.run(100);
        // Mit fast null Leistung sollte der Token kaum Fortschritt haben
        assertTrue(token.progressMeters() < 1.0,
            "Bei fast null Leistung kaum Fortschritt: " + token.progressMeters());
    }

    // --- Fixed dt: TICK_SECONDS und N_SUBSTEPS sind Konstanten (T-D13) ---

    @Test
    void fixedDtConstants() {
        assertEquals(0.05, Simulator.TICK_SECONDS, TOLERANCE);
        assertEquals(4, Simulator.DEFAULT_N_SUBSTEPS);
        Simulator sim = new Simulator(new PowerGrid(new FixedSupply()), 42L);
        assertEquals(0.05 / 4, sim.dt(), TOLERANCE);
        assertEquals(4, sim.nSubsteps());
    }

    @Test
    void customSubsteps() {
        Simulator sim = new Simulator(new PowerGrid(new FixedSupply()), 8, 42L);
        assertEquals(8, sim.nSubsteps());
        assertEquals(0.05 / 8, sim.dt(), TOLERANCE);
    }

    // --- Geordnete Iteration: keine HashSet-Iteration (Regel 8, §9) ---

    @Test
    void multipleTokensIteratedInOrder() {
        RailGraph g = singleEdgeGraph(1000.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);
        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim = new Simulator(grid, 42L);
        sim.addToken(new Token(10L, consist, 100_000.0, edge, 0.0));
        sim.addToken(new Token(20L, consist, 100_000.0, edge, 0.0));
        sim.addToken(new Token(30L, consist, 100_000.0, edge, 0.0));

        assertEquals(3, sim.tokens().size());
        assertEquals(10L, sim.tokens().get(0).id());
        assertEquals(20L, sim.tokens().get(1).id());
        assertEquals(30L, sim.tokens().get(2).id());
    }

    // --- Gesäter Zufall: Random mit festem Seed (Regel 8) ---

    @Test
    void seedPreserved() {
        Simulator sim = new Simulator(new PowerGrid(new FixedSupply()), 12345L);
        assertEquals(12345L, sim.seed());
    }

    // --- Determinismus-Test (T-D24): zwei Läufe mit gleichem Seed → gleicher Endzustand ---

    @Test
    void determinism_sameSeedSameEndState() {
        RailGraph g = singleEdgeGraph(500.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);

        // Lauf 1
        PowerGrid grid1 = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim1 = new Simulator(grid1, 99L);
        Token t1 = new Token(1L, consist, 200_000.0, edge, 0.0);
        sim1.addToken(t1);
        sim1.run(100);

        // Lauf 2 — gleicher Seed, gleicher Graph, gleicher Consist
        PowerGrid grid2 = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim2 = new Simulator(grid2, 99L);
        Token t2 = new Token(1L, consist, 200_000.0, edge, 0.0);
        sim2.addToken(t2);
        sim2.run(100);

        assertEquals(t1.progressMeters(), t2.progressMeters(), TOLERANCE,
            "T-D24: gleicher Seed → gleicher progress");
        assertEquals(t1.speedMps(), t2.speedMps(), TOLERANCE,
            "T-D24: gleicher Seed → gleiche speed");
    }

    @Test
    void determinism_differentSeedMayDiffer() {
        // Mit unterschiedlichem Seed kann der Endzustand abweichen (in P1 nicht, da kein
        // Zufall in der Physik — aber der Test etabliert die API für P3)
        RailGraph g = singleEdgeGraph(500.0, 0.0);
        Edge edge = firstEdge(g);
        Consist consist = new Consist(1, 40_000.0, 0.0);

        PowerGrid grid1 = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim1 = new Simulator(grid1, 1L);
        Token t1 = new Token(1L, consist, 200_000.0, edge, 0.0);
        sim1.addToken(t1);
        sim1.run(100);

        PowerGrid grid2 = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator sim2 = new Simulator(grid2, 2L);
        Token t2 = new Token(1L, consist, 200_000.0, edge, 0.0);
        sim2.addToken(t2);
        sim2.run(100);

        // In P1 (kein Zufall in Physik) sind sie gleich — das ist ok, der Test dokumentiert
        // die Erwartung. P3/P5 fügen Zufall hinzu, dann unterscheiden sie sich.
        assertEquals(t1.progressMeters(), t2.progressMeters(), TOLERANCE,
            "P1: kein Zufall in Physik → gleicher Endzustand trotz unterschiedlichem Seed");
    }

    // --- Simulator ruft Physics.requiredPowerW auf (Regel 2 — kein Formel-Duplikat) ---
    // (Indirekt getestet: der Simulator bewegt den Token basierend auf reqW aus Physics.
    //  Ein Duplikat würde hier nicht auffallen, aber der grep-Check in Step 10 prüft es.)

    @Test
    void uphillTokenNeedsMorePower() {
        RailGraph gUp = singleEdgeGraph(1000.0, 0.02); // 2 % bergauf
        RailGraph gFlat = singleEdgeGraph(1000.0, 0.0);
        Edge edgeUp = firstEdge(gUp);
        Edge edgeFlat = firstEdge(gFlat);
        Consist consist = new Consist(1, 40_000.0, 0.0);

        PowerGrid grid = new PowerGrid(new FixedSupply(), 1000.0);
        Simulator simUp = new Simulator(grid, 42L);
        Token tUp = new Token(1L, consist, 200_000.0, edgeUp, 0.0);
        simUp.addToken(tUp);
        simUp.run(100);

        Simulator simFlat = new Simulator(grid, 42L);
        Token tFlat = new Token(1L, consist, 200_000.0, edgeFlat, 0.0);
        simFlat.addToken(tFlat);
        simFlat.run(100);

        assertTrue(tFlat.progressMeters() > tUp.progressMeters(),
            "Bergauf braucht mehr Leistung → weniger Fortschritt: flat=" + tFlat.progressMeters()
                + " up=" + tUp.progressMeters());
    }

    // --- Eingabe-Validierung ---

    @Test
    void nullPowerGrid_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Simulator(null, 42L));
    }

    @Test
    void zeroSubsteps_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Simulator(new PowerGrid(new FixedSupply()), 0, 42L));
    }

    @Test
    void negativeTicks_throws() {
        Simulator sim = new Simulator(new PowerGrid(new FixedSupply()), 42L);
        assertThrows(IllegalArgumentException.class, () -> sim.run(-1));
    }

    @Test
    void nullToken_throws() {
        Simulator sim = new Simulator(new PowerGrid(new FixedSupply()), 42L);
        assertThrows(IllegalArgumentException.class, () -> sim.addToken(null));
    }
}
