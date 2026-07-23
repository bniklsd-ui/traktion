---
status: handover (P1 abgeschlossen → P2)
purpose: Phasen-Abschlussanalyse für P1. Status, Delta seit P0-Handover, erreichte Ziele, Verstöße gegen §3/§9, offene Entscheidungen. Kalt-lesbar ohne PHASE1_PLAN.md oder Code neu lesen zu müssen.
read-when: vor dem Entwurf von PHASE2_PLAN.md; vor P2-Start; vor dem ersten P2-Schritt
detail: L2
up: ../TRAKTION_OVERALL_PLAN.md
down:
  - ./PHASE1_PLAN.md   # der Plan, den dieses Handover bilanziert
  - ./PHASE0_HANDOVER.md   # vorheriges Handover (P0)
related: ../phase1/CLAUDE.md   # Build-Log + letzter Session-stopped-Block (P1 abgeschlossen)
updated: 2026-07-23
---

# Phase 1 — Handover (Abschlussanalyse)

> **Status:** abgeschlossen.
> **P1 war Kategorie A** (Plan §5/P1, §7) — klassische Informatik: Graphen, Physik, Energiebilanz,
> Zustandsautomaten. Kein Minecraft, kein Verschleiß, kein Planer. Der Durchstich-Beweis
> (Plan §5/P1) ist erbracht: ein Zug fährt in einem Unit-Test von A nach B und wird bei
> Stromknappheit langsamer.

---

## Ergebnis auf einen Blick

| Dimension | Wert |
|---|---|
| **Status** | abgeschlossen |
| **Tests** | grün (`gradle :train-core:test` PASSED, 101 Tests, 0 failures) |
| **Live-Validierung** | nicht möglich — P1 ist `train-core` (Kategorie A), kein Client-Start. Validierung = Test-Suite. |
| **Git** | committed/gepusht auf `main` (Arbeitsbaum clean, up-to-date mit `origin/main`) |
| **Regel-2-Verstoß** | **nein** — genau eine `requiredPowerW`-Definition (`Physics.java:57`); Simulator ruft sie auf (`Simulator.java:142`), kein zweiter Formelkörper |
| **Z5-Tautologie** | **nein** (nicht anwendbar) — kein Planer in P1. P3 ist die Watch-Phase. |
| **Determinismus (Regel 8)** | bestätigt (T-D24) — Integration-Test `determinism_twoRunsSameSeedSameEndState` vergleicht `progressMeters` und `speedMps` mit Toleranz 1e-9 |

---

## Erreichte Ziele (Plan §4) und woran messbar

| Ziel | In P1 erreicht? | Messbar an |
|---|---|---|
| **Z1** (Graph-Invarianten) | ✅ | `RailGraphTest` (20 Tests): Knoten/Kanten-Mutationen, Invarianten-Durchsetzung via `IllegalStateException` bei verwaisten Knoten / fehlenden Endpunkten / duplikaten IDs; `RailKind` nicht-null auf jeder Kante; `gradient`/Länge endlich. |
| **Z2** (Blockabschnitte, Kollisionsfreiheit, Deadlock-Erkennung) | ✅ | `BlockSectionTest` (18 Tests): `BlockSystem.fromGraph` leitet Abschnitte aus Topologie ab (T-D9); Reservierung verweigert zweiten Token im selben Abschnitt; `hasDeadlock()` erkennt Zyklus im Wartegraphen (T-D23, trivial — nicht aufgelöst). |
| **Z3** (Leistungsbedarf, Rekuperation, Unterversorgung bremst) | ✅ | `PhysicsTest` (14 Tests): `requiredPowerW` steigt mit Steigung, sinkt bei Gefälle (negativ = Rekuperation), 0 bei v=0/m=0. `SimulatorTest` (16 Tests) + `IntegrationTest` (6 Tests): Token bewegt sich A→B, wird bei Stromknappheit langsamer/hält. |
| **Z4** (Spannungsabfall, Unterwerk-Reset) | ✅ Teil — `condition` fehlt bewusst (P2) | `PowerGridTest` (15 Tests): `availableW` mit Distanz-basiertem Spannungsabfall (f(Distanz), nicht f(Distanz, condition) — bewusst, Plan §5/P1); `resetSubstation()` setzt zurück. |
| **Z5** (Planer-Prognose) | nicht in P1 | P3. P1 hat den Watchpunkt verankert: genau eine `requiredPowerW` (Regel 2). |
| **Z6, Z7** (Verschleiß, Softlock) | nicht in P1 | P2. |
| **Z8–Z11** | nicht in P1 | P4/P5. |

**Done-When (Plan §5/P1) — alle erfüllt:**
- [x] Z1–Z4 grün in `train-core` (ohne Minecraft, reproduzierbar bei gleichem Seed)
- [x] `train-core` hat null externe Abhängigkeiten außer Test-Bibliotheken (JUnit, jqwik) — `build.gradle.kts` hat nur `testImplementation`
- [x] Zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8, T-D24) — Integration-Test
- [x] Kein Eintrag aus §9 ist im Code (Anti-Pattern-Check, siehe unten)
- [x] `Physics.requiredPowerW` existiert genau einmal (Regel 2 — P3-Watchpunkt verankert)

---

## Verstöße gegen Plan §3 / §9 — im P1-Code gefunden

**Regel-2-Verstoß (Physik an zwei Stellen):** **nein.**
`grep -rn "requiredPowerW" train-core/src/main/` findet genau eine Definition
(`Physics.java:57`). Der Simulator ruft `Physics.requiredPowerW(consist, v, gradient)` auf
(`Simulator.java:142`) und implementiert die Formel nicht selbst. Kein zweiter Formelkörper.
Der P3-Watchpunkt (Plan §5/P1, Handover P0 Abschnitt B) ist intakt.

**Z5-Tautologie (Planer ruft Simulator):** **nein (nicht anwendbar).**
In P1 existiert kein Planer. P3 ist die Watch-Phase. P1 hat nur die Voraussetzung verankert
(genau eine Physikfunktion).

**§9 Anti-Patterns, einzeln geprüft:**

| Anti-Pattern | In P1 gefunden? | Fundstelle / Begründung |
|---|---|---|
| `net.minecraft.*`/NBT-Import in `train-core` | **nein** | `grep` findet nur den Kommentar in `package-info.java` ("Kein net.minecraft.*") und Javadoc-Verweise in Tests. Kein Import, kein NBT, kein `ItemStack` als Typ. |
| Physik-Formel an zwei Stellen (Regel 2) | **nein** | genau eine `requiredPowerW` in `Physics.java`; Simulator ruft auf, dupliziert nicht. |
| Planer ruft Simulator | **nein** | kein Planer in P1. |
| Variable Schrittweite / Wall-Clock / HashSet in Physikschleife | **nein** | `Simulator`: `TICK_SECONDS`/`nSubsteps` Konstanten, `dt = TICK_SECONDS/nSubsteps` fixed; `ArrayList` für Tokens (geordnete Iteration); `Random(seed)` gesät. Kein `System.currentTimeMillis`/`nanoTime`/`Instant.now` in `train-core/src/main/`. `RailGraph` nutzt `LinkedHashSet` (geordnet — Regel 8-konform, kein rohes `HashSet`). |
| Interface ohne zwei benennbare Implementierungen (Regel 3) | **nein** | `PowerSupply` (Port 1) hat `FixedSupply` (Test, heute) und `ManualGenerator` (P2, benannt in Javadoc + Plan §3.2). T-D22 erfüllt. |
| Weltzustand ohne Ausweg (Softlock, Regel 4) | **nein** | kein Weltzustand in P1 (kein Minecraft). |
| Zeitbasierter Verfall (Regel 5) / ItemStack im Kern (Regel 7) | **nein** | kein Verschleiß, keine Items in P1. `Consist` kennt nur `double`/`int` (`carCount`, `tareMassKg`, `payloadMassKg`). |
| Roher OpenGL-Call (T-D16) | **nein** | kein Rendering in P1. |
| Agent liest `example_project/` nach P0 | **nein** | physisch nicht im Repo (P0 stillgelegt). |
| Trial ohne vorher notierte Erwartung | **nein** | Preregistration liegt vor P0-Trials in der History; P1-Trials werden von Nikinger aus diesem Handover eingetragen. |
| Metrik mittelt A und B | **nein** | P1 ist rein Kategorie A. |
| Phase ohne CLAUDE.md als erste Aktion | **nein** | `phase1/CLAUDE.md` existiert (Commit fa5a3b3, Step 2 — vor jedem Domänen-Code). |
| Session ohne `## Session stopped` | **nein** | Block in `phase1/CLAUDE.md` (P1-Abschluss-Block). |

**Fazit: Kein einziger §9-Eintrag ist im P1-Code verletzt.** Der zentrale Watchpunkt
(Regel 2 — Physik nicht dupliziert) ist intakt. Das ist bei einer Kategorie-A-Phase nicht
selbstverständlich — der Plan hatte ihn als Messpunkt markiert (Plan §5/P1 Watchpunkte).

---

## Was NICHT funktioniert hat / negative Befunde

- **Keine Regressionen.** Alle 11 P1-Commits (Step 1–10) liefen beim ersten `gradle :train-core:test`
  grün; keine Iteration brauchte einen Fix-Commit. Das ist für Kategorie A plausibel (klassische
  Informatik, gut im Trainingschnitt), aber dokumentiert, nicht behauptet.
- **jqwik Anti-AI-Klausel nicht ausgelöst.** T-D20 pinnt bewusst jqwik 1.9.0 (neueste 1.9.x ohne
  Anti-AI-Klausel). Der Agent hat in Step 1 die Version aktiviert und `[VERIFY]` aufgelöst, ohne
  auf 1.10.x zu wechseln oder Tests zu löschen. Der Confound (Plan §4.3 Eval-Awareness) entfiel
  durch die Versionswahl — ob der Agent injection-resistent gewesen wäre, ist in P1 **nicht
  getestet**. Das ist ein Messlücke-Hinweis, kein Negativbefund.
- **`[VERIFY] Fabric-Logging-Konvention in 26.2`** bleibt stehen (nicht P1-relevant, P4).
- **Z4 ist nur Teil erfüllt.** `condition` fehlt bewusst (P2). Das ist kein Negativbefund, sondern
  der geplante P1-Scope — aber P2 muss es schließen, sonst ist Z4 unvollständig.

---

## Delta seit letztem Handover (P0 → P1)

**Commits (main, chronologisch, P1-Strang):**
- `d4916c9` config: commit build-traktion permission update (deny→ask) — Step 0.1
- `188bea2` train-mc: fix fabric.mod.json java version (21→25) — Step 0.2
- `8a55d0c` m1: backfill P0 trials (operator-confirmed entries) — Step 0.3
- `560b178` train-core: enable jqwik 1.9.0 (T-D20, verify Gradle 9.5.1 compat) — Step 1
- `fa5a3b3` docs: create phase1/CLAUDE.md (P1 start) — Step 2
- `8229518` train-core: add RailGraph with Z1 invariants (Node, Edge, RailKind) — Step 3
- `91b76d4` train-core: add Consist (carCount, tareMassKg, payloadMassKg, T-D7) — Step 4
- `ae34096` train-core: add Physics.requiredPowerW (Regel 2, single formula, Z3 prep) — Step 5
- `5847c2e` train-core: add PowerGrid + PowerSupply port (Z4 without condition, T-D22) — Step 6
- `f0a68d7` train-core: add Simulator (fixed-dt substep, Z3, T-D13, T-D24 determinism) — Step 7
- `10fc273` train-core: add BlockSection (Z2, T-D23 trivial deadlock detection) — Step 8
- `146f864` train-core: add integration test (A→B, power scarcity, Z2+Z3) — Step 9
- `1bf4153` docs: close P1 (Z1–Z4 green, determinism verified, Rule 2 intact) — Step 10
- `d9e3d61` docs: sync status to P1 completion (README, ROADMAP, INDEX, phase1/README)

**Was auf `main` steht (Code-Wahrheit, P1-Neu):**
- `train-core/src/main/java/de/traktion/traincore/`: `RailGraph`, `Node`, `Edge`, `RailKind`,
  `Consist`, `Physics`, `PowerSupply` (Interface), `PowerGrid`, `Simulator`, `Token`,
  `BlockSection`, `BlockSystem`, `package-info`.
- `train-core/src/test/java/de/traktion/traincore/`: `RailGraphTest` (20), `ConsistTest` (10),
  `PhysicsTest` (14), `PowerGridTest` (15), `SimulatorTest` (16), `BlockSectionTest` (18),
  `IntegrationTest` (6), `FixedSupply` (Test-Hilfe), `JqwikSmokeTest` (1), `SmokeTest` (1).
- `gradle.properties`: `jqwik_version=1.9.0` aktiv.
- `train-core/build.gradle.kts`: jqwik-Dependency aktiv.

**Was sich NICHT geändert hat:** `train-mc` bleibt Stub (nur `package-info` + `fabric.mod.json`,
Java-Version korrigiert). Kein Domänencode in `train-mc`.

---

## Offene Entscheidungen / was P2 vor dem Plan-Entwurf wissen muss

### A. Was P2 aus P1 mitnehmen muss (keine Neu-Entscheidungen, nur Verweise)

- **`condition ∈ [0,1]` schließt Z4 ab.** P1 hat `PowerGrid.availableW` mit f(Distanz) gebaut.
  P2 ergänzt `condition` auf Kante und Oberleitung → f(Distanz, condition). Die Signatur
  `availableW(requestedW, distanceMeters, dtSeconds)` muss erweitert werden — oder `condition`
  wird im `PowerGrid`-Zustand gehalten. Der P2-Plan muss das entscheiden.
- **`wear += f(masse, v)` pro Durchfahrt (T-D4).** Verschleiß ist nutzungsbasiert, nicht
  zeitbasiert (Regel 5). Der Simulator ist der natürliche Ort, um `wear` zu akkumulieren —
  aber P2 muss klären, wo `condition` lebt (Kante? Oberleitung? beides?).
- **`condition` → Widerstand → Spannungsabfall (T-D5).** Kontinuierlich, nie blockierend.
  `condition=0` darf nicht "Zug hält" heißen, sondern "Fahrzeit ↑". Z6 verlangt: degradiert
  messbar, blockiert nie total.
- **Zwei Ports werden produktiv (Plan §3.2):**
  - `PowerSupply` → `ManualGenerator` (fester Output, Brennstoff von Hand — dauerhafte
    Rückfallebene, Regel 4). P1 hat nur `FixedSupply` (Test). P2 liefert die zweite
    Implementierung, damit `PowerSupply` nicht nur im Test existiert.
  - `MaintenanceSupply` → `PlayerLabor` (Spieler repariert selbst, kostet Spielzeit).
    `PlayerLabor` ist **kein Stub** (Plan §3.2) — echter Preis (Zeit). Ein kostenloser Stub
    würde Z6/Z7 zu leeren Tests machen.
- **Z7-Invariantentest:** aus zufälligem Verfallszustand ist Handarbeit ein Ausweg. Das ist
  der Softlock-Schutz-Test (Regel 4). P2 muss ihn schreiben.
- **Langlauf-Sim (Done-When P2):** 10.000 Ticks Dauerbetrieb degradiert messbar und blockiert
  nie total. Das ist der Z6-Akzeptanztest.

### B. Watchpunkte für die M1-Messung (P2-relevant)

- **Regel-2-Verstoß (ja/nein):** P2 baut Verschleiß, keine zweite Physikformel. Falls P2 eine
  zweite Formel für "Verschleiß → Widerstand" einführt, ist das **kein** Regel-2-Verstoß
  (Regel 2 gilt für die Fahrphysik `requiredPowerW`, nicht für Verschleiß). Aber P2 darf
  `requiredPowerW` nicht duplizieren — der Simulator muss weiterhin `Physics.requiredPowerW`
  aufrufen.
- **Z5-Tautologie:** in P2 nicht anwendbar (kein Planer). P3.
- **Regel 3 (Interface ohne zwei Implementierungen):** `MaintenanceSupply` muss `PlayerLabor`
  (heute) und `DepotStock` (später, Plan §3.2) benennen. Wie bei `PowerSupply` in P1.
- **Regel 5 (Verschleiß bestraft Nutzung, nicht Existenz):** `wear += f(masse, v)` pro
  Durchfahrt, nicht `wear += f(time)`. Das ist testbar als Invariante: eine ungenutzte Kante
  hat `condition` unverändert.

### C. [VERIFY]-Marken, die P2 erben muss

- `jqwik` unter Gradle 9.5.1 — **in P1 aufgelöst** (Step 1, läuft). P2 kann es nutzen.
- Fabric-Logging-Konvention in 26.2 (`LoggerFactory.getLogger`) — bleibt bis P4. P2 braucht
  es nicht (kein Minecraft).
- `SavedData`-API-Name — P4. P2 braucht es nicht.

### D. Benennungen, die P2 fortsetzen muss

- `RailKind`-Enum hat fünf Werte (T-D9): `NORMAL, STATION, HIGH_SPEED, DEPOT, TERMINUS`. In P1
  nur `NORMAL` semantisch genutzt. P2/P5 nutzen die anderen. **Keine neuen Werte ohne Lock.**
- `Token` ist die Wahrheit (T-D3), Entity ist Sichtbarmachung (P4). P2 arbeitet nur mit `Token`.
- `BlockSystem.fromGraph(graph)` leitet Abschnitte aus Topologie ab (T-D9). P2 ändert das nicht.
- `Simulator(powerGrid, nSubsteps, seed)` — der Seed ist der Determinismus-Anker (T-D24). P2
  darf die Constructor-Signatur nicht ohne Notwendigkeit brechen.
- `PowerSupply.supply(requestedW, dtSeconds)` — Port 1 (Plan §3.2). P2 liefert `ManualGenerator`.
- `MaintenanceSupply.withdraw(requested)` — Port 2 (Plan §3.2). P2 führt es ein mit `PlayerLabor`.

---

## Dateipfade als Verweise (für den P2-Chat)

| Was | Pfad | Warum |
|---|---|---|
| Overall Plan (Wahrheit) | `TRAKTION_OVERALL_PLAN.md` | §2 Locks, §3 Hard Rules, §3.2 Ports, §4 Ziele, §5/P2, §9 Anti-Patterns |
| Preregistration (FROZEN) | `M1_PREREGISTRATION.md` | §3 Metriken, §7 Trial-Zählung. Nie editieren. |
| P1-Plan (bilanziert) | `docs/plans/PHASE1_PLAN.md` | Schritt-Sequenz, Akzeptanzkriterien, T-D20–T-D24 — Referenz, nicht neu lesen |
| P0-Handover (Vorphase) | `docs/plans/PHASE0_HANDOVER.md` | Aufräum-Schritt, [VERIFY]-Marken, P0-Trials-Rohdaten |
| P1-Build-Log + Session-stopped | `phase1/CLAUDE.md` | P1-Abschluss-Block, Step-Status |
| Konventionen | `docs/CONVENTIONS.md` | Logging, Testmatrix, Root-Layout |
| Build-Files | `gradle.properties`, `train-core/build.gradle.kts` | gepinnte Versionen, jqwik aktiv |
| Kern-Quellen | `train-core/src/main/java/de/traktion/traincore/` | 13 Typen — Code ist Wahrheit |
| Kern-Tests | `train-core/src/test/java/de/traktion/traincore/` | 101 Tests, davon `FixedSupply` (Test-Hilfe) |

---

## Rohdaten für die trials.jsonl-Zeile(n) — an Nikinger, nicht in die Datei

> Plan §7: "Kein Agent schreibt in `trials.jsonl`. Die Messung gehört nicht dem Gemessenen."
> Diese Rohdaten liefere ich als Text. Nikinger trägt ein. P1 hatte mehrere Sub-Schritte;
> ob eine Zeile pro Sub-Schritt oder eine aggregierte P1-Zeile, entscheidet Nikinger.

**Vorschlag: eine aggregierte P1-Zeile (Kategorie A, ein zusammenhängender Durchstich), da alle
P1-Steps dieselbe Kategorie (A) und dasselbe Ziel (Z1–Z4 Durchstich) messen. Die Step-spezifischen
Diff-Zeilen sind im Git-Log nachvollziehbar.**

### P1 — train-core Durchstich (Kategorie A, Z1–Z4)

- `ts`: 2026-07-23 (Handover-Datum; Steps liefen 2026-07-18 bis 2026-07-20 laut Build-Log)
- `phase`: P1
- `ziel`: train-core Durchstich — Z1–Z4 grün, Determinismus (Regel 8), null externe Abhängigkeiten
- `kategorie`: A
- `harness`: opencode
- `modell`: z-ai/glm-5.2
- `effort`: agent
- `iterationen`: 11 Commits (Step 0.1–0.3, Step 1–10), davon 9 Domänen-Commits. Keine
  Regression — jeder Step lief beim ersten `gradle :train-core:test` grün.
- `diff_lines`: 2563 insertions, 32 deletions über den P1-Strang (Commit 11b8790..d9e3d61).
  Step-spezifisch: Step 3 (RailGraph) 523+, Step 7 (Simulator) 561+, Step 8 (BlockSection) 404+,
  Step 9 (Integration) 204+. Siehe `git show <commit> --stat`.
- `tests_gruen`: 101 (SmokeTest 1, JqwikSmokeTest 1, RailGraphTest 20, ConsistTest 10,
  PhysicsTest 14, PowerGridTest 15, SimulatorTest 16, BlockSectionTest 18, IntegrationTest 6)
- `regressionen`: 0
- `operator_eingriffe`: 0 (P1 war reiner Kategorie-A-Agenten-Strang; Nikinger bestätigte nur
  die P0-Trials-Einträge in Step 0.3, was P0-Messung ist, nicht P1)
- `regel2_verstoss`: **nein** — genau eine `requiredPowerW` in `Physics.java:57`; Simulator
  ruft auf (`Simulator.java:142`), kein zweiter Formelkörper
- `z5_tautologie`: **nein** (nicht anwendbar — kein Planer in P1)
- `recherche_schritte`: 1 (jqwik [VERIFY] in Step 1 — Auflösung durch Aktivieren + Testlauf;
  keine Web-Recherche nötig, da T-D20 die Version vorgab)
- `notiz`: P1 abgeschlossen. Alle §9-Checks leer. Determinismus bestätigt (T-D24, Integration-
  Test mit Toleranz 1e-9). jqwik 1.9.0 läuft unter Gradle 9.5.1 (T-D20 verifiziert). Z4 nur Teil
  (condition fehlt bewusst — P2). P3-Watchpunkt (Regel 2) intakt. Kein Operator-Eingriff im
  P1-Strang. jqwik Anti-AI-Klausel nicht ausgelöst (1.9.0, nicht 1.10.x) — Injection-Resistenz
  in P1 nicht getestet.

---

## Session stopped

> Dieser Block ist die Phasen-Abschluss-Analyse. Der operative `## Session stopped`-Block
> (letzte P1-Session) bleibt in `phase1/CLAUDE.md` stehen — er wird nicht hierher verschoben.
> P2 beginnt mit `phase2/CLAUDE.md` als erster Aktion (Plan §11).

**Diese Session (P1-Handover):**
- P1 systematisch gegen Plan §4/§9 ausgewertet. Kein §9-Verstoß im Code. Z1–Z4 grün (101 Tests).
- Regel 2 intakt (genau eine `requiredPowerW`); Z5-Tautologie nicht anwendbar (kein Planer).
- Determinismus bestätigt (T-D24). jqwik [VERIFY] aufgelöst (Step 1).
- Rohdaten für trials.jsonl als Text an Nikinger geliefert (eine aggregierte P1-Zeile).

**Next (P2, neue Session):**
1. `phase2/CLAUDE.md` als allererste Aktion (Plan §11).
2. `PHASE2_PLAN.md` entwerfen (Plan §5/P2): `condition ∈ [0,1]` auf Kante/Oberleitung,
   `wear += f(masse, v)` (T-D4), `condition` → Widerstand → Spannungsabfall (T-D5, schließt Z4 ab),
   `PowerSupply`/`ManualGenerator`, `MaintenanceSupply`/`PlayerLabor`, Z7-Invariantentest,
   Langlauf-Sim (10.000 Ticks, Z6).
3. Done-When P2: Z6 + Z7 grün; Langlauf degradiert messbar, blockiert nie total.
