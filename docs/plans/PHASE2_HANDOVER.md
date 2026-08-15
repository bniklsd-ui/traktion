---
status: handover (P2 abgeschlossen → P3)
purpose: Phasen-Abschlussanalyse für P2. Status, Delta seit P1-Handover, erreichte Ziele, Verstöße gegen §3/§9, offene Entscheidungen für P3. Kalt-lesbar ohne PHASE2_PLAN.md oder Code neu lesen zu müssen.
read-when: vor dem Entwurf von PHASE3_PLAN.md; vor P3-Start; vor dem ersten P3-Schritt
detail: L2
up: ../TRAKTION_OVERALL_PLAN.md
down:
  - ./PHASE2_PLAN.md   # der Plan, den dieses Handover bilanziert
  - ./PHASE1_HANDOVER.md   # vorheriges Handover (P1)
related: ../phase2/CLAUDE.md   # Build-Log + letzter Session-stopped-Block (P2 abgeschlossen)
updated: 2026-08-15
---

# Phase 2 — Handover (Abschlussanalyse)

> **Status:** abgeschlossen.
> **P2 war Kategorie A** (Plan §5/P2, §7) — klassische Informatik: numerische Bilanzen,
> Akkumulatoren, Property-Tests. Kein Minecraft, kein Planer. Der Verschleiß-Durchstich
> (Plan §5/P2) ist erbracht: `condition ∈ [0,1]` auf Kante und Oberleitung, `condition` →
> Spannungsabfall, beide Ports produktiv, Bootstrap-Invariante bewiesen, 10.000-Ticks-
> Langlauf degradiert messbar und blockiert nie total.

---

## Ergebnis auf einen Blick

| Dimension | Wert |
|---|---|
| **Status** | abgeschlossen |
| **Tests** | grün (`gradle :train-core:test` PASSED, **208 Tests, 0 failures**, davon 4 jqwik-Properties) |
| **Live-Validierung** | nicht möglich — P2 ist `train-core` (Kategorie A), kein Client-Start. Validierung = Test-Suite. |
| **Git** | committed auf `main` (Arbeitsbaum clean, ahead of origin/main by 17 Commits — `git push` steht aus, kein Plan-Schritt verlangt das) |
| **Regel-2-Verstoß** | **nein** — genau eine `requiredPowerW`-Definition (`Physics.java:57`); Simulator ruft sie auf (`Simulator.java:142`), kein zweiter Formelkörper; `Wear.accumulate` ist eine **andere** Formel (kein Duplikat) |
| **Z5-Tautologie** | **nein** (nicht anwendbar) — kein Planer in P2. P3 ist die Watch-Phase. |
| **Determinismus (Regel 8)** | bestätigt (T-D24, T-D33) — `WearIntegrationTest.determinism_sameSeedSameEndState` vergleicht `railCondition`, `overheadCondition`, `speedMps`, `progressMeters` mit Toleranz 1e-9; `WearTest.accumulate_deterministicWithoutRng` ohne Zufall |
| **Z4-Abschluss** | ✅ — `condition` ergänzt, `PowerGrid.availableW(...condition)` mit `effectiveReach = maxReachMeters * condition` (T-D27); Simulator leitet `min(rail, overhead)` durch (`Simulator.java:145`) |

---

## Erreichte Ziele (Plan §4) und woran messbar

| Ziel | In P2 erreicht? | Messbar an |
|---|---|---|
| **Z1** (Graph-Invarianten) | ✅ (aus P1, unverändert) | `RailGraphTest` (20 Tests) — `Edge` wurde um `condition`-Felder erweitert, ohne die Identitäts-Invarianten (from/to/railKind/gradient/lengthMeters) zu berühren |
| **Z2** (Blockabschnitte, Deadlock-Erkennung) | ✅ (aus P1, unverändert) | `BlockSectionTest` (18 Tests) — P2 hat keine Topologie geändert |
| **Z3** (Leistungsbedarf, Unterversorgung bremst) | ✅ (aus P1, unverändert) | `PhysicsTest` (14 Tests) + `SimulatorTest` (16 Tests) — `requiredPowerW` ist genau einmal, Simulator ruft auf |
| **Z4** (Spannungsabfall = f(Distanz, `condition`)) | ✅ **abgeschlossen in P2** | `PowerGridTest` (21 Tests) — neue Tests: `availableW_conditionZero_zero`, `availableW_conditionHalf_lessThanConditionOne`, `availableW_conditionMonotonIncreasing`, `availableW_conditionAtDistanceZero_fullPower`, `availableW_conditionOutOfRange_throws`; P1-Rückwärtskompatibilität (`condition = 1.0`) getestet via `availableW_conditionOne_reproducesP1Behavior` |
| **Z5** (Planer-Prognose) | nicht in P2 | P3 — die interessante Watch-Phase |
| **Z6** (Verschleiß entsteht aus Nutzung, degradiert kontinuierlich, blockiert nie total) | ✅ | `WearTest` (11 Tests) + `WearIntegrationTest` (7 Tests) — Akzeptanz: `tenThousandTicks_degradationMeasurable_speedStillPositive` (rail < 1.0 nach 10k Ticks, speed > 0), `tenThousandTicks_neverBlocked` (speed > 0 an jedem 1k-Checkpoint), `degradedNetwork_slowerThanFresh` (Wirkungs-Kette komplett: Verschleiß → condition ↓ → availW ↓ → speed ↓) |
| **Z7** (Bootstrap-Invariante: aus jedem Zustand ist Handarbeit ein Ausweg) | ✅ | `SoftlockInvariantTest` (7 Tests = 3 jqwik Properties + 4 JUnit) — `repairLoopTerminates`, `slowLaborStillTerminates`, `repairNeverWorsensCondition`, `worstCaseFromNearZeroTerminates`, `sumWearMonotonicallyDecreasesUnderPureRepair`, `repairWithoutTimeAccumulationDoesNothing`, `largeNetworkFromNearZeroTerminates` |
| **Z8–Z11** | nicht in P2 | P4/P5/P6. |

**Done-When (Plan §5/P2) — alle erfüllt:**

- [x] `condition ∈ [0,1]` auf Kante und Oberleitung (`Edge.java:37–38`, `Edge.java:87–92`)
- [x] `wear += f(masse, v)` pro Substep (`Wear.accumulate` auf `Simulator.java:170`)
- [x] `condition` → Widerstand → Spannungsabfall (`PowerGrid.availableW` mit `effectiveReach = maxReachMeters * condition`, `PowerGrid.java:92–96`); Z4 abgeschlossen
- [x] `PowerSupply` / `ManualGenerator` (T-D30, `ManualGenerator.java`) — Regel 3 vollständig für Port 1 (`FixedSupply` im Test + `ManualGenerator`)
- [x] `MaintenanceSupply` / `PlayerLabor` (T-D28/T-D29, `PlayerLabor.java`) — Regel 3 vollständig für Port 2 (`PlayerLabor` heute + `DepotStock` benannt im Javadoc)
- [x] **Z7-Invariantentest** grün (Property-based mit jqwik 1.9.0)
- [x] **Z6-Langlauf-Sim 10.000 Ticks** grün (`k=1e-10` ist messbar; Speed > 0 an jedem Checkpoint)
- [x] `train-core` hat null externe Abhängigkeiten außer Test-Bibliotheken (JUnit 5.12.2, jqwik 1.9.0)
- [x] Zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8, T-D24, T-D33)
- [x] Kein Eintrag aus §9 ist im Code (Anti-Pattern-Check, siehe unten)
- [x] `Physics.requiredPowerW` existiert genau einmal (Regel 2 — P3-Watchpunkt intakt)

---

## Verstöße gegen Plan §3 / §9 — im P2-Code gefunden

**Regel-2-Verstoß (Physik an zwei Stellen):** **nein.**
`grep -rn "requiredPowerW" train-core/src/main/` findet genau eine Definition
(`Physics.java:57`). Der Simulator ruft `Physics.requiredPowerW(consist, v, gradient)` auf
(`Simulator.java:142`) und implementiert die Formel nicht selbst. `Wear.accumulate` ist
eine **eigenständige** Formel (`Δcondition = -k * mass * speed * dt`), keine Duplikation —
Regel 2 schützt die **Fahrphysik**, nicht elektrisches Netz-Verhalten. Der P3-Watchpunkt
bleibt intakt: jede künftige Planer-Implementierung muss `Physics.requiredPowerW` aufrufen,
nicht duplizieren.

**Z5-Tautologie (Planer ruft Simulator):** **nein (nicht anwendbar).**
In P2 existiert kein Planer. P3 ist die Watch-Phase. P2 hat nur die Voraussetzung verankert
(genau eine Physikfunktion, siehe oben).

**§9 Anti-Patterns, einzeln geprüft:**

| Anti-Pattern | In P2 gefunden? | Fundstelle / Begründung |
|---|---|---|
| `net.minecraft.*`/NBT-Import in `train-core` | **nein** | `grep -rn "net\.minecraft" train-core/src/` findet nur den Kommentar in `package-info.java` ("Kein net.minecraft.*, kein NBT"). Kein Import, kein NBT. |
| `NBT`/`ItemStack` in `train-core` | **nein** | `grep -rn "NBT\|ItemStack" train-core/src/` findet nur den `package-info.java`-Kommentar und einen Javadoc-Hinweis in `ConsistTest.java:12`. Kein Import, kein Typ. |
| `System.out`/`System.err` in `train-core/src/main/` | **nein** | leer — `WearIntegrationTest.java:86–87` hat ein `System.out.println` als Test-Diagnose, das ist `test/`, nicht `main/`. |
| Wall-Clock in Physikschleife | **nein** | `grep -rn "System\.currentTimeMillis\|Instant\.now\|nanoTime" train-core/src/main/` leer. `Simulator`: `dt = TICK_SECONDS / nSubsteps` Konstanten, fest. |
| `Math.random`/`new Random()` ungesät | **nein** | `Simulator.rng = new Random(seed)` (gesät, `Simulator.java:71`). `Wear.accumulate` nimmt den gesäten `rng` als Parameter (für eventuelle stochastische Komponente — wird in P2 noch nicht genutzt, ist API-Vorbereitung für P3/P5). |
| `HashSet` in Physikschleife | **nein** | `Simulator.tokens` = `ArrayList` (`Simulator.java:72`); `RailGraph.edges` = `LinkedHashSet` (geordnet, Regel 8); `BlockSystem.sections` = `LinkedHashMap` (geordnet). Kein rohes `HashSet`/`HashMap` in `train-core/src/main/`. |
| Interface ohne zwei benennbare Implementierungen (Regel 3) | **nein** | `PowerSupply` (Port 1): `FixedSupply` (Test) + `ManualGenerator` (`PowerSupply.java:6–14` benennt beide). `MaintenanceSupply` (Port 2): `PlayerLabor` + `DepotStock` benannt (`MaintenanceSupply.java:6–11`). T-D28, T-D30 erfüllt. |
| Weltzustand ohne Ausweg (Softlock, Regel 4) | **nein** | Z7-Invariante bewiesen — `SoftlockInvariantTest` mit Property-Tests (`repairLoopTerminates`, `slowLaborStillTerminates`) und JUnit-Ankern (`worstCaseFromNearZeroTerminates`, `largeNetworkFromNearZeroTerminates`). Aus jedem `condition ∈ [0, 1)` auf allen Kanten führt ein Reparatur-Loop mit `PlayerLabor` zu `condition > 0`. |
| Verschleiß bestraft Existenz statt Nutzung (Regel 5) | **nein** | `Wear.accumulate` returnt früh bei `massKg == 0.0 \|\| speedMps == 0.0` (`Wear.java:62–65`). `WearTest.accumulate_zeroMass_noEffect` + `accumulate_zeroSpeed_noEffect` testen das. Eine Kante, die kein Token befährt, bekommt keinen Verschleiß (weil `Wear.accumulate` nur auf der aktuellen Edge des Tokens aufgerufen wird, `Simulator.java:170`). |
| Stub statt echter Implementierung (Plan §3.2) | **nein** | `PlayerLabor` ist Zeit-Akkumulator (`tick(dt)` füllt auf, `withdraw(n)` zieht; rate=5/s, maxWork=20 Default; ohne `tick` bleibt `workAvailable = 0` — bewiesen durch `SoftlockInvariantTest.repairWithoutTimeAccumulationDoesNothing`). `ManualGenerator` hat endlichen Brennstoff-Vorrat (`fuelMj` in MJ, 1000 MJ Default ≈ 278 kWh), `refuel()` als P4-Hook, liefert 0 bei leerem Tank. |
| Roher OpenGL-Call (T-D16) | **nein** | kein Rendering in P2 (kein Minecraft). |
| Phase ohne CLAUDE.md als erste Aktion | **nein** | `phase2/CLAUDE.md` existiert (Commit `1f230c2`, Step 1 — vor jedem Domänen-Code). |
| Session ohne `## Session stopped` | **nein** | Block in `phase2/CLAUDE.md` (P2-Abschluss-Block, Commits `7318713`, `17ade42`/=`f4760e2`). |

**Fazit: Kein einziger §9-Eintrag ist im P2-Code verletzt.** Die zwei zentralen Watchpunkte
(Regel 2 — Physik nicht dupliziert; Z5 — kein Planer/Simulator-Overlapp) sind intakt. Das ist
bei einer Kategorie-A-Phase nicht selbstverständlich — der Plan hatte Regel 2 als Messpunkt
markiert (Plan §5/P2 Watchpunkte).

---

## Was NICHT funktioniert hat / negative Befunde

- **Keine Test-Regressionen.** Alle 16 P2-Commits (Step 0.1–0.2, Step 1–9) liefen beim ersten
  `gradle :train-core:test` grün; **kein Fix-Commit** in der P2-Linie. Das ist für Kategorie A
  plausibel (klassische Informatik, gut im Trainingschnitt), aber dokumentiert, nicht behauptet.

- **Test-Lücke bei Regel 5 (Multi-Edge-Invariante).** Der Plan §5/P2 Step 3 nannte als
  Akzeptanzkriterium explizit: "Simulator mit Token, der **nicht** auf der Edge ist ... die
  ungenutzte Kante hat `condition == 1.0`". Im P2-Code gibt es **keinen** Integration-Test
  mit zwei Kanten, der zeigt, dass eine Kante, die der Token nicht befährt, ihre Condition
  behält. Die konzeptionelle Erfüllung läuft über `WearTest.accumulate_zeroSpeed_noEffect`
  (`Wear.accumulate` macht nichts ohne Token-Fahrt), aber das ist **nicht** dasselbe wie der
  Plan-Test. **Wichtig:** kein Verstoß gegen Regel 5 — der Token pro Substep berührt nur
  seine aktuelle Edge (`Simulator.java:170`); jede andere Edge bleibt unangetastet. Die
  Lücke ist eine Testabdeckungs-Lücke, kein Verhaltens-Verstoß. **Empfehlung:** wenn der
  Token in P3 (oder spätestens P4) tatsächlich Kanten wechselt (`Token.moveToEdge`,
  `Token.java:73–79`, ist schon da — aber noch nicht im Simulator aufgerufen), sollte ein
  Multi-Edge-Integration-Test analog zu `degradedNetwork_slowerThanFresh` geschrieben werden.

- **Operator-Eingriff während P2: einer.** In Step 4 (Commit `d37c0af`) fand der Build-Agent
  einen selbstverschuldeten Anti-Pattern im Test (eine `Edge`-Instanz wurde zwischen zwei
  Simulator-Läufen geteilt, was die `condition`-Mutatoren zum Vorschein brachte) und korrigierte
  ihn. Das ist ein Test-Bug, kein Regel-Verstoß — aber es zeigt, dass das Refactoring von
  P1-Record zu P2-mutable-Class (`Edge`) eine Quelle für subtile Test-Fehler ist. P3 muss
  beim ersten Berühren der `Edge`-Mutation (z.B. wenn der Planer `repair*` aufruft) denselben
  Test-Anti-Pattern kennen.

- **`[VERIFY]`-Marken:** keine neuen in P2 — der Plan §5/P2 T-D34 hatte festgeschrieben, dass
  P2 keine neuen braucht (reines Java in `train-core`). Die P1-Erben (`jqwik` 1.9.0 — in P1
  Step 1 aufgelöst; Fabric-Logging-Konvention in 26.2, `SavedData`-API — beide P4) bleiben
  bis P4 stehen.

- **jqwik Anti-AI-Klausel nicht ausgelöst.** P2 nutzt jqwik 1.9.0 (in P1 aktiviert und
  verifiziert). Der Confound (Plan §4.3 Eval-Awareness) entfiel durch die Versionswahl —
  ob der Agent injection-resistent gewesen wäre, ist in P2 **nicht** getestet. Das ist
  eine Messlücke, kein Negativbefund.

- **Z4 nicht überschritten.** Die Wirkungs-Kette (`Wear` senkt `condition` → `PowerGrid`
  reduziert `availableW` → `Simulator` sieht weniger Überschuss → Zug wird langsamer) ist in
  P2 vollständig geschlossen (`WearIntegrationTest.degradedNetwork_slowerThanFresh`). Was P2
  **nicht** getestet hat: das Verhalten an der unteren Grenze (`condition → 0`). Bei
  `condition = 0` liefert `PowerGrid.availableW` 0 (Grenzfall, getestet in
  `PowerGridTest.availableW_conditionZero_zero`) — was passiert dann mit dem Zug? Der
  Simulator bekommt `availW = 0`, also `excessW = -reqW < 0`, und bremst. Bei `reqW > 0`
  und `dt > 0` wird der Zug auf Dauer auf 0 m/s kommen und stehen. **Das ist kein Softlock**
  (Regel 4 hält: der Spieler repariert), aber es ist eine praktische Grenze: ein Zug auf
  einem Netz mit `condition = 0` kommt nicht von alleine wieder weg. Die Brücke "Spieler
  erkennt den Stillstand und repariert" ist P4. **Empfehlung an P3:** wenn der Planer eine
  Prognose für ein Netz mit `condition = 0` liefert, muss die Vertragsgrenze "nicht
  anwendbar" greifen (analog zu Override / fremder Verkehr — §4 Z5).

- **`phase2/README.md`-Drift:** die Datei sagt im Status-Block noch "P2 ist gestartet.
  Steps 0.1, 0.2, 0b erledigt. Step 1 läuft." Das ist der Stand nach Commit `1f230c2`
  (Step 1), nicht nach Step 9. Wird in diesem Handover-Commit nachgezogen (siehe
  Commit-Notiz).

- **`phase2/CLAUDE.md` Open-questions-Drift:** der Block am Ende enthält eine Duplikation
  ("Keine. ... .s / **Keine** — ...") und einen Kategorie-Tippfehler ("Cateogry A"). Wird
  in diesem Handover-Commit bereinigt. Außerdem steht in der **P2 Steps**-Liste Step 9 noch
  als `[ ]`, während die Build-Log-Tabelle ihn als ✅ zeigt — wird auf `[x]` gezogen.

---

## Delta seit letztem Handover (P1 → P2)

**Commits (main, chronologisch, P2-Strang — 16 Commits):**

- `718b6db` m1: backfill P1 trial (operator-confirmed entry) — Step 0.2
- `1f230c2` docs: create phase2/CLAUDE.md (P2 start) — Step 1
- `beb79c3` train-core: extend Edge with rail/overhead condition (Z6 prep, T-D25/T-D26) — Step 2
- `ae5a474` train-core: add Wear and integrate into Simulator (Z6, T-D31, Rule 5) — Step 3
- `d47ad34` docs: update phase2/CLAUDE.md (Steps 2+3 done, Build-Log aktualisiert)
- `d37c0af` train-core: PowerGrid uses condition for voltage drop (Z4 complete, T-D5) — Step 4
- `efe9f90` docs: update phase2/CLAUDE.md (Steps 2-4 done, Build-Log aktualisiert)
- `fcddcdc` train-core: add MaintenanceSupply port + PlayerLabor (T-D28/T-D29, Rule 3) — Step 5
- `5eadec5` docs: update phase2/CLAUDE.md after Step 5 (P2 progress, 163 tests green)
- `872bfe9` train-core: add ManualGenerator (T-D30, Port 1 second production implementation) — Step 6
- `009cc7c` docs: update phase2/CLAUDE.md after Step 6 (P2 progress, 195 tests green)
- `19183a4` train-core: add Z7 bootstrap-invariant property test (T-D32, Rule 4) — Step 7
- `1330eae` docs: update phase2/CLAUDE.md after Step 7 (P2 progress, 202 tests green)
- `8437eab` train-core: add Z6 long-run simulation (10k ticks, T-D33) — Step 8
- `7318713` docs: update phase2/CLAUDE.md after Step 8 (P2 progress, 209 tests green, Z6+Z7 done)
- `17ade42`/`f4760e2` docs: close P2 (Z6 + Z7 green, condition model, ports complete) — Step 9

(Die kurzen Hashes `17ade42` und `f4760e2` sind derselbe Commit — `git rev-parse --short`
liefert für den Step-9-Commit beide; im Build-Log in `phase2/CLAUDE.md` ist er als
`17ade42` referenziert.)

**Was auf `main` steht (Code-Wahrheit, P2-Neu):**
- `train-core/src/main/java/de/traktion/traincore/`:
  - **neu:** `Wear.java` (Verschleiß-Akkumulator, k=1e-10, 94 LOC)
  - **neu:** `MaintenanceSupply.java` (Interface, Port 2, 39 LOC)
  - **neu:** `PlayerLabor.java` (Zeit-Akkumulator, 113 LOC)
  - **neu:** `ManualGenerator.java` (PowerSupply-Impl mit Brennstoff-Vorrat, 127 LOC)
  - **erweitert:** `Edge.java` (Record→final class, +`railCondition`/`overheadCondition`, +`repairRail`/`repairOverhead`, +`effectiveCondition`, 123 LOC)
  - **erweitert:** `PowerGrid.java` (`availableW(...condition)` Signatur, T-D27, 111 LOC)
  - **erweitert:** `Simulator.java` (Verschleiß-Aufruf pro Substep, 172 LOC)
- `train-core/src/test/java/de/traktion/traincore/`:
  - **neu:** `EdgeTest.java` (16 Tests)
  - **neu:** `WearTest.java` (11 Tests)
  - **neu:** `MaintenanceSupplyTest.java` (28 Tests)
  - **neu:** `ManualGeneratorTest.java` (32 Tests)
  - **neu:** `SoftlockInvariantTest.java` (7 Tests: 3 jqwik Properties + 4 JUnit)
  - **neu:** `WearIntegrationTest.java` (7 Tests: 10k-Ticks-Langlauf)
  - **erweitert:** `PowerGridTest.java` (15 → 21 Tests, +6 für condition-Wirkung)
  - **erweitert:** `IntegrationTest.java` (condition=1.0 für P1-Rückwärtskompatibilität)
  - **erweitert:** `SimulatorTest.java` (neues Verhalten: Simulator leitet `min(rail, overhead)` durch)

**Was sich NICHT geändert hat:**
- `train-mc` bleibt Stub (kein Domänencode in `train-mc`).
- `RailGraph`, `BlockSection`, `BlockSystem`, `Physics`, `Consist`, `Token`, `Node`,
  `RailKind`, `PowerSupply`, `package-info` — keine strukturelle Änderung (nur Imports,
  wo `Edge` jetzt `repairRail`/`repairOverhead` exponiert).
- `gradle.properties`, `train-core/build.gradle.kts` — keine Versions-Änderung.

---

## Offene Entscheidungen / was P3 vor dem Plan-Entwurf wissen muss

### A. Was P3 aus P2 mitnehmen muss (keine Neu-Entscheidungen, nur Verweise)

- **`Physics.requiredPowerW` ist genau einmal.** `Physics.java:57` — die Formel, die der
  Simulator aufruft (`Simulator.java:142`). **P3-Watchpunkt (T-D14):** der Planer muss diese
  **dieselbe** Funktion aufrufen, in **anderer Auflösung** (grob vs. Sub-Tick), mit
  **anderem Scope** (ein Zug, kein Verkehr, kein Override). Eine zweite Implementierung ist
  ein **Regel-2-Verstoß**; der Planer darf den Simulator nicht aufrufen (sonst ist Z5
  tautologisch — `f(x) == f(x)`).

- **`Wear` ist deterministisch, `rng` wird durchgereicht aber nicht genutzt.** Der Simulator
  hat einen gesäten `Random` (`Simulator.java:71`), den er pro Substep an `Wear.accumulate`
  übergibt (`Simulator.java:170`). In P2 hat `Wear` keine stochastische Komponente — der
  `rng` ist API-Vorbereitung für P3/P5. **P3-Hinweis:** wenn der Planer Zufall braucht
  (z.B. Last-Schwankungen), kann er denselben Mechanismus nutzen — `Random(seed)` mit
  dem `seed` aus dem `Simulator`-Constructor, oder einem eigenen. Determinismus (Regel 8)
  gilt für beide.

- **`condition → 0` ist eine weiche Grenze, kein Hard-Lock.** Bei `condition = 0` liefert
  `PowerGrid.availableW` 0 (Grenzfall getestet: `PowerGridTest.availableW_conditionZero_zero`).
  Der Zug kommt zum Stillstand (kein Stromangebot). **Das ist kein Softlock (Regel 4) —
  der Spieler repariert.** Der Planer muss diese Situation als "nicht anwendbar" oder
  "Stillstand in Sekunden" prognostizieren — Vertragsgrenze analog zu §4 Z5.

- **Reparatur-Modell für den Planer.** `Edge.repairRail(amount)` / `Edge.repairOverhead(amount)`
  sind direkte Mutatoren (T-D26, Regel 6). `PlayerLabor.withdraw(n)` liefert bis zu `n`
  Instandhaltungspunkte; `tick(dt)` füllt den Vorrat (rate=5/s, maxWork=20). Der Planer kann
  "Reparaturzeit" prognostizieren, indem er `tick(dt)` + `withdraw(n)` simuliert — das ist
  die einzige sinnvolle Brücke zwischen `MaintenanceSupply` und `Edge`. P3 muss klären, ob
  der Planer **eigene** Reparatur-Strategien vorschlägt (z.B. "repariere zuerst die Kante
  mit dem niedrigsten `effectiveCondition`" — eine plausible Heuristik) oder ob er nur die
  Auswirkungen einer gegebenen Reparatur simuliert.

- **`Token.moveToEdge(Edge)` ist da, aber ungenutzt.** `Token.java:73–79` erlaubt das
  Wechseln der Kante, der Simulator ruft es nicht. Der Token fährt aktuell über das
  Ende seiner Start-Kante hinaus (`progressMeters` wächst ungebremst). **P3 kann diese
  Lücke füllen** (z.B. wenn der Planer einen Routen-Vorschlag macht und die Topologie
  braucht), oder es P4 überlassen (BlockSection, Routen-Resolver).

- **`Simulator(powerGrid, nSubsteps, seed)` — die Signatur bleibt.** P2 hat sie nicht
  gebrochen. `nSubsteps = 4` (Default) ist der T-D13-Wert; der Planer wird seine eigene
  Auflösung wählen (`Planner.predict(route, consist, netState)`) und **nicht** den
  `Simulator` aufrufen.

### B. Watchpunkte für die M1-Messung (P3-relevant)

- **Regel-2-Verstoß (ja/nein):** P3 ist die **eigentliche** Watch-Phase. Wahrscheinliche
  Fallen: (a) der Planer hat eine eigene `requiredPowerW`-Variante ("für die schnelle
  Approximation"); (b) der Planer ruft `Simulator.run(n)` für die Prognose auf — dann ist
  Z5 tautologisch. **Beide Fälle protokollieren, nicht stillschweigend korrigieren** —
  das ist der Messpunkt.

- **Z5-Tautologie (ja/nein):** wie oben. **Wenn** der Planer den Simulator ruft, ist Z5
  trivial `f(x) == f(x)` und beweist nichts. Das Anti-Pattern steht explizit in §9 und
  im Watch-Abschnitt von §5/P3: "Ein Agent wird versuchen, entweder die Formel zu
  duplizieren (Regel-2-Verstoß) **oder** den Planer den Simulator aufrufen zu lassen
  (tautologisches Z5). Beides **protokollieren**, nicht stillschweigend wegkorrigieren."

- **Determinismus (Regel 8):** der Planer muss denselben Seed-Pfad haben. Z5 ist
  property-based (`|planner.predict(r) − simulator.run(r)| / simulator.run(r) < 0.05`),
  ohne Determinismus ist der Test flaky.

- **Regel 3 (Interface ohne zwei Implementierungen):** in P3 nicht unmittelbar relevant
  (kein neuer Port geplant). Wenn der Planer eine `RouteForecast`- oder `BottleneckDetector`-
  Abstraktion einführt: **zwei heute benennbare Implementierungen** oder gar keine.

- **Z6/Z7-Reibung mit dem Planer:** wenn der Planer `condition` als Eingabe nimmt und
  `PlayerLabor`-Reparatur modelliert, braucht er `WorkedWork`-Einheiten (Instandhaltungs-
  punkte). Die Brücke ist `tick(dt) + withdraw(n)`. P3 muss klären: rechnet der Planer
  mit dieser Brücke, oder ist "Reparaturzeit" out of scope für die Prognose (Z5 zielt auf
  Fahrzeit, nicht Reparaturzeit — §4 Z5).

### C. [VERIFY]-Marken, die P3 erben könnte

- **keine** aus P2 (P2 ist reines Java, T-D34). Die P1-Erben (`Fabric-Logging-Konvention
  in 26.2`, `SavedData`-API-Name — beide P4) bleiben bis P4 stehen. P3 könnte eine eigene
  `[VERIFY]`-Marke brauchen, wenn es beim Entwurf auf eine ungeklärte API-Frage stößt
  (unwahrscheinlich bei reinem `train-core`-Code).

### D. Benennungen, die P3 fortsetzen muss

- `RailKind` (T-D9, fünf Werte) — unverändert.
- `Token` ist die Wahrheit (T-D3), Entity ist Sichtbarmachung (P4).
- `BlockSystem.fromGraph(graph)` leitet Abschnitte aus Topologie ab (T-D9).
- `Physics.requiredPowerW(consist, speedMps, gradient)` — **die** Formel, die der Planer
  ebenfalls aufrufen muss.
- `Simulator(powerGrid, nSubsteps, seed)` — Signatur bleibt.
- `Edge(railCondition, overheadCondition, repairRail(...), repairOverhead(...),
  effectiveCondition())` — die Mutatoren, die der Planer ggf. für "Reparaturzeit" nutzt.
- `Wear.accumulate(edge, massKg, speedMps, dtSeconds, rng)` — die deterministische
  Verschleiß-Formel.
- `PowerGrid.availableW(requestedW, distanceMeters, condition, dtSeconds)` — die
  Stromangebots-Funktion.
- `ManualGenerator` (100 kW, 1000 MJ Brennstoff), `PlayerLabor` (5/s, max 20) — die
  Defaults, die der Planer ggf. als "Standard-Spielwelt" annimmt.
- **Neu in P3 zu benennen:** `Planner.predict(route, consist, netState) → Prognose` —
  asymmetrisch zum Simulator (grob, ein Zug, kein Verkehr, kein Override), aber **dieselbe
  Physikfunktion** (Regel 2).

### E. Test-Lücke, die P3 oder P4 schließen muss

- **Multi-Edge-Regel-5-Invariante:** ein Test mit zwei Kanten, der zeigt, dass die
  ungenutzte Kante `condition == 1.0` behält, während der Token auf der anderen fährt.
  Konzeptionell trivial (`Wear.accumulate` läuft nur auf der aktuellen Edge), aber als
  Test nicht da. Wenn der Token in P3 oder P4 Kanten wechselt, sollte dieser Test
  geschrieben werden — am besten als Erweiterung in `WearIntegrationTest`.

---

## Dateipfade als Verweise (für den P3-Chat)

| Was | Pfad | Warum |
|---|---|---|
| Overall Plan (Wahrheit) | `TRAKTION_OVERALL_PLAN.md` | §2 Locks, §3 Hard Rules, §3.2 Ports, §4 Ziele, §5/P3, §9 Anti-Patterns |
| Preregistration (FROZEN) | `M1_PREREGISTRATION.md` | §3 Metriken, §7 Trial-Zählung. Nie editieren. |
| P2-Plan (bilanziert) | `docs/plans/PHASE2_PLAN.md` | Schritt-Sequenz, Akzeptanzkriterien, T-D25–T-D34 — Referenz, nicht neu lesen |
| P1-Handover (Vorphase) | `docs/plans/PHASE1_HANDOVER.md` | P1-Abschluss, offene P2-Fragen, Watchpunkte |
| P2-Build-Log + Session-stopped | `phase2/CLAUDE.md` | P2-Abschluss-Block, Step-Status |
| Konventionen | `docs/CONVENTIONS.md` | Logging, Testmatrix, Root-Layout |
| Architektur-Schnitt | `ARCHITECTURE.md` | train-core / train-mc, die zwei Ports |
| Build-Files | `gradle.properties`, `train-core/build.gradle.kts` | gepinnte Versionen, jqwik aktiv |
| Kern-Quellen | `train-core/src/main/java/de/traktion/traincore/` | 17 Typen — Code ist Wahrheit |
| Kern-Tests | `train-core/src/test/java/de/traktion/traincore/` | 208 Tests, davon `FixedSupply` (Test-Hilfe), 4 jqwik-Properties |

---

## Rohdaten für die trials.jsonl-Zeile(n) — an Nikinger, nicht in die Datei

> Plan §7: "Kein Agent schreibt in `trials.jsonl`. Die Messung gehört nicht dem Gemessenen."
> Diese Rohdaten liefere ich als Text. Nikinger trägt ein. P2 ist eine zusammenhängende
> Kategorie-A-Phase (Verschleiß + Ports, ein Z-Durchstich). Konsistent mit P1: **eine
> aggregierte P2-Zeile**.

### P2 — Verschleiß + Ports (Kategorie A, Z4-Abschluss + Z6 + Z7 + beide Ports produktiv)

- `ts`: 2026-08-15 (Handover-Datum; Steps liefen laut Build-Log am 2026-08-15)
- `phase`: P2
- `ziel`: Verschleiß-Durchstich (Z6, Z7) + Ports produktiv (Regel 3 für beide Ports) +
  Z4-Abschluss (`condition` → Spannungsabfall)
- `kategorie`: A
- `harness`: opencode
- `modell`: minimax-coding-plan/MiniMax-M2.7
- `effort`: agent
- `iterationen`: 16 Commits (Step 0.2, Step 1–9), davon 9 Domänen-Commits
  (`beb79c3`, `ae5a474`, `d37c0af`, `fcddcdc`, `872bfe9`, `19183a4`, `8437eab`,
  plus die Step-0.2-`m1: backfill` und die Step-9-`docs: close`). 7 Doku-Commits.
  **Keine** Test-Fix-Iteration — jeder Step lief beim ersten `gradle :train-core:test` grün.
- `diff_lines`: **3031 insertions, 174 deletions** über den P2-Strang
  (Commit `718b6db..f4760e2`). Step-spezifisch (siehe `git show <commit> --stat`):
  Step 2 (Edge) ~250, Step 3 (Wear) ~250, Step 4 (PowerGrid) ~140, Step 5
  (MaintenanceSupply+PlayerLabor) ~330, Step 6 (ManualGenerator) ~470, Step 7
  (SoftlockInvariant) ~290, Step 8 (WearIntegration) ~360.
- `tests_gruen`: **208** (SmokeTest 1, JqwikSmokeTest 1 [Property], RailGraphTest 20,
  ConsistTest 10, PhysicsTest 14, PowerGridTest 21 [war 15 in P1, +6 für condition],
  SimulatorTest 16, BlockSectionTest 18, IntegrationTest 6, EdgeTest 16, WearTest 11,
  MaintenanceSupplyTest 28, ManualGeneratorTest 32, SoftlockInvariantTest 4 [JUnit] +
  3 [Property], WearIntegrationTest 7). Davon 4 @Property (jqwik).
- `regressionen`: **0** (alle P1-Tests weiterhin grün, P1-Verhalten mit `condition = 1.0`
  rückwärtskompatibel)
- `operator_eingriffe`: **0** während der P2-Domänen-Steps. Eine Korrektur im Step-4-Test
  war eine Selbstkorrektur des Build-Agenten (eine `Edge`-Instanz wurde zwischen zwei
  Simulator-Läufen geteilt, was bei der neuen Mutable-Condition sichtbar wurde) — das ist
  kein Operator-Eingriff, sondern ein Test-Bug-Fix durch den Agenten.
- `regel2_verstoss`: **nein** — genau eine `requiredPowerW` in `Physics.java:57`; Simulator
  ruft auf (`Simulator.java:142`); `Wear.accumulate` ist eine eigenständige Formel, kein
  Duplikat. P3-Watchpunkt bleibt intakt.
- `z5_tautologie`: **nein** (nicht anwendbar — kein Planer in P2; P3 ist die Watch-Phase)
- `recherche_schritte`: **0** (P2 ist reines Java in `train-core`, keine 26.2-API)
- `notiz`: P2 abgeschlossen. Z4 abgeschlossen (condition → Spannungsabfall), Z6 grün
  (Langlauf 10k Ticks degradiert messbar, nie blockiert), Z7 grün (Bootstrap-Invariante
  property-based bewiesen, Regel 4 erfüllt). Beide Ports produktiv — `ManualGenerator`
  (Port 1) + `PlayerLabor` (Port 2), Regel 3 für beide Ports erfüllt. Determinismus
  bestätigt (T-D24, T-D33 — `WearIntegrationTest.determinism_sameSeedSameEndState` mit
  Toleranz 1e-9). Kein §9-Verstoß. jqwik Anti-AI-Klausel nicht ausgelöst (1.9.0).
  **Bekannte Test-Lücke:** keine Multi-Edge-Regel-5-Invariante (Regel 5 ist konzeptionell
  erfüllt — `Wear.accumulate` läuft nur auf der aktuellen Edge des Tokens — aber kein
  expliziter Test mit zwei Kanten, der zeigt, dass die ungenutzte Kante `condition == 1.0`
  behält; siehe Handover Abschnitt "Was NICHT funktioniert hat").

---

## Session stopped

> Dieser Block ist die Phasen-Abschluss-Analyse. Der operative `## Session stopped`-Block
> (letzte P2-Session) bleibt in `phase2/CLAUDE.md` stehen — er wird nicht hierher verschoben.
> P3 beginnt mit `phase3/CLAUDE.md` als erster Aktion (Plan §11). Diese Datei enthält keinen
> operativen Session-stopped-Block.

**Diese Session (P2-Handover):**
- P2 systematisch gegen Plan §4/§9 ausgewertet. Kein §9-Verstoß im Code.
- Z4 abgeschlossen, Z6 + Z7 grün, beide Ports produktiv, Regel 3 für beide erfüllt
  (208 Tests, 0 Failures, davon 4 jqwik-Properties).
- Regel 2 intakt (genau eine `requiredPowerW`); Z5-Tautologie nicht anwendbar (kein Planer).
- Determinismus bestätigt (T-D24, T-D33).
- Eine Test-Lücke identifiziert (Multi-Edge-Regel-5-Invariante) — kein Verstoß, nur
  Coverage-Lücke. P3-Hinweis im Handover dokumentiert.
- Doku-Drift in `phase2/README.md` und `phase2/CLAUDE.md` im selben Commit nachgezogen.
- Rohdaten für trials.jsonl als Text an Nikinger geliefert (eine aggregierte P2-Zeile).

**Next (P3, neue Session):**
1. `phase3/CLAUDE.md` als allererste Aktion (Plan §11).
2. `PHASE3_PLAN.md` entwerfen (Plan §5/P3): `Planner.predict(route, consist, netState) →
   Prognose` — grobe Auflösung, ein Zug, kein Verkehr, **dieselbe** `Physics.requiredPowerW`
   (Regel 2). **Niemals** den Simulator aufrufen (Z5-Tautologie, §9).
3. Engpass-Erkennung, Soll/Ist-Vergleich als Zahlen (Z11-Kern-Anteil).
4. Z5 property-based mit jqwik, Generator für Netze/Verschleiß/Last/Profil, Vertragsgrenze
   (Override oder fremder Verkehr → "nicht anwendbar").
5. Done-When P3: Z5 grün über ≥ 1000 generierte Fälle, kein Fall über 5 % Abweichung.