package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteForecastTest {

    private static Node n1() { return new Node(1L); }
    private static Node n2() { return new Node(2L); }
    private static Edge edgeWithCondition(double rail, double overhead) {
        return new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, rail, overhead);
    }

    @Test
    void constructor_withValidArgs_createsRouteForecast() {
        Edge e = edgeWithCondition(0.5, 0.8);
        List<Bottleneck> bottlenecks = List.of(
                new Bottleneck(e, BottleneckArt.SPANNUNG, 5.0)
        );
        RouteForecast rf = new RouteForecast(100.0, 110.0, 10.0, bottlenecks);
        assertEquals(100.0, rf.sollFahrzeitSekunden());
        assertEquals(110.0, rf.istFahrzeitSekunden());
        assertEquals(10.0, rf.deltaProzent());
        assertEquals(1, rf.bottlenecks().size());
    }

    @Test
    void constructor_negativeSollFahrzeit_throws() {
        List<Bottleneck> empty = List.of();
        assertThrows(IllegalArgumentException.class, () ->
                new RouteForecast(-0.01, 100.0, 0.0, empty));
    }

    @Test
    void constructor_negativeIstFahrzeit_throws() {
        List<Bottleneck> empty = List.of();
        assertThrows(IllegalArgumentException.class, () ->
                new RouteForecast(100.0, -0.01, 0.0, empty));
    }

    @Test
    void constructor_nanSollFahrzeit_throws() {
        List<Bottleneck> empty = List.of();
        assertThrows(IllegalArgumentException.class, () ->
                new RouteForecast(Double.NaN, 100.0, 0.0, empty));
    }

    @Test
    void constructor_nanDelta_throws() {
        List<Bottleneck> empty = List.of();
        assertThrows(IllegalArgumentException.class, () ->
                new RouteForecast(100.0, 110.0, Double.NaN, empty));
    }

    @Test
    void constructor_nullBottlenecks_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteForecast(100.0, 110.0, 10.0, null));
    }

    @Test
    void bottlenecks_areDefensiveCopy() {
        Edge e = edgeWithCondition(0.5, 0.8);
        List<Bottleneck> original = new java.util.ArrayList<>();
        original.add(new Bottleneck(e, BottleneckArt.SPANNUNG, 5.0));
        RouteForecast rf = new RouteForecast(100.0, 110.0, 10.0, original);

        // Ändert die Original-Liste die Kopie im Record? (Nein — List.copyOf ist eine defensive Kopie)
        original.clear();
        assertEquals(1, rf.bottlenecks().size());

        // Die Kopie ist unmodifiable — Änderungen von außen sind nicht möglich
        assertThrows(UnsupportedOperationException.class, () ->
                rf.bottlenecks().clear());
    }

    @Test
    void emptyBottlenecksList_allowed() {
        RouteForecast rf = new RouteForecast(100.0, 100.0, 0.0, List.of());
        assertTrue(rf.bottlenecks().isEmpty());
    }

    @Test
    void deltaProzent_mayBeNegative() {
        // Ist < Soll — der Planer kann in manchen Konfigurationen optimistisch sein
        // (z.B. wenn startSpeedMps > 0, der Zug bereits in Bewegung ist)
        RouteForecast rf = new RouteForecast(110.0, 100.0, -9.09, List.of());
        assertEquals(-9.09, rf.deltaProzent());
    }

    @Test
    void equals_and_hashCode_consistent() {
        List<Bottleneck> empty1 = List.of();
        List<Bottleneck> empty2 = List.of();
        RouteForecast rf1 = new RouteForecast(100.0, 110.0, 10.0, empty1);
        RouteForecast rf2 = new RouteForecast(100.0, 110.0, 10.0, empty2);
        assertEquals(rf1, rf2);
        assertEquals(rf1.hashCode(), rf2.hashCode());
    }

    @Test
    void differentDelta_notEquals() {
        List<Bottleneck> empty = List.of();
        RouteForecast rf1 = new RouteForecast(100.0, 110.0, 10.0, empty);
        RouteForecast rf2 = new RouteForecast(100.0, 110.0, 11.0, empty);
        assertNotEquals(rf1, rf2);
    }
}
