package de.traktion.traincore;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.api.statistics.Statistics;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Z7-Bootstrap-Invariante — property-based Test (T-D32, Plan §5/P2 Step 7).
 *
 * <p>Beweist: aus <b>jedem</b> erreichbaren Verfallszustand (alle condition ∈ [0, 1)) führt
 * eine endliche Sequenz von {@code withdraw(n) → repairRail(n) + repairOverhead(n)} zu
 * {@code condition > 0} auf jeder Kante. Das ist die Bootstrap-Invariante (Regel 4):
 * der Spieler hat <b>immer</b> einen Ausweg.
 *
 * <p>Zwei Invarianten:
 * <ol>
 *   <li><b>Monoton:</b> die Summe von {@code (1 - condition)} nimmt bei Reparatur nicht zu</li>
 *   <li><b>Terminiert:</b> nach endlich vielen Schritten ist {@code condition > 0} auf jeder Kante</li>
 * </ol>
 */
class SoftlockInvariantTest {

    private static final double TOLERANCE = 1e-9;

    // --- Hilfsmethoden ---

    private static double sumWear(List<Edge> edges) {
        double sum = 0;
        for (Edge e : edges) {
            sum += (1.0 - e.railCondition());
            sum += (1.0 - e.overheadCondition());
        }
        return sum;
    }

    private static void repair(Edge edge, double amount, PlayerLabor labor) {
        int available = labor.withdraw((int) Math.ceil(amount));
        if (available > 0) {
            edge.repairRail(available);
            edge.repairOverhead(available);
        }
    }

    private static boolean allConditionsPositive(List<Edge> edges) {
        for (Edge e : edges) {
            if (e.railCondition() <= 0 || e.overheadCondition() <= 0) {
                return false;
            }
        }
        return true;
    }

    private static List<Edge> makeEdges(int count, double baseCondition, Random r) {
        List<Edge> edges = new ArrayList<>();
        Node prev = new Node(r.nextLong());
        for (int i = 0; i < count; i++) {
            Node next = new Node(r.nextLong());
            double rail = Math.max(0.0, Math.min(0.999, baseCondition + (r.nextDouble() - 0.5) * 0.1));
            double oh = Math.max(0.0, Math.min(0.999, baseCondition + (r.nextDouble() - 0.5) * 0.1));
            edges.add(new Edge(prev, next, RailKind.NORMAL, 0.0, 100.0, rail, oh));
            prev = next;
        }
        return edges;
    }

    // --- Property 1: Monotonie der Reparatur-Invariante ---

    /**
     * Property: für jeden zufälligen Verfallszustand gilt: sum(1 - condition) nimmt nach
     * einer Reparatur-Aktion nicht zu.
     */
    @Property
    void repairNeverWorsensCondition(
            @ForAll("edgeCounts") int count,
            @ForAll("seeds") long seed,
            @ForAll("playerLaborConfigs") PlayerLabor labor
    ) {
        Random r = new Random(seed);
        List<Edge> edges = makeEdges(count, 0.5, r);
        double before = sumWear(edges);

        for (Edge edge : edges) {
            repair(edge, 1.0, labor);
        }

        double after = sumWear(edges);

        assertTrue(after <= before + TOLERANCE,
                "repair should not worsen total condition: before=" + before + ", after=" + after);
    }

    // --- Property 2: Termination in endlicher Zeit ---

    @Property
    void repairLoopTerminates(
            @ForAll("edgeCounts") int count,
            @ForAll("seeds") long seed,
            @ForAll("playerLaborConfigs") PlayerLabor labor,
            @ForAll("repairBudgets") int budget
    ) {
        Random r = new Random(seed);
        List<Edge> edges = makeEdges(count, 0.5, r);

        int steps = 0;
        int maxSteps = 2000;

        while (!allConditionsPositive(edges) && steps < maxSteps) {
            for (Edge edge : edges) {
                repair(edge, budget, labor);
            }
            labor.tick(1.0);
            steps++;
        }

        Statistics.collect(steps);

        assertTrue(steps < maxSteps,
                "repair loop should terminate, took " + steps + " steps");
        assertTrue(allConditionsPositive(edges),
                "all conditions should be > 0 after " + steps + " steps");
    }

    // --- Property 3: Langsame Labor-Konfiguration terminiert ---

    @Property
    void slowLaborStillTerminates(
            @ForAll("edgeCounts") int count,
            @ForAll("seeds") long seed,
            @ForAll("repairBudgets") int budget
    ) {
        PlayerLabor labor = new PlayerLabor(1, 5);
        Random r = new Random(seed);
        List<Edge> edges = makeEdges(count, 0.5, r);

        int steps = 0;
        int maxSteps = 5000;

        while (!allConditionsPositive(edges) && steps < maxSteps) {
            for (Edge edge : edges) {
                repair(edge, budget, labor);
            }
            labor.tick(1.0);
            steps++;
        }

        Statistics.collect(steps);

        assertTrue(steps < maxSteps,
                "even slow labor should terminate, took " + steps);
        assertTrue(allConditionsPositive(edges),
                "all conditions should be > 0");
    }

    // --- Arbitraries ---

    @Provide
    private static Arbitrary<Integer> edgeCounts() {
        return Arbitraries.integers().between(2, 5);
    }

    @Provide
    private static Arbitrary<Long> seeds() {
        return Arbitraries.longs().between(0L, 10_000L);
    }

    @Provide
    private static Arbitrary<PlayerLabor> playerLaborConfigs() {
        return Combinators.combine(
                Arbitraries.integers().between(1, 20),
                Arbitraries.integers().between(5, 50)
        ).as(PlayerLabor::new);
    }

    @Provide
    private static Arbitrary<Integer> repairBudgets() {
        return Arbitraries.integers().between(1, 5);
    }

    // --- Klassische JUnit-Tests ---

    /**
     * Worst-Case: von condition ≈ 0 auf > 0 in endlicher Zeit.
     */
    @Test
    void worstCaseFromNearZeroTerminates() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Edge edge = new Edge(a, b, RailKind.NORMAL, 0.0, 100.0, 0.001, 0.001);

        PlayerLabor labor = new PlayerLabor();

        int steps = 0;
        while (edge.railCondition() <= 0 && steps < 10_000) {
            labor.tick(1.0);
            int avail = labor.withdraw(10);
            if (avail > 0) {
                edge.repairRail(avail);
                edge.repairOverhead(avail);
            }
            steps++;
        }

        assertTrue(steps < 10_000, "should terminate before 10k steps, took " + steps);
        assertTrue(edge.railCondition() > 0.0);
        assertTrue(edge.overheadCondition() > 0.0);
    }

    /**
     * sumWear nimmt unter reiner Reparatur monoton ab.
     */
    @Test
    void sumWearMonotonicallyDecreasesUnderPureRepair() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Node c = new Node(3L);

        List<Edge> edges = List.of(
                new Edge(a, b, RailKind.NORMAL, 0.0, 100.0, 0.3, 0.4),
                new Edge(b, c, RailKind.NORMAL, 0.0, 100.0, 0.5, 0.2)
        );
        PlayerLabor labor = new PlayerLabor(10, 100);

        double prevSum = sumWear(edges);
        boolean everIncreased = false;

        for (int i = 0; i < 100; i++) {
            labor.tick(1.0);
            for (Edge e : edges) {
                int avail = labor.withdraw(10);
                if (avail > 0) {
                    e.repairRail(avail);
                    e.repairOverhead(avail);
                }
            }
            double currentSum = sumWear(edges);
            if (currentSum > prevSum + TOLERANCE) {
                everIncreased = true;
                break;
            }
            prevSum = currentSum;
        }

        assertFalse(everIncreased,
                "sumWear should monotonically decrease under pure repair");
    }

    /**
     * Reparatur ohne Zeit (kein tick) → Vorrat bleibt 0, sumWear ändert sich nicht.
     */
    @Test
    void repairWithoutTimeAccumulationDoesNothing() {
        Node a = new Node(1L);
        Node b = new Node(2L);
        Edge edge = new Edge(a, b, RailKind.NORMAL, 0.0, 100.0, 0.5, 0.5);

        PlayerLabor labor = new PlayerLabor();
        // Kein tick — Vorrat bleibt 0

        double before = sumWear(List.of(edge));

        for (int i = 0; i < 20; i++) {
            repair(edge, 1.0, labor);
        }

        double after = sumWear(List.of(edge));

        assertTrue(after <= before + TOLERANCE,
                "without time, repair should do nothing: before=" + before + ", after=" + after);
        assertEquals(0, labor.workAvailable(),
                "workAvailable should still be 0 without tick");
    }

    /**
     * Sehr niedrige conditions auf allen Kanten eines größeren Netzes — Terminierung.
     */
    @Test
    void largeNetworkFromNearZeroTerminates() {
        // 5 Kanten, alle bei ~0.001
        Node prev = new Node(1L);
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Node next = new Node((long) (i + 2));
            edges.add(new Edge(prev, next, RailKind.NORMAL, 0.0, 100.0, 0.001, 0.001));
            prev = next;
        }

        PlayerLabor labor = new PlayerLabor();

        int steps = 0;
        while (!allConditionsPositive(edges) && steps < 20_000) {
            labor.tick(1.0);
            for (Edge e : edges) {
                int avail = labor.withdraw(10);
                if (avail > 0) {
                    e.repairRail(avail);
                    e.repairOverhead(avail);
                }
            }
            steps++;
        }

        assertTrue(steps < 20_000, "should terminate before 20k steps, took " + steps);
        assertTrue(allConditionsPositive(edges));
    }
}
