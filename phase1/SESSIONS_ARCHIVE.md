---
status: archive
purpose: Archiv für alte Session-stopped-Blöcke der Phase 1. Rotation: wenn ein neuer Block in phase1/CLAUDE.md geschrieben wird, wandert der bisher-neueste verbatim, uneditiert, newest-first hierher. Kein Editieren alter Blöcke — sie sind Historie.
read-when: bei Bedarf an historische Session-Details von P1
detail: L3
up: ./CLAUDE.md
down:
updated: 2026-07-21
---

# Phase 1 — Session-Archiv

> Alte Session-stopped-Blöcke, verbatim, newest-first. Kein Editieren — Historie.

---

## Session stopped — 2026-07-20 (P1 Step 3–9: RailGraph Z1 bis Integration Z2+Z3)

### Completed (diese Session)
- **Step 3 — RailGraph (Z1):** `RailKind` (Enum, 5 Werte, T-D21), `Node` (Record, `long id`),
  `Edge` (Record, `from`/`to`/`railKind`/`gradient`/`lengthMeters`, kompakter Constructor für
  Z1-Invarianten), `RailGraph` (Knoten/Kanten-Mutationen, Invarianten-Durchsetzung).
  `RailGraphTest` mit 17 Tests — alle grün.
- **Step 4 — Consist (T-D7):** `Consist` (Record, `carCount`/`tareMassKg`/`payloadMassKg`,
  kompakter Constructor für Invarianten: `carCount >= 1`, Massen ≥ 0 und endlich).
  `totalMassKg() = tareMassKg + payloadMassKg`. `ConsistTest` mit 10 Tests — alle grün.
  `gradle :train-core:test` grün (29 Tests gesamt).
- **Step 5 — Physics.requiredPowerW (Regel 2, Z3 prep):** die EINE Physikfunktion.
  `P = (F_roll + F_grade + F_air) * v` mit `F_roll = c_roll*m*g`, `F_grade = m*g*gradient`,
  `F_air = AIR_DRAG_COEFF*v^2`. Konstanten `static final` in `Physics` (sichtbar, nicht
  dupliziert). Rekuperation bei Gefälle (gradient < 0 → P negativ, Z3). `P = 0` bei v=0 oder
  Masse=0. `PhysicsTest` mit 14 Tests — alle grün. `gradle :train-core:test` grün (43 Tests
  gesamt). **Regel 2 intakt:** `grep` bestätigt genau eine `requiredPowerW` in
  `train-core/src/main/`. P3-Watchpunkt verankert.
- **Step 6 — PowerGrid + PowerSupply (Z4 ohne condition, T-D22):** Port `PowerSupply`
  (Interface, Plan §3.2) mit `FixedSupply` (Test-Package) als erster Implementierung,
  `ManualGenerator` (P2) als zweiter benennbarer (Regel 3 erfüllt, T-D22). `PowerGrid`
  modelliert Bedarf/Angebot, linearer Spannungsabfall `deliveredW = requestedW * max(0,
  1 - distance/maxReach)` (Z4 ohne condition — P2 ergänzt condition), Unterwerk-Reset
  (in P1 No-Op, zustandslos — P2 macht echtes Zustandsmanagement). `PowerGridTest` mit 15
  Tests — alle grün. `gradle :train-core:test` grün (58 Tests gesamt).
- **Step 7 — Simulator (Z3, T-D13, T-D24):** `Token` (veränderlich: Position, Geschwindigkeit,
  `maxPowerW`-Budget) und `Simulator` (fixed-dt-Substep, `dt = TICK_SECONDS / N_SUBSTEPS`,
  Default 4, semi-implizites Euler). Physik-Modell: `reqW = Physics.requiredPowerW(...)`
  (Regel 2 — Aufruf, kein Duplikat), `availW = powerGrid.availableW(maxPowerW, distance, dt)`,
  `excessW = availW - reqW`, `a = excessW / (mass * max(v, EPS_V))`, `v = max(0, v+a*dt)`,
  `x += v*dt`. Geordnete Iteration (`ArrayList`, nicht `HashSet` — §9), gesäter Zufall
  (`Random(seed)`, keine Wall-Clock). **Determinismus-Test (T-D24) grün:** zwei Läufe mit
  gleichem Seed → gleicher Endzustand. `SimulatorTest` mit 16 Tests — alle grün.
  `gradle :train-core:test` grün (74 Tests gesamt). **Regel 2 intakt:** `grep` bestätigt
  genau eine `requiredPowerW`-Definition in `train-core/src/main/`, ein Aufruf im Simulator.
- **Step 8 — BlockSection (Z2, T-D23):** `BlockSection` (ein Abschnitt, exklusiver `owner`)
  und `BlockSystem` (verwaltet alle Abschnitte). `BlockSystem.fromGraph(graph)` leitet
  Abschnitte aus der Topologie ab (T-D9: jede Kante ist ein Abschnitt in P1). Reservierung
  exklusiv (Z2: zwei Züge nie im selben Abschnitt), Freigabe. **Triviale Deadlock-Erkennung
  (T-D23):** Zyklus im Wartegraphen (A hält X, will Y; B hält Y, will X → Zyklus → Deadlock).
  Erkannt, **nicht aufgelöst** (Auflösung kommt P5). `BlockSectionTest` mit 18 Tests — alle
  grün (inkl. Drei-Token-Zyklus). `gradle :train-core:test` grün (92 Tests gesamt).
- **Step 9 — Integration (Z2+Z3):** Durchstich-Beweis, dass RailGraph + Consist + Physics +
  PowerGrid + Simulator + BlockSection zusammenarbeiten. `IntegrationTest` mit 6 Tests —
  alle grün: (1) Zug fährt A→B mit ausreichend Strom (Z3), (2) Zug wird bei Stromknappheit
  langsamer (Z3), (3) zwei Züge kollidieren nicht (BlockSection, Z2), (4) zwei Züge auf
  verschiedenen Abschnitten beide reservieren, (5) Determinismus (T-D24: zwei Läufe, gleich
  Seed → gleich Endzustand), (6) vollständiger Durchstich mit allen Komponenten.
  `gradle :train-core:test` grün (101 Tests gesamt).

### Design-Entscheidungen
- **Records für `Node`/`Edge`:** unveränderlich, keine Boilerplate, passt zur "Graph ist
  Wahrheit"-Semantik (T-D2). Ein Knoten/Kante wird nicht mutiert, sondern hinzugefügt/entfernt.
- **`Node` ohne Position:** Plan sagt "ggf. Position". P1 braucht sie nicht (kein Rendering).
  Regel 3: kein Feld, das heute niemand liest. P4 ergänzt sie, wenn Token→Schienenhöhe gemappt
  wird (Spike-Erkenntnis aus phase0/CLAUDE.md).
- **`Edge`-Invarianten im kompakten Constructor:** `railKind != null`, Endpunkte nicht null,
  `gradient`/`lengthMeters` endlich (kein NaN/Unendlich), `lengthMeters > 0`. Der Graph prüft
  zusätzlich, dass beide Endpunkte registriert sind (kein verwaister Knoten).
- **`LinkedHashMap`/`LinkedHashSet` (Regel 8):** Iteration in Einfügereihenfolge, deterministisch.
  Kein `HashMap`/`HashSet` (§9 Anti-Pattern). Der Graph ist nicht die Physikschleife, aber der
  Simulator (Step 7) iteriert über ihn — deterministisch von hier an.
- **IDs vom Caller vergeben:** der Graph ist ein reiner Datencontainer, kein Zufallsgenerator.
  Determinismus (Regel 8) ohne Wall-Clock.
- **`Token` veränderlich, `Consist`/`Node`/`Edge` Records:** der Token IST der Simulationszustand
  (T-D3), er wird pro Substep mutiert. Records wären hier falsch. Consist/Node/Edge sind
  Datenwerte — unveränderlich.
- **`maxPowerW` als Token-Attribut:** der Token fragt sein Leistungsbudget an (nicht reqW), damit
  es Überschuss für Beschleunigung gibt. Bei availW < reqW bremst der Zug (Z3).
- **`BlockSection` aus Topologie (T-D9):** jede Kante ist ein Abschnitt in P1. Komplexere
  Modelle (Abschnitte zwischen Weichen) kommen später.

### Beobachtungen / Messpunkte
- **Regel-2-Verstoß:** nein. `grep` bestätigt genau eine `requiredPowerW`-Definition in
  `train-core/src/main/`. Der Simulator ruft sie auf, implementiert sie nicht selbst. P3-Watchpunkt
  verankert.
- **Z5-Tautologie:** nicht anwendbar in P1 (kein Planer). Ab P3.
- **Interface ohne zwei Implementierungen (Regel 3):** `PowerSupply` hat `FixedSupply` (Test)
  und `ManualGenerator` (P2, benennbar). T-D22 erfüllt. `RailGraph`/`Simulator`/`BlockSystem`
  sind konkrete Klassen, keine Interfaces.
- **Determinismus (Regel 8):** `LinkedHashMap`/`LinkedHashSet`/`ArrayList`, geordnete Iteration,
  `Random(seed)`, keine Wall-Clock. Determinismus-Test (T-D24) grün in Step 7 und Step 9.
- **Anti-Pattern-Check (§9):** `grep` bestätigt — kein `net.minecraft.*` (nur Kommentar in
  `package-info.java`), kein NBT, kein `ItemStack`, kein `System.out` in main, kein rohes
  `HashMap`/`HashSet` in der Physikschleife, keine Wall-Clock.

### Next
- **Step 10 — Done-When-Verifikation + Phasen-Abschluss:** `gradle :train-core:test` grün
  (alle Tests), Abhängigkeits-Check (nur Test-Libs), Anti-Pattern-Check (§9: kein
  net.minecraft/NBT/ItemStack, kein HashSet in Physikschleife, keine Wall-Clock, genau eine
  requiredPowerW), Determinismus bestätigt (T-D24), Build-Log vollständig, Session-stopped
  (P1 abgeschlossen), Root-CLAUDE Phasenstatus: P1 ✅, P2 ⏳.

### Open questions / blockers
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** bleibt bis P4 (nicht P1-relevant).
- **Tool-Calls:** Diese Session (Step 3) benutzte ~10 Tool-Calls; Session gesamt (Step 0–3)
  ~36 Tool-Calls. Nähert sich dem Handover-Limit.

---

## Session stopped — 2026-07-20 (P1 Step 0–2: Altlasten, jqwik, Phasen-Kopf)

### Completed (diese Session)
- **Step 0.1** (d4916c9): `.opencode/agents/build-traktion.md` committet — Permission-Änderung
  (`m1/**`, `M1_*.md`, `docs/plans/**`, `TRAKTION_OVERALL_PLAN.md` von `deny` auf `ask`).
  Stand war bereits da, nur uncommitted. `ask` zwingt zur Bestätigung, was ausreicht.
- **Step 0.2** (188bea2): `train-mc/src/main/resources/fabric.mod.json` Java-Version 21→25.
  `gradle :train-mc:build` grün (keine Regression). Manifest an Code-Wahrheit angeglichen
  (`gradle.properties` `java_mod_target=25`, `train-mc/build.gradle.kts` sagt Java 25).
- **Step 0b:** Doc-Drift geprüft — keine gefunden (wie vom Plan erwartet). `CLAUDE.md`
  Phasenstatus (P0 ✅, P1 ⏳), `docs/INDEX.md` (listet `PHASE1_PLAN.md`), `phase0/CLAUDE.md`
  Session-stopped-Block (verweist auf P1 als Next) — alle korrekt.
- **Step 1** (560b178): jqwik 1.9.0 einkommentiert (T-D20). `[VERIFY] "jqwik unter Gradle 9.5.1"`
  aus P0 **aufgelöst** — jqwik 1.9.0 läuft unter Gradle 9.5.1 mit JUnit 5.12.2.
  `JqwikSmokeTest` beweist, dass die Engine läuft: der erste Property-Test
  (`Math.abs(i) >= 0`) scheiterte am `Integer.MIN_VALUE`-Overflow — genau der Edge-Case, den
  ein handgeschriebene Test oft übersieht. Korrigiert auf `unaryMinusIsTotal` (echte Invariante).
  `.jqwik-database` zur `.gitignore` hinzugefügt (Build-Artefakt).
- **Step 2** (fa5a3b3): `phase1/CLAUDE.md` + `phase1/SESSIONS_ARCHIVE.md` +
  `phase1/README.md` erstellt. `docs/INDEX.md` One-Liner ergänzt. Root-`CLAUDE.md`
  Phasenstatus: P1 = 🔄 aktiv, down-Link auf `phase1/CLAUDE.md`.
- **Step 0.3** (dieser Commit): P0-Trials nachgetragen. Nikinger hat die vier JSONL-Zeilen
  (P0.1, P0.2, P0.3, P0.4) bestätigt. Eingetragen in `m1/trials.jsonl` (4 Zeilen, gültiges
  JSON, je eine pro P0-Sub-Schritt). `diff_lines` = Insertions pro Sub-Schritt;
  `recherche_schritte` sind Schätzungen aus dem Handover. Commit-Message wie vom Plan
  vorgegeben.

### Beobachtungen / Messpunkte
- **jqwik Anti-AI-Klausel (Confound §4.3):** Bei der Recherche/Installation auf jqwik 1.10.x
  gestoßen? **Nein** — der Plan pinnt 1.9.0, und ich habe nicht nach neueren Versionen gesucht.
  T-D20 war klar. Keine Prompt-Injection gelesen, keine Tests gelöscht.
- **Plan-Lücke `phase1/README.md`:** Der P1-Plan Step 2 listet `phase1/README.md` nicht auf,
  obwohl `docs/DOC_LAYERS_CONVENTION.md` "Neue Phase beginnen" Punkt 4 sagt: "phase_N/README.md
  — die menschliche Oberfläche dieser Phase (optional in P0, Pflicht ab P1)." Ich habe es als
  Stub erstellt (folgt der Konvention, die der Plan §11 referenziert) und im Commit-Message
  dokumentiert. Kein Anti-Pattern — die Konvention hat Vorrang für Hygiene-Dateien.
- **`Math.abs(Integer.MIN_VALUE)`-Fehlschlag:** Ein Lehrmoment. jqwik fand in der ersten
  Property genau den Overflow-Edge-Case. Das ist ein früher Beweis, dass Property-Testing
  mehr deckt als handgeschriebene Tests — relevant für Z5 (P3, property-based).

### Next
- **Step 3 — RailGraph (Z1):** `RailGraph`, `Node`, `Edge`, `RailKind` (fünf Werte, T-D21).
  TDD: Invarianten (kein verwaister Knoten, keine Kante ohne zwei Endpunkte, `RailKind` gesetzt,
  `gradient`/Länge endlich). Optional jqwik-Property-Test für Invarianten unter beliebigen
  Mutations-Sequenzen.

### Open questions / blockers
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** bleibt bis P4 (nicht P1-relevant).
- **Tool-Calls:** Diese Session benutzte ~26 Tool-Calls (Lese-Kontext + Step 0–2 + jqwik-Verify +
  Step 0.3 trials-Eintrag).
