package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BottleneckTest {

    private static Node n1() { return new Node(1L); }
    private static Node n2() { return new Node(2L); }
    private static Edge edge() {
        return new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.5, 0.8);
    }

    @Test
    void constructor_withValidArgs_createsBottleneck() {
        Edge e = edge();
        Bottleneck b = new Bottleneck(e, BottleneckArt.SPANNUNG, 5.0);
        assertSame(e, b.edge());
        assertEquals(BottleneckArt.SPANNUNG, b.art());
        assertEquals(5.0, b.beitragSekunden());
    }

    @Test
    void constructor_nullEdge_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Bottleneck(null, BottleneckArt.SPANNUNG, 5.0));
    }

    @Test
    void constructor_nullArt_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Bottleneck(edge(), null, 5.0));
    }

    @Test
    void constructor_negativeBeitrag_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Bottleneck(edge(), BottleneckArt.SPANNUNG, -0.01));
    }

    @Test
    void constructor_nanBeitrag_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Bottleneck(edge(), BottleneckArt.SPANNUNG, Double.NaN));
    }

    @Test
    void constructor_infiniteBeitrag_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Bottleneck(edge(), BottleneckArt.SPANNUNG, Double.POSITIVE_INFINITY));
    }

    @Test
    void constructor_zeroBeitrag_allowed() {
        // 0 ist erlaubt — der Bottleneck wird in diesem Fall nicht in die Top-3 aufgenommen
        Bottleneck b = new Bottleneck(edge(), BottleneckArt.SPANNUNG, 0.0);
        assertEquals(0.0, b.beitragSekunden());
    }

    @Test
    void bottleneckArt_hasThreeValues() {
        BottleneckArt[] values = BottleneckArt.values();
        assertEquals(3, values.length);
        assertEquals(BottleneckArt.SPANNUNG, values[0]);
        assertEquals(BottleneckArt.STEIGUNG, values[1]);
        assertEquals(BottleneckArt.KOMBI, values[2]);
    }

    @Test
    void differentArt_notEquals() {
        Edge e = edge();
        Bottleneck b1 = new Bottleneck(e, BottleneckArt.SPANNUNG, 5.0);
        Bottleneck b2 = new Bottleneck(e, BottleneckArt.STEIGUNG, 5.0);
        assertNotEquals(b1, b2);
    }
}
