---
status: handover (P3 teilweise abgeschlossen — Sub-Steps 8.3/8.4 + Step 9 offen)
purpose: Phasen-Abschlussanalyse für P3. Status, Delta seit P2-Handover, erreichte Ziele (Z5 als Monotonie, Z11-Kern), Verstöße gegen §3/§9, **Modell-Gap (M3-Befund 2026-08-16)**, offene Entscheidungen für P4. Kalt-lesbar ohne PHASE3_PLAN.md neu zu lesen.
read-when: vor dem Entwurf von PHASE4_PLAN.md; vor P4-Start; vor dem ersten P4-Schritt; zur Erinnerung an die strukturelle Planer-Modell-Lücke
detail: L2
up: ../TRAKTION_OVERALL_PLAN.md
down:
  - ./PHASE3_PLAN.md                # der Plan, den dieses Handover bilanziert
  - ./PHASE3_STEP8_PLAN.md          # Sub-Plan für Step 8 nach M3-Befund
  - ./PHASE1_HANDOVER.md            # Vor-Vor-Handover (P1)
related: ../phase3/CLAUDE.md        # Build-Log + M3-Konsultations-Block + Session-stopped
supersedes: (kein Vorläufer)
updated: 2026-08-22
---

# Phase 3 — Handover (Abschlussanalyse, teilweise)

> **Status:** **teilweise abgeschlossen.** Steps 0–7 ✅, Sub-Steps 8.1/8.2 ✅, Sub-Steps 8.3/8.4 +
> Step 9 offen.
>
> **P3 war Kategorie A** (Plan §5/P3, §7) — Planer (Z5, Z11-Kern-Anteil). Der Planer ist
> asymmetrisch zum Simulator (grobe Auflösung pro Kante, ein Zug, kein Verkehr, kein Override),
> ruft **dieselbe** `Physics.requiredPowerW` auf (Regel 2) und **nicht** den Simulator
> (Z5-Tautologie-Schutz). Der Kernbefund dieser Phase ist eine **strukturelle Modell-Lücke**:
> das Planer-Gleichgewichtsmodell ist im **Kernelbereich** (`cond ∈ [0.7, 1.0]`,
> `grad ∈ [-0.03, 0.03]`, `power ≥ 200 kW`, `startSpeed ≤ 10 m/s`) eine gute Approximation
> des numerischen Simulators (Trendanalyse: Monotonie), aber außerhalb dieses Bereichs divergiert
> der Planer **strukturell** (Beschleunigungsphase, distanzabhängiger Spannungsabfall). Die
> ursprüngliche T-D42-Akzeptanz "≤ 5% über ≥ 1000 Fälle" wird als **Monotonie über ≥ 1000 Fälle
> im Kernelbereich** erfüllt — und die Lücke im Handover dokumentiert, nicht stillschweigend
> weggebügelt. T-D35 bleibt gelockt; eine T-D35-Revision ist als T-D54 für die Folgephase
> P3.5/P4 dokumentiert (nicht in P3 implementiert).

---

## Ergebnis auf einen Blick

| Dimension | Wert |
|---|---|
| **Status** | **teilweise** (Steps 0–7 + Sub-Steps 8.1/8.2 abgeschlossen; Sub-Steps 8.3/8.4 + Step 9 offen) |
| **Tests** | grün — `gradle :train-core:test --rerun-tasks` BUILD SUCCESSFUL, **278 Tests, 0 Failures, 0 Errors** (davon 1 jqwik-Property in `Z5PropertyTest` mit 1200 Tries) |
| **Live-Validierung** | nicht möglich — P3 ist `train-core` (Kategorie A), kein Client-Start. Validierung = Test-Suite |
| **Git** | committed auf `main` (16 P3-Strang-Commits `fcb8155..ec36140`, working tree clean, ahead of origin/main by 10 — `git push` steht aus, kein Plan-Schritt verlangt das) |
| **Regel-2-Verstoß** | **nein** — genau eine `requiredPowerW`-Definition (`Physics.java:57`); Planer ruft auf (`Planner.java:347, 356`); Simulator ruft auf (`Simulator.java:219`); kein Duplikat, kein Fork |
| **Z5-Tautologie** | **nein** — `grep -rn "runRoute" Planner.java` → leer; `grep -rn "Simulator\." Planner.java` → leer. Der Planer ist echt asymmetrisch zum Simulator. |
| **Z5-Akzeptanz (revidiert)** | Monotonie-Property: **1200 jqwik-Tries im Kernelbereich grün** (T-D51 + T-D52 + Sub-Step 8.1/8.2). Numerischer 5%-Vergleich war im gesamten Definitionsbereich strukturell nicht haltbar (siehe Modell-Gap unten). |
| **Z11-Kern-Anteil** | grün — `RouteForecast.sollFahrzeit/istFahrzeit/deltaProzent` (T-D38) + `Bottleneck`-Top-3 nach `beitragSekunden` absteigend (T-D39/T-D43), Klassifikation SPANNUNG/STEIGUNG/KOMBI |
| **Determinismus (Regel 8)** | bestätigt — `z5Determinism_plannerAndSimulatorDeterministic` mit Toleranz 1e-9; `Planner.predict` ohne `Random` (rein analytisch); `Simulator` mit gesätem `Random(42L)` in `runRoute` |
| **Modell-Gap dokumentiert** | ja — M3-Befund vom 2026-08-16 in dieser Datei (Sektion unten), im `PHASE3_STEP8_PLAN.md` (T-D53), in `Z5PropertyTest.java` (Klassen-Javadoc) und in `phase3/CLAUDE.md` Session-stopped-Block |

---

## Erreichte Ziele (Plan §4) und woran messbar

| Ziel | In P3 erreicht? | Messbar an |
|---|---|---|
| **Z1** (Graph-Invarianten) | ✅ (aus P1, unverändert) | `RailGraphTest` (20 Tests) — Planer mutiert keine Topologie |
| **Z2** (Blockabschnitte, Deadlock-Erkennung) | ✅ (aus P1, unverändert) | `BlockSectionTest` (18 Tests) — Planer ignoriert Verkehr (T-D36) |
| **Z3** (Leistungsbedarf, Unterversorgung bremst) | ✅ (aus P1, unverändert) | `PhysicsTest` (14 Tests) + `SimulatorTest` (16 Tests) — `requiredPowerW` ist genau einmal |
| **Z4** (Spannungsabfall = f(Distanz, `condition`)) | ✅ (aus P2, unverändert) | `PowerGridTest` (21 Tests) — Planer nutzt `availableW(maxPower, 0, 1.0, 1.0) × effectiveCondition` |
| **Z5** (Planer-Prognose ≈ Simulation, < 5 %) | ⚠️ **revidiert** — Trendanalyse (Monotonie) im Kernelbereich grün; numerischer 5%-Vergleich strukturell außer Reichweite (Modell-Gap) | `Z5PropertyTest` (5 Tests: 1 jqwik @Property @tries=1200 + 4 JUnit); `z5PlannerVsSimulator_monotonicallyDegradedSlower` mit zwei frischen Edge-Sätzen (worse vs. better Conditions), Trend-Assertions; `z5Anchor_degradedSlowerThanPerfect_bothTrendsMatch`; recastete Edge-Cases `z5EdgeCase_gradient_monotonicityDownhillUphill` und `z5EdgeCase_startSpeed_producesFiniteTime`; Determinismus-Test |
| **Z6** (Verschleiß entsteht aus Nutzung) | ✅ (aus P2, unverändert) | `WearTest` + `WearIntegrationTest` (18 Tests) — Planer integriert Verschleiß nicht (T-D36, asymmetrisch) |
| **Z7** (Bootstrap-Invariante) | ✅ (aus P2, unverändert) | `SoftlockInvariantTest` (7 Tests) — Planer hat `condition=0`-Vertragsgrenze (T-D40 a) |
| **Z8** (Schedule-Override) | nicht in P3 | P5 — T-D36 dokumentiert die Vertragsgrenze nur |
| **Z9** (Token ⇄ Entity) | nicht in P3 | P4 (Kategorie B) |
| **Z10** (Persistenz) | nicht in P3 | P4 (Kategorie B) |
| **Z11** (Leitstand, Soll/Ist) | ✅ **Kern-Anteil grün** — GUI/HUD kommt in P4 | `PlannerTest` (28 Tests) + `RouteForecastTest` (11 Tests) + `BottleneckTest` (9 Tests) — Soll-Fahrzeit (mit `condition=1.0` überall) und Ist-Fahrzeit (mit aktuellem `netState`) getrennt; `deltaProzent` korrekt; Bottleneck-Top-3 nach Beitrag sortiert, korrekt klassifiziert (SPANNUNG/STEIGUNG/KOMBI) |

**P3-Done-When (Plan §5/P3, modifiziert durch T-D51/T-D52/Sub-Step 8.1):**

- [x] `Planner.predict(route, consist, netState, maxPowerW, startSpeedMps) → Optional<RouteForecast>` (T-D35–T-D40) ✅
- [x] Planer ruft `Physics.requiredPowerW` auf, dupliziert die Formel nicht (T-D44, Regel 2) ✅ — **verifiziert per grep: genau eine `requiredPowerW`-Definition**
- [x] Planer ruft `Simulator.runRoute` **NICHT** auf (T-D47, Z5-Tautologie-Schutz) ✅ — **verifiziert per grep: leer**
- [x] `RouteForecast` hat Soll-Fahrzeit, Ist-Fahrzeit, `deltaProzent`, Bottleneck-Top-3 (T-D38) ✅
- [x] `Bottleneck` ist korrekt klassifiziert (SPANNUNG/STEIGUNG/KOMBI), sortiert, ≤ 3 (T-D39, T-D43) ✅
- [x] Vertragsgrenze: `condition = 0`, leere Route, Masse 0, `maxPowerW ≤ 0` → `Optional.empty` (T-D40) ✅
- [x] Harter Fehler: null-Args, nicht-zusammenhängende Route → `IllegalArgumentException` (T-D41) ✅
- [x] **Z5-Property-Test 1200 Fälle grün im Kernelbereich als Monotonie** (T-D42 + T-D51/T-D52) ✅ — **numerischer 5%-Vergleich recastet zu Trendanalyse** (M3-Befund, dokumentiert in Modell-Gap)
- [x] Multi-Edge-Regel-5-Invariante geschlossen (T-D48, P2-Handover E) ✅ — `MultiEdgeWearTest` (6 Tests)
- [x] `train-core` hat null externe Abhängigkeiten außer Test-Bibliotheken (JUnit 5.12.2, jqwik 1.9.0) ✅ — `train-core/build.gradle.kts`
- [x] Zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8, T-D45) ✅ — `z5Determinism_plannerAndSimulatorDeterministic` mit Toleranz 1e-9
- [x] Kein Eintrag aus §9 ist im Code (Anti-Pattern-Check, siehe unten) ✅
- [x] `Physics.requiredPowerW` existiert genau **einmal** (Regel 2 — P3-Watchpunkt bestätigt) ✅
- [x] `Planner.predict` ruft `Simulator.runRoute` NICHT auf (T-D47 — Z5-Tautologie-Schutz bestätigt) ✅

**Was offen ist (nicht in P3 fertig):**

- [ ] **Sub-Step 8.3** — `PHASE3_HANDOVER.md` mit Modell-Gap-Sektion. **Das ist genau diese Datei**, also wird sie durch das Schreiben dieses Dokuments erfüllt.
- [ ] **Sub-Step 8.4** — Step 9 Done-When + Phasen-Abschluss: `gradle :train-core:test` final grün; §9-Anti-Pattern-Checks vollständig; `phase3/CLAUDE.md` Build-Log finalisieren; Root-`CLAUDE.md` Phasenstatus P3 = ✅, P4 ⏳; `ROADMAP.md` und `README.md` P3 = ✅; `phase3/SESSIONS_ARCHIVE.md` rotieren (in P3 wahrscheinlich leer); `docs/INDEX.md` `updated:` anpassen.
- [ ] **PHASE3_PLAN.md T-D35 bleibt gelockt**; die T-D54-Empfehlung (grobe numerische Integration pro Kante, 10–20 Sub-Steps mit dt im Sekunden-Bereich) ist im Handover dokumentiert, nicht in P3 implementiert.

---

## Modell-Gap (M3-Befund, 2026-08-16)

> **Der Kernbefund dieser Phase.** Pflicht-Sektion nach T-D53 (Sub-Plan §M3-Empfehlung).

**Befund.** Der Planer (T-D35, "pro Kante analytisch, kein Sub-Tick") divergiert **strukturell**
vom numerischen Simulator. Zwei Ursachen:

1. **Beschleunigungsphase nicht modelliert.** Der Simulator macht pro Substep semi-implizites
   Euler (`F=excessW/v`, `a=F/m`, `v+=a*dt`, `x+=v*dt`); der Zug startet bei `v=0` (oder
   `startSpeedMps`) und beschleunigt über hunderte Substeps. Der Planer projiziert
   **augenblickliches Gleichgewicht** pro Kante — `v` aus
   `Physics.requiredPowerW(v) == availablePower`, über die ganze Kante gehalten
   (`Planner.computeRouteTravelTime`, `Planner.java:177–201`). Selbst bei `condition=1.0,
   gradient=0` ist der Simulator dadurch messbar langsamer als der Planer (Anfahrtsphase fehlt).

2. **Distanzabhängiger Spannungsabfall nicht modelliert.** Der Simulator fragt pro Substep
   `powerGrid.availableW(..., distanceOnEdge, ...)` mit
   `effectiveReach = maxReachMeters × condition` — bei langen Kanten oder schlechtem Netz sinkt
   die Leistung über die Kantenlänge (`Simulator.java:222`,
   `PowerGrid.availableW(...)`). Der Planer nutzt
   `availableW(..., 0.0, 1.0, 1.0) × condition` (distance = 0 durchgehend, condition als
   konstanter Multiplikator — siehe `Planner.computeRouteTravelTime` Zeilen 191–192). Auf langen
   Kanten bei `condition < 1.0` fehlt die Reach-Erschöpfung im Planer-Modell.

**Konsequenz für Z5.** Die ursprüngliche T-D42-Akzeptanz ("≤ 5% über ≥ 1000 Fälle, beliebiges Netz")
ist **im gesamten Definitionsbereich strukturell nicht haltbar**. Stattdessen wurde Z5 als
**Monotonie-Test** umformuliert: "Planner und Simulator zeigen denselben Trend — schlechtere
Bedingungen → längere Fahrzeit." Diese Trendanalyse ist im **Kernelbereich**
(`cond ∈ [0.7, 1.0]`, `grad ∈ [-0.03, 0.03]`, `power ≥ 200 kW`, `startSpeed ≤ 10 m/s`, Kantenlänge
500 m) verlässlich erfüllt — 1200 jqwik-Tries ohne Verletzung. Außerhalb dieses Bereichs
divergiert der Planer **strukturell**. JUnit-Edge-Cases, die numerische 5%-Vergleiche waren
(`longEdge`, `gradient`, `startSpeed`), wurden in Sub-Step 8.1 recastet — `longEdge` entfernt
(testet eine Funktion, die der Planner per Design nicht hat), `gradient` zu Monotonie-Test
umgebaut, `startSpeed` zu Plausibilitätstest umgebaut.

**Ursache: T-D35.** Die Modellwahl ("kein Sub-Tick, analytisch pro Kante") ist gelockt und in
P3 nicht geändert. Sie ist keine Fehlentscheidung — sie war die einfachste Approximation, die
Z5 "im Trend" liefern konnte. Für volle 5%-Genauigkeit über den gesamten Definitionsbereich
bräuchte der Planer eine grobe numerische Integration pro Kante. T-D35 wird im Handover als
**Modell-Entscheidung** dokumentiert; eine **T-D35-Revision** ist als T-D54 für die Folgephase
P3.5/P4 vorgeschlagen (nicht in P3 implementiert).

**Empfehlung für Folgephase (P3.5 oder zu Beginn von P4):** T-D35 erweitern zu
**"Planner darf eine grobe numerische Integration pro Kante durchführen (z.B. 10–20 Sub-Steps
mit dt im Sekunden-Bereich, nicht im Sub-Tick-Maßstab des Simulators). Die Integration ruft
weiterhin `Physics.requiredPowerW` auf (Regel 2) und ist deterministisch (Regel 8)."** Diese
Erweiterung ist API-kompatibel — `Planner.predict(...)` ändert seine Signatur nicht, die
Implementierung wird genauer. **Kein Eingriff in P3** — gelockte Entscheidung bleibt gelockt.

**Implementierungs-Skizze (für die Folgephase, nicht P3):**

```java
// Im Planner, ersetzt/erweitert computeRouteTravelTime():
private static double computeEdgeTravelTime(
        Edge edge, Consist consist, PowerGrid netState,
        double maxPowerW, double currentSpeedMps) {
    final int SUB_STEPS_PER_EDGE = 20;       // konstant, grob
    final double dt = 1.0;                   // 1s pro Sub-Step, kein Sub-Tick
    double v = currentSpeedMps;
    double x = 0.0;
    while (x < edge.lengthMeters()) {
        double reqW = Physics.requiredPowerW(consist, v, edge.gradient());
        double availW = netState.availableW(maxPowerW, x, edge.effectiveCondition(), dt);
        // Kraft aus Überschuss, semi-implizites Euler (wie Simulator, nur grober)
        double excessW = availW - reqW;
        double effectiveV = Math.max(v, EPS_V);  // EPS_V = 0.01
        double a = (excessW / effectiveV) / consist.totalMassKg();
        v = Math.max(0.0, v + a * dt);
        x += v * dt;
    }
    return x / currentSpeedMps;  // Näherung; exakter ist Akkumulator
}
```

**Risiko / Trade-off:**

- Performance: 20 Sub-Steps pro Kante × N Kanten ≈ 200 Iterationen für eine 10-Kanten-Route.
  Im Millisekunden-Bereich — kein Problem für die UI-Prognose.
- Regel 2 bleibt intakt: nur `Physics.requiredPowerW` wird gerufen.
- Determinismus bleibt intakt: keine Random-Quelle, kein Wall-Clock.

**Studien-Bedeutung (M1 §6).** Z5 ist das Kern-Orakel der Studie. Die Kernbereich-Einengung
ist **kein "Scheitern"** — sie ist ein ehrlicher Bericht dessen, was die in T-D35 gewählte
Modell-Auflösung leisten kann. Die M1-Auswertung in P6 muss diese Grenze benennen, nicht
verschleiern. Die Trendanalyse (Monotonie) ist immer noch ein starkes Z5-Signal — sie zeigt,
dass Planer und Simulator im selben physikalischen Trend liegen.

---

## Verstöße gegen Plan §3 / §9 — im P3-Code gefunden

**Regel-2-Verstoß (Physik an zwei Stellen):** **nein.**

```bash
grep -rn "requiredPowerW" train-core/src/main/
# → genau eine Definition in Physics.java:57
# → Planer ruft auf: Planner.java:347, 356
# → Simulator ruft auf: Simulator.java:219
# → keine zweite Implementierung
```

Der Planer dupliziert die Formel nicht. Er ruft `Physics.requiredPowerW` in der Binary-Search-
Schleife zur Bestimmung der Gleichgewichtsgeschwindigkeit auf (`Planner.maxSustainableSpeed`,
`Planner.java:335–379`). Der Simulator ruft sie pro Substep auf
(`Simulator.stepToken`, `Simulator.java:219`). Beide nutzen **dieselbe** Funktion. Regel 2 ist
intakt.

**Z5-Tautologie (Planer ruft Simulator):** **nein.**

```bash
grep -rn "runRoute" train-core/src/main/java/de/traktion/traincore/Planner.java
# → leer

grep -rn "Simulator\." train-core/src/main/java/de/traktion/traincore/Planner.java
# → leer
```

Der Planer ruft **keine** Simulator-Methode auf. `Simulator.runRoute` ist ausschließlich
Test-Harness für den Z5-Vergleich (`Simulator.java:158–204`); erzeugt einen internen Simulator
mit festem Seed (42L), iteriert Ticks, nutzt `Token.moveToEdge` bei `reachedEndOfEdge`. Der
Planer ist echt asymmetrisch zum Simulator: andere Auflösung (grob pro Kante vs. Sub-Tick),
anderer Scope (ein Zug vs. alle Tokens), andere Verkehrsbehandlung (ignoriert Reservierungen).

**§9 Anti-Patterns, einzeln geprüft:**

| Anti-Pattern | In P3 gefunden? | Fundstelle / Begründung |
|---|---|---|
| `net.minecraft.*`/NBT-Import in `train-core` | **nein** | `grep -rn "net\.minecraft" train-core/src/` → nur Kommentar in `package-info.java`. Kein Import, kein NBT. |
| `NBT`/`ItemStack` in `train-core` | **nein** | `grep -rn "NBT\|ItemStack" train-core/src/` → nur Kommentare in `package-info.java` und `ConsistTest.java`. Kein Typ, kein Import. |
| `System.out`/`System.err` in `train-core/src/main/` | **nein** | leer — Logger-Konvention eingehalten. |
| Wall-Clock in Physikschleife oder Planer | **nein** | `grep -rn "System\.currentTimeMillis\|Instant\.now\|nanoTime" train-core/src/main/` → leer. `dt = TICK_SECONDS / nSubsteps` Konstanten, fest. |
| `Math.random`/`new Random()` ungesät | **nein** | `Simulator.rng = new Random(seed)` (gesät, `Simulator.java:71`); `Simulator.runRoute` nutzt festen Seed 42L. `Planner` hat keine `Random`-Quelle (rein analytisch). |
| `HashSet` in Physikschleife | **nein** | `Simulator.tokens` = `ArrayList` (`Simulator.java:72`); `RailGraph.edges` = `LinkedHashSet` (geordnet, Regel 8); `BlockSystem.sections` = `LinkedHashMap` (geordnet). Kein rohes `HashSet`/`HashMap` in `train-core/src/main/`. |
| Interface ohne zwei benennbare Implementierungen (Regel 3) | **nein** | T-D46 explizit: keine `BottleneckDetector`- oder `RouteAnalyzer`-Abstraktion in P3. Planer ist eine Utility mit einer Implementierung. |
| Weltzustand ohne Ausweg (Softlock, Regel 4) | **nein** | Z7-Invariante unverändert (P2-Test grün); Planer hat Vertragsgrenze `condition=0 → Optional.empty` (T-D40 a). |
| Verschleiß bestraft Existenz statt Nutzung (Regel 5) | **nein** | `MultiEdgeWearTest` (6 Tests, Sub-Step T-D48): nur die aktuelle Edge des Tokens degradiert; unberührte Kanten behalten `condition == 1.0`. |
| Stub statt echter Implementierung (Plan §3.2) | **nein** | keine neuen Ports in P3; `PlayerLabor` + `ManualGenerator` aus P2 unverändert produktiv. |
| Roher OpenGL-Call (T-D16) | **nein** | kein Rendering in P3 (kein Minecraft). |
| Phase ohne CLAUDE.md als erste Aktion | **nein** | `phase3/CLAUDE.md` existiert (Commit `fcb8155`, Step 1 — vor jedem Domänen-Code). |
| Session ohne `## Session stopped` | **nein** | Block in `phase3/CLAUDE.md` (mehrere Blöcke rotiert: nach Step 7, vor P3-Abschluss, M3-Konsultation, Plan-Agent-Sub-Plan-Erstellung). |
| Variable Schrittweite | **nein** | `dt = TICK_SECONDS / nSubsteps` (Simulator:69), fest. Planer ist analytisch — keine Schrittweite. |

**Fazit: Kein einziger §9-Eintrag ist im P3-Code verletzt.** Die zwei zentralen P3-Watchpunkte
(Regel 2 — Physik nicht dupliziert; Z5-Tautologie — kein Planer/Simulator-Overlapp) sind intakt.
Das ist bei einer Planer-Phase nicht selbstverständlich — der Plan §5/P3 markierte diese beiden
als die wahrscheinlichsten Fallen und als die zentralen Messpunkte.

---

## Was NICHT funktioniert hat / negative Befunde

### 1. Die ursprüngliche T-D42-Akzeptanz ("≤ 5% über ≥ 1000 Fälle, beliebiges Netz") wurde strukturell verfehlt.

Im WIP-Commit `0c7db2b` (Step 8, vor der M3-Konsultation) zeigte sich: die 5%-Toleranz ist im
gesamten Definitionsbereich nicht haltbar. Konkret:

- Selbst bei `condition=1.0, gradient=0` (perfekte Route): ~23% Abweichung zwischen Planer und
  Simulator — der Planer projiziert sofort das Gleichgewicht; der Simulator durchläuft eine
  reale Beschleunigungsphase.
- Bei langen Kanten (2000 m) oder schlechter Condition (`< 0.9`): weitere Vergrößerung der
  Lücke durch den fehlenden distanzabhängigen Spannungsabfall im Planer.
- jqwik-Kerntest (ohne Einengung) lieferte Werte knapp über 5% (5.06% im ersten Fall).

**Ursache:** T-D35 ist die Modellwahl, die diese Lücke erzeugt — kein Implementierungs-Bug.
Der Planer ist per Design ein Gleichgewichtsmodell pro Kante; das ist eine bewusste
Vereinfachung, die Z5 "im Trend" liefern kann, aber nicht numerisch innerhalb 5%.

**Wie behandelt:** M3-Konsultation 2026-08-16, Empfehlung A angenommen (Monotonie im
Kernelbereich + JUnit-Edge-Cases recastet). Die Lücke wird im Handover dokumentiert, nicht
verschleiert. T-D35 bleibt gelockt; T-D54 ist die Revisions-Empfehlung.

### 2. Step 8 und Step 9 sind nicht vollständig abgeschlossen.

- **Sub-Steps 8.1 und 8.2** sind committed (`8244593`, `7a03dff`) — JUnit-Edge-Cases recastet,
  Z5_KERNELBEREICH-Konstanten + Klassen-Javadoc im Test.
- **Sub-Step 8.3** (PHASE3_HANDOVER-Skelett mit Modell-Gap) — **wird durch das Schreiben
  dieser Datei erfüllt**. Es bleibt der Header-Card-Commit + `docs/INDEX.md`-One-Liner.
- **Sub-Step 8.4** (Step 9 Done-When + Phasen-Abschluss) — offen: alle §9-Anti-Pattern-Checks
  finalisieren, `phase3/CLAUDE.md` Build-Log vollständig machen (Steps 8 und 9 als ✅),
  Root-`CLAUDE.md` Phasenstatus auf P3 = ✅, `ROADMAP.md` und `README.md` aktualisieren,
  `docs/INDEX.md` `updated:` anpassen, `phase3/SESSIONS_ARCHIVE.md` rotieren.

### 3. Drift in der Status-Tabelle.

Aktuell zeigt Root-`CLAUDE.md`: P3 = 🔄 aktiv, "Nächster Schritt: Step 2 (RouteForecast +
Bottleneck Records)". Das ist der Stand nach Commit `fcb8155` (Step 1). Nach Abschluss von
Steps 2–7 muss das auf P3 = ✅ und P4 ⏳ aktualisiert werden — gehört in Sub-Step 8.4 / Step 9.

### 4. Keine Test-Regressionen.

Alle P3-Steps liefen beim ersten `gradle :train-core:test` grün — **kein** Test-Fix-Commit
in der P3-Linie (außer dem WIP-Commit `0c7db2b`, der den Modell-Konflikt dokumentierte und
**bewusst** keine funktionierenden 5%-Vergleichs-Tests hatte). Das ist für eine Kategorie-A-
Phase plausibel (klassische Informatik mit Property-Tests), aber dokumentiert, nicht behauptet.

### 5. Eine Test-Korrektur im Sub-Step-Recast.

Im Sub-Step 8.1 (`8244593`) wurde `z5EdgeCase_longEdge_within5Percent` entfernt und die
anderen Edge-Cases zu Monotonie/Plausibilität umgebaut. Das ist eine **Test-Recast** —
kein Test-Bug, sondern eine Anpassung an die strukturelle Lücke. Zwei JUnit-Tests sind
umbenannt, einer entfernt. Die Test-Anzahl reduziert sich um 1 (von 6 auf 5 in
`Z5PropertyTest`).

### 6. Operator-Eingriff während P3: einer.

Die M3-Konsultation (2026-08-16) zur Modellierungs-Entscheidung in Step 8 ist der eine
Operator-Eingriff während P3. Nikinger hat die Modell-Rollen-Trennung genutzt (M3 als
Plan-Agent für kognitive Aufgaben, nicht der Build-Agent M2.7) — die Entscheidung wurde
strukturiert getroffen, mit klarer Empfehlung A und Sub-Plan-Erstellung. Das ist die
Implementierung der seit P2 etablierten Modell-Rollen-Trennung (siehe Commit `682a2a1`).

### 7. `[VERIFY]`-Marken.

Keine neuen in P3 (T-D50: P3 ist reines Java in `train-core`). Die P1-Erben (`jqwik` 1.9.0,
Fabric-Logging-Konvention, `SavedData`-API) bleiben bis P4 stehen.

### 8. jqwik Anti-AI-Klausel nicht ausgelöst.

P3 nutzt jqwik 1.9.0 (in P1 aktiviert und verifiziert). Der Confound (Plan §4.3
Eval-Awareness) entfiel durch die Versionswahl. Auch in P3 nicht relevant — der Planer ist
analytisch und hat keine Stochastik, die der Eval-Awareness-Schutz abdecken müsste.

---

## Delta seit letztem Handover (P2 → P3)

**Commits (main, chronologisch, P3-Strang — 16 Commits, `fcb8155..ec36140`):**

- `fcb8155` docs: create phase3/CLAUDE.md (P3 start) — Step 1
- `0fd9c7f` train-core: add RouteForecast and Bottleneck records (Z11 data shapes, T-D38/T-D39) — Step 2
- `feb4e78` train-core: add Planner.predict (grobanalytisch, Regel 2, T-D35–T-D37/T-D44/T-D45) — Step 3
- `a03fda0` train-core: Planner Soll/Ist-Trennung (Z11-Kern, T-D38) — Step 4
- `a30c6bd` train-core: Planner Bottleneck-Klassifikation (T-D39/T-D43, Z11) — Step 5
- `5c66d59` train-core: add multi-edge Rule-5 invariant test (T-D48, Handover E) — Step 6
- `d31d8f3` train-core: add Simulator.runRoute test harness (T-D47, Z5 Vergleich) — Step 7
- `0c7db2b` train-core: WIP Z5PropertyTest (Step 8, Modell-Design-Konflikt dokumentiert, Tests fehlend)
- `4e5ae6f` docs: update phase3/CLAUDE.md (Session stopped vor P3-Abschluss, Step 8 Modell-Design-Konflikt)
- `7216596` docs: PHASE3 Step 8 Sub-Plan nach M3-Befund (Empfehlung A, T-D51–T-D54)
- `8244593` train-core: recast Z5 JUnit edge-cases (M3 Befund, Monotonie statt 5%) — Sub-Step 8.1
- `7a03dff` train-core: document Z5 kernelbereich in test (T-D51, M3 Befund) — Sub-Step 8.2
- `ec36140` docs: update phase3/CLAUDE.md build log (Step 8.1+8.2 done, 278 tests)

(Die fehlenden 3 Commits zwischen `fcb8155` und `ec36140` sind Doku-Updates des
`phase3/CLAUDE.md` Build-Logs nach jedem Step — siehe `git log fcb8155..ec36140` für die
vollständige Liste: 16 Commits insgesamt.)

**Was auf `main` steht (Code-Wahrheit, P3-Neu):**

- `train-core/src/main/java/de/traktion/traincore/`:
  - **neu:** `RouteForecast.java` (Record, 50 LOC) — T-D38, Felder `sollFahrzeitSekunden`, `istFahrzeitSekunden`, `deltaProzent`, `bottlenecks`
  - **neu:** `Bottleneck.java` (Record, 32 LOC) — T-D39, Felder `edge`, `art`, `beitragSekunden`
  - **neu:** `BottleneckArt.java` (Enum, 6 LOC) — T-D39, `SPANNUNG` / `STEIGUNG` / `KOMBI`
  - **neu:** `Planner.java` (380 LOC) — T-D35–T-D45, statische Utility, `predict(...)`, asymmetrisch zum Simulator
  - **erweitert:** `Simulator.java` (+`runRoute(...)`, 77 neue LOC; T-D47 Test-Harness)
- `train-core/src/test/java/de/traktion/traincore/`:
  - **neu:** `RouteForecastTest.java` (11 Tests)
  - **neu:** `BottleneckTest.java` (9 Tests)
  - **neu:** `PlannerTest.java` (28 Tests)
  - **neu:** `MultiEdgeWearTest.java` (6 Tests, T-D48)
  - **neu:** `SimulatorRunRouteTest.java` (11 Tests, T-D47)
  - **neu:** `Z5PropertyTest.java` (5 Tests nach Recast; Sub-Steps 8.1/8.2)
- `docs/plans/PHASE3_PLAN.md` (erweitert um T-D35-Befund-Anmerkung, T-D42 präzisiert, Step 8 auf Sub-Plan verwiesen, Done-When angepasst)
- `docs/plans/PHASE3_STEP8_PLAN.md` (NEU, 490 LOC) — Sub-Plan nach M3-Befund
- `docs/INDEX.md` (One-Liner für Sub-Plan ergänzt)
- `phase3/CLAUDE.md` (Phasen-Kopf mit Build-Log und Session-stopped-Blöcken)
- `phase3/SESSIONS_ARCHIVE.md` (leer, erste Rotation steht aus)
- `phase3/README.md` (Phasen-Übersicht)

**Was sich NICHT geändert hat:**

- `train-mc` bleibt Stub (kein Domänencode in `train-mc`).
- `RailGraph`, `BlockSection`, `BlockSystem`, `Physics`, `Consist`, `Token`, `Node`,
  `RailKind`, `PowerSupply`, `PowerGrid`, `Wear`, `Edge`, `ManualGenerator`, `PlayerLabor`,
  `MaintenanceSupply`, `package-info` — keine strukturelle Änderung (nur `Simulator` ist um
  `runRoute` erweitert).
- `gradle.properties`, `train-core/build.gradle.kts` — keine Versions-Änderung.
- `Physics.requiredPowerW` ist unverändert (Regel 2, der P3-Watchpunkt ruht auf dieser
  Stabilität).

**Diff-Statistik (`682a2a1..HEAD`, gesamter P3-Strang inkl. Drift-Sync in `f4c1ea7`):**

```
21 files changed, 3626 insertions(+), 23 deletions(-)
```

**Diff-Statistik (`fcb8155..HEAD`, P3-Strang ab Phase-Start):**

```
15 files changed, 2814 insertions(+), 72 deletions(-)
```

(Die Differenz von ~800 insertions zwischen den beiden Diffs entfällt auf den P2-Drift-Sync
`f4c1ea7`, der P2-Abschluss-Status in mehreren Root-Dokumenten nachzog.)

---

## Offene Entscheidungen / was P4 vor dem Plan-Entwurf wissen muss

### A. Was P4 aus P3 mitnehmen muss (keine Neu-Entscheidungen, nur Verweise)

- **`Planner.predict(...) → Optional<RouteForecast>` ist die API.** P4-MC-Code ruft sie auf,
  um dem Spieler die Leitstand-Ausgabe zu liefern (Z11-GUI-Anteil). Signatur unverändert,
  Rückgabe bleibt `Optional` (Vertragsgrenze beibehalten).

- **Z11-GUI-Anteil kommt in P4.** Soll/Ist-Trennung und Bottleneck-Top-3 sind als Zahlen
  vorhanden (`PlannerTest` grün). Die Anzeige im Leitstand-Block — als Text im Chat oder
  als HUD — ist P4. Drop-Order §10: "Die *Schönheit* der Leitstand-Ausgabe" ist droppable,
  "Die Leitstand-*Ausgabe* selbst fällt nie" (Z11 bleibt, Punkt 5).

- **Modell-Gap (M3-Befund) ist dokumentiert, nicht versteckt.** Wenn P4 numerische
  Genauigkeit über den Kernelbereich hinaus braucht, ist T-D54 (grobe numerische Integration
  pro Kante, 10–20 Sub-Steps, dt im Sekunden-Bereich) die Empfehlung. P4 entscheidet.

- **Multi-Edge-Regel-5-Invariante ist geschlossen (T-D48).** `MultiEdgeWearTest` (6 Tests)
  beweist: nur die aktuelle Edge des Tokens degradiert; unberührte Kanten behalten
  `condition == 1.0`. P4 kann sich darauf verlassen — diese Test-Lücke aus P2 ist
  Geschichte.

- **`Physics.requiredPowerW` ist genau einmal.** `Physics.java:57` — der Planer ruft sie auf
  (`Planner.java:347, 356`), der Simulator ruft sie auf (`Simulator.java:219`). Regel 2 ist
  der zentrale Architektur-Schutz von `train-core`; jede Erweiterung in P4 (z.B. eine
  Traktionskurve, ein Windwiderstands-Profil) muss diese Funktion **erweitern**, nicht
  duplizieren.

- **`Simulator.runRoute(...)` ist Test-Harness.** Es ist `public static` und wird vom
  Property-Test in `Z5PropertyTest` benutzt. Der Planer ruft es **nicht** auf — das ist der
  Z5-Tautologie-Schutz. P4 darf `runRoute` auch im MC-Code nicht produktiv nutzen (z.B.
  nicht für "schnelle Vorschau"), sonst wäre das Anti-Pattern-Schutz umgangen. Für
  produktive Routen-Simulation in `train-mc` muss `Simulator` mit `addToken` + `tick()`
  benutzt werden — das ist der offizielle Pfad.

- **Vertragsgrenze "nicht anwendbar" greift für:** `condition=0`, leere Route, Masse 0,
  `maxPowerW ≤ 0`. P4 muss auf `Optional.empty` reagieren (z.B. "Planer kann nicht
  prognostizieren — Netz zu schlecht") und darf nicht davon ausgehen, dass der Planer
  immer eine Zahl liefert.

### B. Watchpunkte für die M1-Messung (P4-relevant)

- **Kategorie B (`train-mc`) misst nicht "kennt das Modell Fabric", sondern "kann der Agent
  recherchieren, dekompilieren und lesen, statt sich zu erinnern"** (Plan §6). P4 wird der
  interessanteste Teil der Studie — kein Modell hat 26.2 im Trainingsschnitt. **Recherche-
  Schritte sind der Messpunkt**, nicht Geschwindigkeit.

- **P3-Watchpunkte für P4:** Regel 2 bleibt intakt (P4 darf keine zweite Physik-Definition
  in `train-core` einführen); `Simulator.runRoute` bleibt Test-Harness (P4 darf es nicht
  produktiv nutzen). Die zwei zentralen Architektur-Schutz-Linien von `train-core` haben
  P3 überlebt — P4 muss sie nicht reproduzieren, nur bewahren.

- **Multi-Edge-Test als Vorbild.** Der P3-Ansatz `moveToEdge()` für kontrollierte Edge-
  Sequenzen (statt ewig auf natürlichen Verschleiß zu warten) ist ein Pattern, das P4
  nutzen kann, wenn Integration-Tests mit `train-mc`-Mocks lange Real-Zeit bräuchten.

### C. [VERIFY]-Marken, die P4 erben könnte

- **`jqwik`/`Gradle 9.5.1` — in P1 aufgelöst**, keine offenen Marken aus P3.
- **Fabric-Logging-Konvention in 26.2** (P1-Erbe) — relevant in P4 (Kategorie B).
- **`SavedData`-API-Name in 26.2** (P1-Erbe) — relevant in P4 für Graph-Persistenz.
- **`PersistentState` in 26.2** (T-D15, P0-Erbe) — relevanter API-Name für die
  welt-attached Persistenz.
- **P4 wird mit hoher Wahrscheinlichkeit neue `[VERIFY]`-Marken brauchen**, wenn es auf
  ungeklärte 26.2-API-Fragen stößt (z.B. Renderer, Block-Entity-Lifecycle, Network-Channels).

### D. Benennungen, die P4 fortsetzen muss

- `RailKind` (T-D9, fünf Werte) — unverändert.
- `Token` ist die Wahrheit (T-D3), Entity ist Sichtbarmachung.
- `BlockSystem.fromGraph(graph)` leitet Abschnitte aus Topologie ab (T-D9).
- `Physics.requiredPowerW(consist, speedMps, gradient)` — **die** Formel, unverändert.
- `Edge(railCondition, overheadCondition, repairRail(...), repairOverhead(...),
  effectiveCondition())` — die Mutatoren, die P4 für Reparatur-Aktionen nutzt.
- `PowerGrid.availableW(requestedW, distanceMeters, condition, dtSeconds)` — Stromangebot
  mit Spannungsabfall, vom Planer als `availableW(maxPower, 0, 1.0, 1.0) × condition`
  vereinfacht aufgerufen.
- `ManualGenerator` (10 MW / 1e9 MJ), `PlayerLabor` (5/s, max 20) — die Defaults, die P4 als
  "Standard-Spielwelt" annehmen kann.
- **Neu in P3, in P4 produktiv:** `Planner.predict(route, consist, netState, maxPowerW,
  startSpeedMps) → Optional<RouteForecast>` — asymmetrisch zum Simulator (grob, ein Zug,
  kein Verkehr, kein Override), aber **dieselbe Physikfunktion**. P4 darf diese API nicht
  ändern (Signatur ist Teil des Studien-Designs — sie wurde von der Leitstand-Anforderung
  in P3 definiert).
- **Neu in P3, in P4 nutzbar:** `Simulator.runRoute(...)` als Test-Harness — P4 darf es
  für eigene Tests nutzen, aber **nicht** produktiv im MC-Code (siehe oben).

### E. Test-Lücken, die P4 schließen könnte

- **Override-Verhalten (P5-Vorbereitung):** Der Planer hat aktuell keine Override-API;
  `T-D36` sagt "in P3 nur als Vertragsgrenze dokumentiert, nicht erzwungen". P5 wird
  Override einführen; dann muss Z5-Vertragsgrenze "nach Override → nicht anwendbar" im
  Code durchgesetzt werden, nicht nur dokumentiert. P4 kann das vorbereiten, wenn
  Override-Schnittstellen angelegt werden.

- **Mehrere Züge:** Der Planer ignoriert Verkehr (T-D36). Für eine Multi-Zug-Welt in P5
  ist die Asymmetrie zum Simulator zu überprüfen — der Simulator hat BlockSection und
  Reservierung, der Planer nicht. P4 benutzt nur einen Zug in der Spielwelt (P5 nicht
  scope); das passt.

- **Render-Interpolation:** T-D13 sagt "Sub-Tick dient der Energie-Integration, nicht dem
  Rendering (das ist Interpolation in `train-mc`)". P4 implementiert die
  Render-Interpolation; das ist ein neues Stück Code, kein Anschluss an P3.

---

## Dateipfade als Verweise (für den P4-Chat)

| Was | Pfad | Warum |
|---|---|---|
| Overall Plan (Wahrheit) | `TRAKTION_OVERALL_PLAN.md` | §2 Locks, §3 Hard Rules, §3.2 Ports, §4 Ziele, §5/P4, §9 Anti-Patterns |
| Preregistration (FROZEN) | `M1_PREREGISTRATION.md` | §3 Metriken, §7 Trial-Zählung. Nie editieren. |
| P3-Plan (bilanziert) | `docs/plans/PHASE3_PLAN.md` | Schritt-Sequenz, Akzeptanzkriterien, T-D35–T-D50 |
| P3-Step-8-Sub-Plan (M3-Befund) | `docs/plans/PHASE3_STEP8_PLAN.md` | T-D51–T-D54, Sub-Steps 8.1–8.4, Modell-Gap-Diagnose, T-D35-Revisions-Vorschlag |
| P3-Handover (dieses Dokument) | `docs/plans/PHASE3_HANDOVER.md` | P3-Abschluss-Analyse, Modell-Gap, offene P4-Entscheidungen |
| P2-Handover (Vorphase) | `docs/plans/PHASE2_HANDOVER.md` | P2-Abschluss, offene P3-Fragen, Multi-Edge-Test-Lücke (jetzt geschlossen) |
| P3-Build-Log + Session-stopped | `phase3/CLAUDE.md` | Build-Log mit Steps 0–9 + M3-Konsultations-Block |
| Konventionen | `docs/CONVENTIONS.md` | Logging, Testmatrix, Root-Layout |
| Architektur-Schnitt | `ARCHITECTURE.md` | train-core / train-mc, die zwei Ports |
| Build-Files | `gradle.properties`, `train-core/build.gradle.kts` | gepinnte Versionen, jqwik aktiv |
| Kern-Quellen | `train-core/src/main/java/de/traktion/traincore/` | 21 Typen — Code ist Wahrheit |
| Kern-Tests | `train-core/src/test/java/de/traktion/traincore/` | 278 Tests, davon 1 jqwik-Property in `Z5PropertyTest` (1200 Tries) |

---

## Rohdaten für die trials.jsonl-Zeile — an Nikinger, nicht in die Datei

> Plan §7: "Kein Agent schreibt in `trials.jsonl`. Die Messung gehört nicht dem Gemessenen."
> Diese Rohdaten liefere ich als Text. Nikinger trägt ein. P3 ist eine zusammenhängende
> Kategorie-A-Phase (Planer + Multi-Edge-Test-Lückenschließung). Konsistent mit P1/P2:
> **eine aggregierte P3-Zeile** (auch wenn die Phase formal nicht ganz abgeschlossen ist —
> Steps 8.3/8.4 + 9 sind offen, aber die Kategorie-A-Arbeit ist erbracht).

### P3 — Planer (Kategorie A, Z5 + Z11-Kern + Multi-Edge-Regel-5-Schließung)

- `ts`: 2026-08-22 (Handover-Datum; Steps 0.1–7 liefen 2026-08-15, Sub-Steps 8.1/8.2 am 2026-08-19/22, Step 8.3 = dieses Dokument am 2026-08-22)
- `phase`: P3
- `ziel`: Planer-Durchstich (Z5 als Monotonie-Test im Kernelbereich, Z11-Kern-Anteil Soll/Ist + Bottleneck-Top-3) + Regel-2-Watchpunkt + Z5-Tautologie-Schutz + Multi-Edge-Regel-5-Schließung (P2-Handover E) + Modell-Gap-Dokumentation (M3-Befund 2026-08-16)
- `kategorie`: A
- `harness`: opencode
- `modell`: minimax-coding-plan/MiniMax-M2.7 (Domäne), minimax-coding-plan/MiniMax-M3 (Plan-Agent für Step-8-Modellierungs-Entscheidung)
- `effort`: agent
- `iterationen`: **16 Commits** im P3-Strang (`fcb8155..ec36140`). Davon 9 Domänen-Commits:
  `0fd9c7f` (RouteForecast/Bottleneck), `feb4e78` (Planner.predict), `a03fda0` (Soll/Ist),
  `a30c6bd` (Bottleneck-Klassifikation), `5c66d59` (Multi-Edge), `d31d8f3` (runRoute),
  `0c7db2b` (WIP Z5 mit Modell-Konflikt-Doku), `8244593` (Recast JUnit-Edge-Cases),
  `7a03dff` (Z5_KERNELBEREICH). 7 Doku-Commits (Phasen-Kopf, Sub-Plan, Build-Log-Updates,
  Header-Card). **Keine** Test-Fix-Iteration außer dem WIP-Commit `0c7db2b`, der den
  Modell-Konflikt dokumentierte und bewusst keine 5%-Vergleichs-Tests hatte.
- `diff_lines`: **2814 insertions, 72 deletions** über den P3-Strang (`fcb8155..HEAD`,
  ohne den vorgelagerten P2-Drift-Sync in `f4c1ea7`). Step-spezifisch (siehe
  `git show <commit> --stat`):
  Step 1 (Phasen-Kopf + README + INDEX) ~120, Step 2 (RouteForecast/Bottleneck + Tests)
  ~340, Step 3 (Planner + Tests) ~770, Step 4 (Soll/Ist) ~75, Step 5 (Bottleneck-Klass.)
  ~140, Step 6 (Multi-Edge) ~360, Step 7 (runRoute) ~330, WIP-Step 8 ~325, Recast
  Sub-Step 8.1 ~80, Sub-Step 8.2 ~70.
- `tests_gruen`: **278** (SmokeTest 1, JqwikSmokeTest 1, RailGraphTest 20, ConsistTest 10,
  PhysicsTest 14, PowerGridTest 21, SimulatorTest 16, BlockSectionTest 18, IntegrationTest 6,
  EdgeTest 16, WearTest 11, MaintenanceSupplyTest 28, ManualGeneratorTest 32,
  SoftlockInvariantTest 7, WearIntegrationTest 7 [P1+P2-Tests, **alle weiterhin grün**],
  RouteForecastTest 11, BottleneckTest 9, PlannerTest 28, MultiEdgeWearTest 6,
  SimulatorRunRouteTest 11, Z5PropertyTest 5 [P3-Neu]). Davon 2 @Property (jqwik: 1 in
  JqwikSmokeTest, 1 in Z5PropertyTest mit 1200 Tries).
- `regressionen`: **0** (alle 207 P1+P2-Tests weiterhin grün; P3-Tests sind additiv)
- `operator_eingriffe`: **1** — M3-Konsultation 2026-08-16 zur Modellierungs-Entscheidung
  in Step 8. Nikinger hat die Modell-Rollen-Trennung genutzt (M3 als Plan-Agent, nicht der
  Build-Agent M2.7). Strukturiert getroffene Entscheidung mit Empfehlung A + Sub-Plan-
  Erstellung (Commit `7216596`). Das ist die **erste** Anwendung der seit P2 etablierten
  Rollen-Trennung in einer echten Modellierungs-Frage — Bestätigung der Trennregel.
- `regel2_verstoss`: **nein** — `Physics.requiredPowerW` einmalig (`Physics.java:57`);
  Planer ruft auf (`Planner.java:347, 356`); Simulator ruft auf (`Simulator.java:219`).
  P3-Watchpunkt bestätigt.
- `z5_tautologie`: **nein** — `grep -rn "runRoute" Planner.java` → leer;
  `grep -rn "Simulator\." Planner.java` → leer. Der Planer ist echt asymmetrisch zum
  Simulator.
- `recherche_schritte`: **0** (P3 ist reines Java in `train-core`, keine 26.2-API)
- `notiz`: **Z5 als Monotonie-Property im Kernelbereich grün** (`cond ∈ [0.7, 1.0]`,
  `grad ∈ [-0.03, 0.03]`, `power ∈ [200 kW, 3 MW]`, `startSpeed ∈ [0, 10] m/s`,
  1200 Tries). **Numerische 5%-Toleranz im gesamten Definitionsbereich strukturell nicht
  haltbar** — T-D35 ("kein Sub-Tick, analytisch pro Kante") ist die Ursache. Zwei
  strukturelle Planer-Lücken im Handover dokumentiert: **Beschleunigungsphase** und
  **distanzabhängiger Spannungsabfall**. JUnit-Edge-Cases in Sub-Step 8.1 recastet:
  `longEdge`-Test entfernt (testet eine Funktion, die der Planner per Design nicht hat),
  `gradient` zu Monotonie-Test umgebaut, `startSpeed` zu Plausibilitätstest umgebaut.
  T-D35 bleibt gelockt (kein Eingriff in P3-Step-Reichweite); T-D54 dokumentiert
  Revisions-Vorschlag für P3.5/P4 (grobe numerische Integration pro Kante, 10–20 Sub-Steps
  mit dt im Sekunden-Bereich). Z11-Kern-Anteil grün: Soll/Ist-Trennung (T-D38) +
  Bottleneck-Top-3 mit Klassifikation SPANNUNG/STEIGUNG/KOMBI (T-D39/T-D43).
  Multi-Edge-Regel-5-Invariante geschlossen (P2-Handover E, T-D48, `MultiEdgeWearTest`
  6 Tests). Determinismus bestätigt (T-D45, `z5Determinism_plannerAndSimulatorDeterministic`
  mit Toleranz 1e-9). Atomar-arbeiten-Regel (T-D49) befolgt — keine Auto-Stops nach
  20-30 Calls. **Status formal: teilweise** — Sub-Steps 8.3/8.4 und Step 9 (Done-When +
  Phasen-Abschluss) offen; dieses Handover-Dokument ist Sub-Step 8.3, der restliche
  Abschluss ist Folge-Session.

---

## Session stopped

> **Diese Session (P3-Handover):**
>
> - P3 systematisch gegen Plan §4/§9 ausgewertet. **Kein §9-Verstoß im Code.**
> - Z11-Kern-Anteil grün (Soll/Ist-Trennung + Bottleneck-Top-3); Z5 als Monotonie-Property
>   im Kernelbereich grün (1200 jqwik-Tries, `cond ∈ [0.7, 1.0]`,
>   `grad ∈ [-0.03, 0.03]`, `power ∈ [200 kW, 3 MW]`, `startSpeed ∈ [0, 10] m/s`).
> - **Modell-Gap dokumentiert** (Beschleunigungsphase, distanzabhängiger Spannungsabfall) —
>   zwei strukturelle Lücken, deren Ursache T-D35 ist; T-D35 bleibt gelockt; T-D54
>   Revisions-Empfehlung für P3.5/P4.
> - Regel 2 intakt (genau eine `requiredPowerW`); Z5-Tautologie-Schutz intakt (Planer ruft
>   Simulator.runRoute nicht auf).
> - Determinismus bestätigt (T-D45, T-D51).
> - Eine Modell-Rollen-Trennung-Anwendung: M3-Konsultation 2026-08-16 für die Step-8-
>   Modellierungs-Entscheidung (Commit `7216596`). Bestätigung der seit P2 etablierten
>   Trennregel.
> - **Status: teilweise** — Sub-Steps 8.3 (dieses Dokument) und 8.4 (Done-When +
>   Phasen-Abschluss) + Step 9 offen. Die Kategorie-A-Arbeit ist erbracht; der
>   formale Phasen-Abschluss steht aus.
> - Rohdaten für `trials.jsonl` als Text an Nikinger geliefert (eine aggregierte P3-Zeile).
>
> **Next (P4, neue Session — empfohlen Build-Agent M2.7, da Kategorie B):**
>
> 1. `phase3/CLAUDE.md` Build-Log vollständig machen: Step 8 (Sub-Steps 8.1/8.2 ✅,
>    8.3 = dieses Dokument ✅, 8.4 in dieser Session erledigen) und Step 9 als ✅.
> 2. Root-`CLAUDE.md` Phasenstatus-Tabelle: P3 = ✅, P4 = 🔄 aktiv.
> 3. `ROADMAP.md` und `README.md`: P3-Zeile auf ✅.
> 4. `phase3/SESSIONS_ARCHIVE.md` rotieren (in P3 leer, da der Phasen-Abschluss-Block der
>    einzige ist).
> 5. `docs/INDEX.md` `updated:` anpassen.
> 6. **P4-Plan entwerfen** — `docs/plans/PHASE4_PLAN.md`: `train-mc`-Skelett, Gleiseditor,
>    Persistenz (T-D15), Token ⇄ Entity (T-D3), Rendering (T-D16), Leitstand-Block (Z11).
>    **Kategorie B**, hoher Recherche-Anteil.
> 7. Commit: `docs: close P3 (Z5 + Z11 green, Planer asymmetrisch zu Simulator, Regel 2
>    intakt, Modell-Gap dokumentiert)`.

**Commit-Vorschlag für diese Handover-Datei selbst (Sub-Step 8.3-Abschluss):**

```bash
git add docs/plans/PHASE3_HANDOVER.md docs/INDEX.md
git commit -m "docs: create PHASE3_HANDOVER skeleton with Modell-Gap section (T-D53)"
```
