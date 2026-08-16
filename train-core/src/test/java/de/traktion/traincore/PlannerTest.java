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

    // ─── Step 4: Soll/Ist-Vergleich (T-D38, Z11-Kern) ───────────────────────

    @Test
    void predict_conditionOneAllEdges_deltaZero() {
        // T-D38 + Akzeptanzkriterium: condition = 1.0 überall → deltaProzent == 0
        Edge e1 = new Edge(n1(), n2(), RailKind.NORMAL, 0.02, 500.0, 1.0, 1.0);
        Edge e2 = new Edge(n2(), n3(), RailKind.NORMAL, 0.05, 800.0, 1.0, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e1, e2), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        assertEquals(rf.sollFahrzeitSekunden(), rf.istFahrzeitSekunden(), 1e-9,
                "Bei condition = 1.0 everywhere: soll == ist");
        assertEquals(0.0, rf.deltaProzent(), 1e-6,
                "deltaProzent muss 0 sein bei idealem Zustand");
    }

    @Test
    void predict_conditionReduced_istLongerThanSoll() {
        // Akzeptanzkriterium: condition < 1.0 auf einer Kante → deltaProzent > 0
        // Modell: availablePower = maxPowerW × condition → bei condition=0.5
        // ist die Leistung halbiert → langsameres Gleichgewicht → Ist > Soll
        Edge degraded = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 1000.0, 0.5, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(degraded), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        assertTrue(rf.istFahrzeitSekunden() > rf.sollFahrzeitSekunden() + 1e-6,
                "Ist muss länger sein als Soll bei degraded condition: ist="
                + rf.istFahrzeitSekunden() + " > soll=" + rf.sollFahrzeitSekunden());
        assertTrue(rf.deltaProzent() > 0.0,
                "deltaProzent muss > 0 sein bei condition < 1.0: " + rf.deltaProzent());
        assertTrue(rf.deltaProzent() < 100.0,
                "deltaProzent unrealistisch hoch bei condition=0.5 (nicht totales outage): " + rf.deltaProzent());
    }

    @Test
    void predict_conditionMonotonie_worseConditionLargerDelta() {
        // Akzeptanzkriterium Monotonie: schlechteres Netz → größeres deltaProzent
        Edge cond70 = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 1000.0, 0.7, 1.0);
        Edge cond30 = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 1000.0, 0.3, 1.0);

        Optional<RouteForecast> r70 = Planner.predict(
                List.of(cond70), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);
        Optional<RouteForecast> r30 = Planner.predict(
                List.of(cond30), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(r70.isPresent());
        assertTrue(r30.isPresent());
        assertTrue(r30.get().deltaProzent() > r70.get().deltaProzent(),
                "condition=0.3 muss größeres delta haben als condition=0.7: " +
                r30.get().deltaProzent() + " > " + r70.get().deltaProzent());
    }

    @Test
    void predict_multiEdge_partialDegradation() {
        // Zwei Kanten: eine mit condition=1.0, eine mit condition=0.4
        Edge perfect = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 500.0, 1.0, 1.0);
        Edge degraded = new Edge(n2(), n3(), RailKind.NORMAL, 0.0, 500.0, 0.4, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(perfect, degraded), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        // Ist sollte länger sein als Soll, da eine Kante degradiert ist
        assertTrue(rf.istFahrzeitSekunden() > rf.sollFahrzeitSekunden() + 1e-6);
        assertTrue(rf.deltaProzent() > 0.0);
        // Beide Zeiten müssen > 0 sein
        assertTrue(rf.sollFahrzeitSekunden() > 0.0);
        assertTrue(rf.istFahrzeitSekunden() > 0.0);
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

    // ─── Step 5: Bottleneck-Klassifikation (T-D39, T-D43) ───────────────────

    @Test
    void predict_bottleneck_spannung() {
        // SPANNUNG: condition < 1.0 und gradient == 0
        Edge spannungEdge = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 500.0, 0.3, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(spannungEdge), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        assertEquals(1, rf.bottlenecks().size());
        Bottleneck bn = rf.bottlenecks().get(0);
        assertEquals(BottleneckArt.SPANNUNG, bn.art());
        assertEquals(spannungEdge, bn.edge());
        assertTrue(bn.beitragSekunden() > 0.0,
                "SPANNUNG mit condition=0.3 muss Beitrag > 0 haben: " + bn.beitragSekunden());
    }

    @Test
    void predict_bottleneck_steigung() {
        // STEIGUNG: gradient > 0 und condition == 1.0
        // maxPowerW auf 500 kW reduzieren, damit die Steigungswirkung messbar wird
        // (bei 100 MW hitten beide den MAX_SPEED_MPS cap)
        Edge steepEdge = new Edge(n1(), n2(), RailKind.NORMAL, 0.05, 500.0, 1.0, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(steepEdge), DEFAULT_CONSIST, grid(), 500_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        assertEquals(1, rf.bottlenecks().size(),
                "gradient=0.05 sollte 1 Bottleneck erzeugen: " + rf.bottlenecks().size());
        Bottleneck bn = rf.bottlenecks().get(0);
        assertEquals(BottleneckArt.STEIGUNG, bn.art());
        assertEquals(steepEdge, bn.edge());
        assertTrue(bn.beitragSekunden() > 0.0,
                "STEIGUNG mit gradient=0.05 muss Beitrag > 0 haben: " + bn.beitragSekunden());
    }

    @Test
    void predict_bottleneck_kombi() {
        // KOMBI: condition < 1.0 und gradient > 0
        Edge kombiEdge = new Edge(n1(), n2(), RailKind.NORMAL, 0.05, 500.0, 0.4, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(kombiEdge), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        assertEquals(1, rf.bottlenecks().size());
        Bottleneck bn = rf.bottlenecks().get(0);
        assertEquals(BottleneckArt.KOMBI, bn.art());
        assertEquals(kombiEdge, bn.edge());
        assertTrue(bn.beitragSekunden() > 0.0);
    }

    @Test
    void predict_bottleneck_twoEdges_topIsWorst() {
        // Zwei Edges: cond=0.8 (gering) und cond=0.3 (schlimmer) → Top-1 ist cond=0.3
        // Hinweis: der "marginale" Beitrag von edge 2 wird durch den niedrigeren
        // currentSpeedMps (aus edge 1) verfälscht — das ist das korrekte Modellverhalten.
        Edge minor = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 500.0, 0.8, 1.0);
        Edge major = new Edge(n2(), n3(), RailKind.NORMAL, 0.0, 500.0, 0.3, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(minor, major), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        RouteForecast rf = result.get();
        assertTrue(rf.bottlenecks().size() >= 1, "Mindestens 1 Bottleneck erwartet");
        // Top-1 ist die schlechtere Edge (cond=0.3) oder gleichwertig
        Bottleneck top = rf.bottlenecks().get(0);
        assertEquals(BottleneckArt.SPANNUNG, top.art());
        assertTrue(top.edge().effectiveCondition() <= 0.5,
                "Top-Bottleneck sollte die schlechtere oder gleichwertige Condition haben: " +
                top.edge().effectiveCondition());
    }

    @Test
    void predict_bottleneck_maxThree() {
        // Fünf Kanten mit jeweils unterschiedlichem Verschleiß → nur Top 3
        Edge e1 = new Edge(n1(), new Node(10L), RailKind.NORMAL, 0.0, 100.0, 0.5, 1.0);
        Edge e2 = new Edge(new Node(10L), new Node(11L), RailKind.NORMAL, 0.0, 100.0, 0.6, 1.0);
        Edge e3 = new Edge(new Node(11L), new Node(12L), RailKind.NORMAL, 0.0, 100.0, 0.7, 1.0);
        Edge e4 = new Edge(new Node(12L), new Node(13L), RailKind.NORMAL, 0.0, 100.0, 0.8, 1.0);
        Edge e5 = new Edge(new Node(13L), n3(), RailKind.NORMAL, 0.0, 100.0, 0.9, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e1, e2, e3, e4, e5), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        assertTrue(result.get().bottlenecks().size() <= 3,
                "Maximal 3 Bottlenecks erwartet, aber: " + result.get().bottlenecks().size());
    }

    @Test
    void predict_bottleneck_emptyWhenPerfect() {
        // Perfekte Route (condition=1.0, gradient=0) → keine Bottlenecks
        Edge e = edgePerfect(500.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(e), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        assertTrue(result.get().bottlenecks().isEmpty(),
                "Perfekte Kante (condition=1.0, gradient=0) sollte keine Bottlenecks haben");
    }

    @Test
    void predict_bottleneck_sortedDescending() {
        // Zwei Kanten mit unterschiedlichem Verschleiß → sortiert nach Beitrag absteigend
        Edge worse = new Edge(n1(), n2(), RailKind.NORMAL, 0.0, 500.0, 0.2, 1.0);
        Edge better = new Edge(n2(), n3(), RailKind.NORMAL, 0.0, 500.0, 0.5, 1.0);
        Optional<RouteForecast> result = Planner.predict(
                List.of(worse, better), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(result.isPresent());
        List<Bottleneck> bn = result.get().bottlenecks();
        assertEquals(2, bn.size());
        assertTrue(bn.get(0).beitragSekunden() >= bn.get(1).beitragSekunden(),
                "Erster Bottleneck muss größeren oder gleichen Beitrag haben: " +
                bn.get(0).beitragSekunden() + " >= " + bn.get(1).beitragSekunden());
    }

    @Test
    void predict_bottleneck_deterministic() {
        // Zwei identische Aufrufe → identische Bottleneck-Liste (T-D45)
        Edge e = new Edge(n1(), n2(), RailKind.NORMAL, 0.03, 500.0, 0.6, 1.0);
        Optional<RouteForecast> r1 = Planner.predict(List.of(e), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);
        Optional<RouteForecast> r2 = Planner.predict(List.of(e), DEFAULT_CONSIST, grid(), 100_000_000, 0.0);

        assertTrue(r1.isPresent());
        assertTrue(r2.isPresent());
        List<Bottleneck> bn1 = r1.get().bottlenecks();
        List<Bottleneck> bn2 = r2.get().bottlenecks();
        assertEquals(bn1.size(), bn2.size());
        for (int i = 0; i < bn1.size(); i++) {
            assertEquals(bn1.get(i).art(), bn2.get(i).art());
            assertEquals(bn1.get(i).edge(), bn2.get(i).edge());
            assertEquals(bn1.get(i).beitragSekunden(), bn2.get(i).beitragSekunden(), 1e-9);
        }
    }
}
