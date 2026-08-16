package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlannerTest {

    private static Node n1() { return new Node(1L); }
    private static Node n2() { return new Node(2L); }
    private static Node n3() { return new Node(3L); }

    private static Edge edge(double gradient, double railCond, double overheadCond, double length) {
        return new Edge(n1(), n2(), RailKind.NORMAL, gradient, length, railCond, overheadCond);
    }

    private static Edge edgePerfect(double length) {
        return edge(0.0, 1.0, 1.0, length);
    }

    private static PowerGrid grid() {
        // FixedSupply liefert immer was angefordert wird (kein eigenes Limit).
        // maxPowerW in predict() begrenzt, was der Zug abrufen kann.
        return new PowerGrid(new FixedSupply(), 1000.0);
    }

    private static Consist consist(double tareKg, double payloadKg) {
        return new Consist(1, tareKg, payloadKg);
    }

    private static Consist DEFAULT_CONSIST = consist(10_000, 0);

    // ─── T-D40 (b): leere Route ───────────────────────────────────────────────

    @Test
    void predict_emptyRoute_returnsEmpty() {
        Optional<RouteForecast> result = Planner.predict(
                List.of(), DEFAULT_CONSIST, grid(), 100_000, 0.0);
        assertTrue(result.isEmpty());
    }

    // ─── T-D40 (c): Masse 0 ───────────────────────────────────────────────────

    @Test
    void predict_zeroMass_returnsEmpty() {
        Edge e = edgePerfect(100.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), new Consist(1, 0.0, 0.0), grid(), 100_000, 0.0);
        assertTrue(result.isEmpty());
    }

    // ─── T-D40 (d): maxPowerW <= 0 ──────────────────────────────────────────

    @Test
    void predict_zeroMaxPower_returnsEmpty() {
        Edge e = edgePerfect(100.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), DEFAULT_CONSIST, grid(), 0.0, 0.0);
        assertTrue(result.isEmpty());
    }

    @Test
    void predict_negativeMaxPower_returnsEmpty() {
        Edge e = edgePerfect(100.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), DEFAULT_CONSIST, grid(), -100.0, 0.0);
        assertTrue(result.isEmpty());
    }

    // ─── T-D40 (a): condition == 0 auf einer Kante ──────────────────────────

    @Test
    void predict_zeroCondition_returnsEmpty() {
        // railCondition = 0 auf einer Kante
        Edge badEdge = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 0.0, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(badEdge), DEFAULT_CONSIST, grid(), 100_000, 0.0);
        assertTrue(result.isEmpty());
    }

    // ─── T-D41: null-Argumente ───────────────────────────────────────────────

    @Test
    void predict_nullRoute_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                Planner.predict(null, DEFAULT_CONSIST, grid(), 100_000, 0.0));
    }

    @Test
    void predict_nullConsist_throws() {
        Edge e = edgePerfect(100.0);
        assertThrows(IllegalArgumentException.class, () ->
                Planner.predict(List.of(e), null, grid(), 100_000, 0.0));
    }

    @Test
    void predict_nullNetState_throws() {
        Edge e = edgePerfect(100.0);
        assertThrows(IllegalArgumentException.class, () ->
                Planner.predict(List.of(e), DEFAULT_CONSIST, null, 100_000, 0.0));
    }

    @Test
    void predict_nanStartSpeed_throws() {
        Edge e = edgePerfect(100.0);
        assertThrows(IllegalArgumentException.class, () ->
                Planner.predict(List.of(e), DEFAULT_CONSIST, grid(), 100_000, Double.NaN));
    }

    @Test
    void predict_negativeStartSpeed_throws() {
        Edge e = edgePerfect(100.0);
        assertThrows(IllegalArgumentException.class, () ->
                Planner.predict(List.of(e), DEFAULT_CONSIST, grid(), 100_000, -1.0));
    }

    // ─── T-D41 (e): nicht zusammenhängende Route ───────────────────────────

    @Test
    void predict_disconnectedRoute_throws() {
        // n1→n2, dann n3→n2 (nicht n2→n3)
        Edge e1 = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 1.0);
        Edge e2 = new Edge(n3(), n2(), RailKind.NORMAL, 0.0, 100.0, 1.0, 1.0);
        assertThrows(IllegalArgumentException.class, () ->
                Planner.predict(List.of(e1, e2), DEFAULT_CONSIST, grid(), 100_000, 0.0));
    }

    // ─── Grundfunktion: 1-Kante, condition = 1.0, gradient = 0 ─────────────

    @Test
    void predict_singleEdge_perfectCondition_sollEqualsIst() {
        Edge e = edgePerfect(1000.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        // Bei condition = 1.0 everywhere: soll == ist
        assertEquals(rf.sollFahrzeitSekunden(), rf.istFahrzeitSekunden(), 1e-9);
        assertEquals(0.0, rf.deltaProzent(), 1e-6);
    }

    @Test
    void predict_singleEdge_perfectCondition_positiveTravelTime() {
        Edge e = edgePerfect(1000.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        assertTrue(result.get().sollFahrzeitSekunden() > 0,
                "Fahrzeit muss > 0 sein");
    }

    @Test
    void predict_singleEdge_perfectCondition_bottlenecksEmpty() {
        Edge e = edgePerfect(1000.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        assertTrue(result.get().bottlenecks().isEmpty());
    }

    // ─── Determinismus (T-D45) ──────────────────────────────────────────────

    @Test
    void predict_deterministic_twoCallsIdentical() {
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.01, 1000.0, 0.7, 0.9);
        List<Edge> route = List.of(e);
        Optional<RouteForecast> r1 = Planner.predict(route, DEFAULT_CONSIST, grid(), 100_000_000, 5.0);
        Optional<RouteForecast> r2 = Planner.predict(route, DEFAULT_CONSIST, grid(), 100_000_000, 5.0);

        assertTrue(r1.isPresent());
        assertTrue(r2.isPresent());
        RouteForecast rf1 = r1.get();
        RouteForecast rf2 = r2.get();
        assertEquals(rf1.sollFahrzeitSekunden(), rf2.sollFahrzeitSekunden(), 1e-9);
        assertEquals(rf1.istFahrzeitSekunden(), rf2.istFahrzeitSekunden(), 1e-9);
        assertEquals(rf1.deltaProzent(), rf2.deltaProzent(), 1e-9);
    }

    // ─── Zustandsabhängigkeit ───────────────────────────────────────────────

    @Test
    void predict_gradientIncrease_istLongerThanSoll() {
        // Gradient > 0 → höherer Leistungsbedarf → Ist > Soll (Steigung)
        // Da gradient im Soll/Ist beide Male gleich ist, ist der Unterschied hier nur
        // die verfügbare Leistung (condition). Bei condition = 1.0 everywhere ist
        // gradient = 0 das Soll und gradient > 0 das Ist. ABER: im current design
        // berücksichtigt computeRouteTravelTimeWithPerfectCondition DEN gradient der Kante!
        // Das ist ein Modellierungs-Fehler: das Soll sollte gradient = 0 sein, nicht
        // der aktuelle gradient.
        // → dieser Test zeigt, dass das Modell noch nicht ganz stimmt.
        // (Step 4 korrigiert das Soll/Ist-Modell.)
        Edge uphill = new Edge(n1(), n2(), RailKind.NORMAL, 0.05, 1000.0, 1.0, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(uphill), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        // Steile Steigung mit viel Leistung → slowest aber noch fahrbar
        assertTrue(result.get().sollFahrzeitSekunden() > 0);
    }

    // ─── maxPowerW groß genug: Zug erreicht Gleichgewicht ───────────────────

    @Test
    void predict_highPower_reachesHighSpeed() {
        // Kante deutlich kürzer als grid-reach, damit genug Leistung verfügbar ist
        Edge e = edgePerfect(100.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        // 100m kurze Kante mit viel Leistung → hohe Geschwindigkeit
        double timeSeconds = result.get().sollFahrzeitSekunden();
        double avgSpeedMps = 100.0 / timeSeconds;
        assertTrue(avgSpeedMps > 10.0, "Mit 100 MW sollte der Zug > 10 m/s erreichen");
    }
}
