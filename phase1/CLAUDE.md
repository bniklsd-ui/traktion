---
status: live
purpose: Phasen-Kopf für P1 — train-core Durchstich (Z1–Z4). Build-Log + der aktuelle Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P1; vor jedem P1-Schritt; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE1_PLAN.md      # Konzept/Plan, aus dem P1 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-07-21
---

# Phase 1 — `train-core`: Durchstich

> **Status:** P1 abgeschlossen. Alle Steps ✅. Z1–Z4 grün, 101 Tests, Determinismus bestätigt,
> Regel 2 intakt. Nächster Schritt: P2 (Verschleiß + Ports) in neuer Session.
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
| Step 7 — Simulator (Z3, T-D13, T-D24) | ✅ | (dieser Commit) | Fixed-dt-Substep, semi-implizites Euler, ruft Physics auf; 16 Tests grün, Determinismus grün |
| Step 8 — BlockSection (Z2, T-D23) | ✅ | (dieser Commit) | BlockSystem.fromGraph, Reservierung, Deadlock-Erkennung; 18 Tests grün |
| Step 9 — Integration (A→B, Stromknappheit, Z2+Z3) | ✅ | (dieser Commit) | Durchstich-Beweis: alle Komponenten zusammen; 6 Tests grün |
| Step 10 — Done-When-Verifikation + Phasen-Abschluss | ✅ | (dieser Commit) | 101 Tests grün, Anti-Pattern-Check leer, Determinismus bestätigt, P1 abgeschlossen |

---

## Session stopped — 2026-07-20 (P1 Step 10: Done-When-Verifikation, P1 abgeschlossen)

### Completed (diese Session)
- **Step 10 — Done-When-Verifikation + Phasen-Abschluss:** alle Checks durchgeführt.
  - `gradle :train-core:test` grün (101 Tests gesamt: SmokeTest, JqwikSmokeTest, RailGraphTest
    17, ConsistTest 10, PhysicsTest 14, PowerGridTest 15, SimulatorTest 16, BlockSectionTest 18,
    IntegrationTest 6).
  - Abhängigkeits-Check: `train-core/build.gradle.kts` hat nur `testImplementation` (JUnit,
    jqwik). Keine Runtime-Abhängigkeiten. (Plan §1, §3 Regel 1)
  - Anti-Pattern-Check (§9): `grep` bestätigt — kein `net.minecraft.*` (nur Kommentar in
    `package-info.java`), kein NBT, kein `ItemStack`, kein `System.out` in main, kein rohes
    `HashMap`/`HashSet` in der Physikschleife, keine Wall-Clock, genau eine `requiredPowerW`-
    Definition (Regel 2 intakt).
  - Determinismus bestätigt (T-D24): Determinismus-Tests in Step 7 und Step 9 grün.
  - Build-Log vollständig: alle Steps ✅.
  - Root-`CLAUDE.md` Phasenstatus aktualisiert: P1 ✅, P2 ⏳.

### P1 Done-When (Plan §5/P1) — alle erfüllt
- [x] Z1–Z4 grün in `train-core` (ohne Minecraft, reproduzierbar bei gleichem Seed)
- [x] `train-core` hat null externe Abhängigkeiten außer Test-Bibliotheken (JUnit, jqwik)
- [x] Zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8, T-D24)
- [x] Kein Eintrag aus §9 ist im Code (Anti-Pattern-Check in Step 10)
- [x] `Physics.requiredPowerW` existiert genau einmal (Regel 2 — P3-Watchpunkt verankert)

### P1 ist abgeschlossen. Nächster Schritt: P2 (Verschleiß + Ports) in neuer Session.

### Open questions / blockers
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** bleibt bis P4 (nicht P1-relevant).
- **Tool-Calls:** Diese Session (Step 10) benutzte ~8 Tool-Calls; Session gesamt (Step 0–10)
  ~70 Tool-Calls über mehrere Fortsetzungen.
