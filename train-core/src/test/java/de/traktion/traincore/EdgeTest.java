package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeTest {

    private static Node n1() { return new Node(1L); }
    private static Node n2() { return new Node(2L); }

    @Test
    void defaultConditions_areOne() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0);
        assertEquals(1.0, e.railCondition());
        assertEquals(1.0, e.overheadCondition());
    }

    @Test
    void explicitConditions_setCorrectly() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.7, 0.9);
        assertEquals(0.7, e.railCondition());
        assertEquals(0.9, e.overheadCondition());
    }

    @Test
    void railCondition_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, -0.1, 1.0));
        assertThrows(IllegalArgumentException.class, () ->
                new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.1, 1.0));
        assertThrows(IllegalArgumentException.class, () ->
                new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, Double.NaN, 1.0));
    }

    @Test
    void overheadCondition_outOfRange_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, -0.1));
        assertThrows(IllegalArgumentException.class, () ->
                new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 1.1));
    }

    @Test
    void effectiveCondition_isMinOfBoth() {
        Edge e1 = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.5, 0.8);
        assertEquals(0.5, e1.effectiveCondition());

        Edge e2 = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.9, 0.3);
        assertEquals(0.3, e2.effectiveCondition());

        Edge e3 = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 1.0);
        assertEquals(1.0, e3.effectiveCondition());
    }

    @Test
    void repairRail_increasesRailCondition() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.5, 1.0);
        e.repairRail(0.2);
        assertEquals(0.7, e.railCondition());
        assertEquals(1.0, e.overheadCondition()); // unchanged
    }

    @Test
    void repairOverhead_increasesOverheadCondition() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 0.5);
        e.repairOverhead(0.2);
        assertEquals(1.0, e.railCondition()); // unchanged
        assertEquals(0.7, e.overheadCondition());
    }

    @Test
    void repairRail_clampsAtOne() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.9, 1.0);
        e.repairRail(0.5);
        assertEquals(1.0, e.railCondition());
    }

    @Test
    void repairOverhead_clampsAtOne() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 0.9);
        e.repairOverhead(0.5);
        assertEquals(1.0, e.overheadCondition());
    }

    @Test
    void repairRail_negativeAmount_decreasesCondition() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.5, 1.0);
        e.repairRail(-0.2);
        assertEquals(0.3, e.railCondition());
        assertEquals(1.0, e.overheadCondition());
    }

    @Test
    void repairRail_clampsAtZero() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.1, 1.0);
        e.repairRail(-0.5);
        assertEquals(0.0, e.railCondition());
    }

    @Test
    void repairOverhead_clampsAtZero() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 0.1);
        e.repairOverhead(-0.5);
        assertEquals(0.0, e.overheadCondition());
    }

    @Test
    void repairRail_doesNotChangeOverhead() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.5, 0.6);
        e.repairRail(0.1);
        assertEquals(0.6, e.overheadCondition()); // unchanged
    }

    @Test
    void repairOverhead_doesNotChangeRail() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.5, 0.6);
        e.repairOverhead(0.1);
        assertEquals(0.5, e.railCondition()); // unchanged
    }

    @Test
    void repairRail_amountIsNanOrInfinite_throws() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.5, 1.0);
        assertThrows(IllegalArgumentException.class, () -> e.repairRail(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> e.repairRail(Double.POSITIVE_INFINITY));
    }

    @Test
    void repairOverhead_amountIsNaNOrInfinite_throws() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 0.5);
        assertThrows(IllegalArgumentException.class, () -> e.repairOverhead(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> e.repairOverhead(Double.POSITIVE_INFINITY));
    }

    // --- optionale jqwik property tests ---
    // (Optional gemäß Plan §5/P2 Step 2 —jqwik-Arbitrary-Referenzen via String müssen
    // noch verifiziert werden; die JUnit-Tests decken die Invarianten ab.)

}
