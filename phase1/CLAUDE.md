---
status: live
purpose: Phasen-Kopf für P1 — train-core Durchstich (Z1–Z4). Build-Log + der aktuelle Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P1; vor jedem P1-Schritt; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE1_PLAN.md      # Konzept/Plan, aus dem P1 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-07-20
---

# Phase 1 — `train-core`: Durchstich

> **Status:** P1 läuft. Step 0 (Altlasten) ✅ · Step 0b (Doc-Drift) ✅ · Step 1 (jqwik) ✅ ·
> Step 2 (Phasen-Kopf) ✅ · Step 3 (RailGraph, Z1) ✅ · Step 4 (Consist, T-D7) ✅ ·
> Step 5 (Physics.requiredPowerW, Regel 2) ✅ · Step 6 (PowerGrid + PowerSupply, Z4) ✅.
> Nächster Schritt: Step 7 (Simulator, Z3, T-D13, T-D24).
>
> **Konzept:** `docs/plans/PHASE1_PLAN.md` (gelockte Entscheidungen T-D20–T-D24, Schritt-Sequenz,
> Akzeptanzkriterien).
>
> **Kategorie A** (Plan §5/P1, §7). Klassische Informatik: Graphen, Physik, Energiebilanz,
> Zustandsautomaten. Kein Minecraft, kein Verschleiß, kein Planer.

---

## Build-Log

| Schritt | Status | Commit | Notiz |
|---|---|---|---|
| Step 0.1 — build-traktion.md committen (deny→ask) | ✅ | d4916c9 | Permission-Änderung committet |
| Step 0.2 — fabric.mod.json Java 21→25 | ✅ | 188bea2 | Manifest an Code-Wahrheit angeglichen |
| Step 0.3 — P0-Trials Rohdaten an Nikinger | ✅ | (dieser Commit) | Rohdaten bestätigt, 4 Zeilen in trials.jsonl eingetragen |
| Step 0b — Doc-Drift prüfen | ✅ | — | Keine Drift gefunden (leer, wie vom Plan erwartet) |
| Step 1 — jqwik 1.9.0 einkommentieren (T-D20) | ✅ | 560b178 | [VERIFY] aufgelöst: jqwik läuft unter Gradle 9.5.1 |
| Step 2 — phase1/CLAUDE.md + SESSIONS_ARCHIVE + README | ✅ | (dieser Commit) | Phasen-Kopf erstellt |
| Step 3 — RailGraph (Z1) | ✅ | (dieser Commit) | Node, Edge, RailKind (5 Werte), RailGraph mit Invarianten; 17 Tests grün |
| Step 4 — Consist (T-D7) | ✅ | (dieser Commit) | Record mit carCount/tareMassKg/payloadMassKg, totalMassKg(); 10 Tests grün |
| Step 5 — Physics.requiredPowerW (Regel 2, Z3 prep) | ✅ | (dieser Commit) | EINE Funktion, Rekuperation bei Gefälle; 14 Tests grün |
| Step 6 — PowerGrid + PowerSupply (Z4 ohne condition, T-D22) | ✅ | (dieser Commit) | Port + FixedSupply (Test), linearer Spannungsabfall, Reset; 15 Tests grün |
| Step 7 — Simulator (Z3, T-D13, T-D24) | ⏳ | — | |
| Step 8 — BlockSection (Z2, T-D23) | ⏳ | — | |
| Step 9 — Integration (A→B, Stromknappheit, Z2+Z3) | ⏳ | — | |
| Step 10 — Done-When-Verifikation + Phasen-Abschluss | ⏳ | — | |

---

## Session stopped — 2026-07-20 (P1 Step 3–4: RailGraph Z1, Consist T-D7)

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

### Beobachtungen / Messpunkte
- **Regel-2-Verstoß:** nein (keine Physik in Step 3 — `Physics` kommt Step 5).
- **Z5-Tautologie:** nein (kein Planer/Simulator in Step 3).
- **Interface ohne zwei Implementierungen (Regel 3):** `RailGraph` ist eine konkrete Klasse,
  kein Interface. `RailKind` ist ein Enum (Regel 3 gilt für Interfaces, nicht Enums — T-D21).
  Kein Anti-Pattern.
- **Determinismus (Regel 8):** `LinkedHashMap`/`LinkedHashSet`, geordnete Iteration getestet
  (`nodes_iterateInInsertionOrder`, `edges_iterateInInsertionOrder`).
- **Anti-Pattern-Check (§9):** `grep` bestätigt — kein `net.minecraft.*` (nur Kommentar in
  `package-info.java`), kein NBT, kein `ItemStack`, kein `System.out` in main, kein rohes
  `HashMap`/`HashSet`.

### Next
- **Step 7 — Simulator (Z3, T-D13, T-D24):** fixed-dt-Substep-Schleife, semi-implizites Euler.
  `Token` (Position, Geschwindigkeit). Simulator ruft `Physics.requiredPowerW` auf (Regel 2 —
  kein Formel-Duplikat). Token bewegt sich A→B (Z3), Unterversorgung bremst. Determinismus-Test
  (T-D24: zwei Läufe, gleich Seed → gleich Endzustand). Geordnete Collections, gesäter Zufall,
  keine Wall-Clock, kein HashSet in der Physikschleife (Regel 8).

### Open questions / blockers
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** bleibt bis P4 (nicht P1-relevant).
- **Tool-Calls:** Diese Session (Step 3) benutzte ~10 Tool-Calls; Session gesamt (Step 0–3)
  ~36 Tool-Calls. Nähert sich dem Handover-Limit.
