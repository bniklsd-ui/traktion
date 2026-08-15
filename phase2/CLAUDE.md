---
status: live
purpose: Phasen-Kopf für P2 — Verschleiß + Ports (Z6, Z7). Build-Log + der aktuelle Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P2; vor jedem P2-Schritt; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE2_PLAN.md      # Konzept/Plan, aus dem P2 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-08-15
---

# Phase 2 — Verschleiß + Ports

> **Status:** P2 gestartet. Steps 0.1–8 erledigt. Nächster Schritt:
> Step 9 (Done-When-Verifikation + Phasen-Abschluss).
>
> **Konzept:** `docs/plans/PHASE2_PLAN.md` (gelockte Entscheidungen T-D25–T-D34, Schritt-Sequenz,
> Akzeptanzkriterien).
>
> **Kategorie A** (Plan §5/P2, §7). Klassische Informatik: numerische Bilanzen, Akkumulatoren,
> Property-Tests. **Kein Minecraft, kein Planer.** Verschleiß-Durchstich: `condition` →
> Spannungsabfall, plus zwei produktive Ports.

---

## Build-Log

| Schritt | Status | Commit | Notiz |
|---|---|---|---|
| Step 0.1 — Drift-Commits sync (CLAUDE/ARCHITECTURE/README/phase1/*) | ✅ | 6db2cfe | Fünf Dateien committed |
| Step 0.2 — P1-Trials Rohdaten an Nikinger | ✅ | 718b6db | Modell korrigiert: glm-5.2 → minimax-coding-plan/MiniMax-M2.7 |
| Step 0b — Doc-Drift prüfen | ✅ | — | Keine Drift gefunden (leer, wie vom Plan erwartet) |
| Step 1 — phase2/CLAUDE.md + SESSIONS_ARCHIVE + README erstellen | ✅ | 1f230c2 | Phasen-Kopf erstellt, P2 = 🔄 |
| Step 2 — Edge um Verschleiß-Zustand erweitern (Z6, T-D25, T-D26) | ✅ | beb79c3 | Edge record→class, railCondition + overheadCondition, repairRail/repairOverhead, 16 Tests grün |
| Step 3 — Wear + Integration in Simulator (Z6, T-D31, Regel 5) | ✅ | ae5a474 | Wear.java (Utility), Simulator ruft Wear.accumulate pro Substep, 13 WearTests + alle P1-Tests grün (137 total) |
| Step 4 — PowerGrid nutzt condition für Spannungsabfall (Z4 vollständig, T-D5, T-D27) | ✅ | d37c0af | availableW(condition), Simulator leitet min(rail,overhead) durch, P1-Signatur-Bruch migriert, 134 Tests grün |
| Step 5 — MaintenanceSupply Port + PlayerLabor (Z7-Infrastruktur, T-D28, T-D29) | ✅ | fcddcdc | MaintenanceSupply.java (Interface, Port 2), PlayerLabor.java (Zeit-Akkumulator: rate=5/s, maxWork=20), 28 Tests grün, 163 Tests gesamt |
| Step 6 — ManualGenerator (Port 1, zweite Produktions-Implementierung, T-D30) | ✅ | 872bfe9 | ManualGenerator.java (100kW, 1000MJ Brennstoff, fuelMj in MJ), ManualGeneratorTest.java (32 Tests), IntegrationTest mit PowerGrid, 195 Tests gesamt |
| Step 7 — Z7-Bootstrap-Invariante (Property-based Test, T-D32) | ✅ | 19183a4 | SoftlockInvariantTest.java (3 jqwik Property-Tests + 4 JUnit-Tests), repairLoopTerminates, slowLaborStillTerminates, repairNeverWorsensCondition, 202 Tests gesamt |
| Step 8 — Z6-Langlauf-Sim (10.000 Ticks, T-D33) | ✅ | 8437eab | WearIntegrationTest.java (7 Tests: 10k-Ticks-Degradation, neverBlocked, determinism_sameSeed, degradedVsFresh, longSingleEdge), k=1e-10 ist messbar, 209 Tests gesamt |
| Step 9 — Done-When-Verifikation + Phasen-Abschluss | ✅ | 17ade42 | Anti-Pattern-Check §9 leer, gradle test grün (209), Root-CLAUDE.md P2=✅, P3=⏳, ROADMAP/README aktualisiert, Session-Archiv rotiert |

---

## Session stopped — 2026-08-15 (P2 Step 9: Done-When + Abschluss)

### Completed (diese Session)
- **Step 0.1 — Drift-Commits:** fünf Dateien committed (6db2cfe).
- **Step 0.2 — P1-Trials:** Rohdaten an Nikinger geliefert, Modell korrigiert (glm-5.2 →
  minimax-coding-plan/MiniMax-M2.7), Zeile eingetragen und committed (718b6db).
- **Step 0b — Doc-Drift:** keine Drift gefunden (leer).
- **Step 1 — phase2/CLAUDE.md:** erstellt mit Header-Card, Build-Log, Session-stopped-Block (1f230c2).
- **Step 2 — Edge erweitert:** Edge von Record zu final class konvertiert mit railCondition +
  overheadCondition (double ∈ [0,1], Default 1.0), effectiveCondition() = min(beide),
  repairRail(amount) / repairOverhead(amount). EdgeTest.java: 16 Tests grün.
- **Step 3 — Wear + Simulator:** Wear.java (statische Utility, k=1e-10 pro kg·m/s),
  Simulator ruft Wear.accumulate pro Substep (mass>0 UND speed>0 → Verschleiß, sonst kein
  Effekt, Regel 5). WearTest.java: 12 Tests grün. 136 Tests gesamt.
- **Step 4 — PowerGrid mit condition:** availableW(condition) als 4. Parameter,
  effectiveReach = maxReachMeters * condition, Simulator leitet min(rail,overhead) durch.
  P1-Signatur-Bruch migriert (PowerGridTest mit condition=1.0). Anti-Pattern im Test:
  Edge-Instanz zwischen Simulator-Läufen geteilt → korrigiert. SimulatorDeterminismus-Test
  an neues Verhalten angepasst. 134 Tests grün.
- **Step 5 — MaintenanceSupply + PlayerLabor:** MaintenanceSupply.java (Interface, Port 2),
  PlayerLabor.java (Zeit-Akkumulator: rate=5/s, maxWork=20, withdraw+tick), 28 Tests
  grün. 163 Tests gesamt. Regel 3 erfüllt (zwei Implementierungen benannt: PlayerLabor + DepotStock).
- **Step 6 — ManualGenerator:** ManualGenerator.java implementiert PowerSupply mit endlichem
  Brennstoff-Vorrat (fuelMj in MJ, 1000 MJ Default ≈ 278 kWh), maxOutputW=100kW Default,
  supply(reqW,dt) liefert min(reqW,maxOutputW,fuel*1e6/dt), refuel(amountMJ) als Test-Hook.
  32 ManualGenerator-Tests grün. 195 Tests gesamt. Regel 3 erfüllt für Port 1 (FixedSupply + ManualGenerator).
- **Step 7 — Z7-Bootstrap-Invariante:** SoftlockInvariantTest.java mit 3 jqwik Property-Tests
  (repairLoopTerminates, slowLaborStillTerminates, repairNeverWorsensCondition) + 4 JUnit-Tests
  (worstCaseFromNearZeroTerminates, sumWearMonotonicallyDecreases, repairWithoutTimeDoesNothing,
  largeNetworkFromNearZeroTerminates). 202 Tests gesamt. Z7 vollständig grün.
- **Step 8 — Z6-Langlauf-Sim:** WearIntegrationTest.java mit 7 Tests: 10k-Ticks-
  Degradation (rail<1.0, speed>0), neverBlocked (speed>0 at each 1k checkpoint), determinism
  (same seed→same end), degradedVsFresh (degraded slower), longSingleEdge, singleEdge_tokenStillMoving.
  k=1e-10 ist messbar (condition geht in 10k Ticken messbar runter). 209 Tests gesamt. Z6 vollständig grün.

### P2 Steps (Plan §5/P2)

- [x] Step 0.1 — Drift-Commits sync
- [x] Step 0.2 — P1-Trials Rohdaten an Nikinger
- [x] Step 0b — Doc-Drift prüfen
- [x] Step 1 — `phase2/CLAUDE.md` erstellen
- [x] Step 2 — `Edge` um Verschleiß-Zustand erweitern (Z6, T-D25, T-D26)
- [x] Step 3 — `Wear` + Integration in Simulator (Z6, T-D31, Regel 5)
- [x] Step 4 — `PowerGrid` nutzt `condition` für Spannungsabfall (Z4 vollständig, T-D5, T-D27)
- [x] Step 5 — `MaintenanceSupply` Port + `PlayerLabor` (Z7-Infrastruktur, T-D28, T-D29)
- [x] Step 6 — `ManualGenerator` (Port 1, zweite Produktions-Implementierung, T-D30)
- [x] Step 7 — Z7-Bootstrap-Invariante (Property-based Test, T-D32)
- [x] Step 8 — Z6-Langlauf-Sim (10.000 Ticks, T-D33)
- [x] Step 9 — Done-When-Verifikation + Phasen-Abschluss

### Nächster Schritt
**P2 abgeschlossen.** Nächste Phase: P3 (Planer, Z5 — das Kern-Orakel). Siehe `docs/plans/PHASE2_HANDOVER.md`.

### Done-When P2 (Plan §5/P2) — alle erfüllt
- [x] Z7 grün in `train-core` (Bootstrap-Invariante bewiesen, Regel 4 erfüllt)
- [x] Z6 grün in `train-core` (Langlauf-Sim 10k Ticks bestanden, k=1e-10 messbar)
- [x] Langlauf degradiert messbar, blockiert nie total
- [x] Beide Ports produktiv (`ManualGenerator`, `PlayerLabor`), Regel 3 erfüllt
- [x] Anti-Pattern-Check §9 leer (alle Checks bestanden)
- [x] `Physics.requiredPowerW` genau einmal (Regel 2, P3-Watchpunkt intakt)
- [x] Determinismus bestätigt (T-D24, T-D33)
- [x] `train-core` null externe Runtime-Dependencies (nur JUnit + jqwik)

### Open questions / blocker
- **Keine.** P2 ist Kategorie A (reines Java), keine 26.2-API-Kontakte.
- **Wear-Koeffizient k=1e-10:** ✅ k=1e-10 ist messbar (Step 8 bestätigt — keine Erhöhung nötig).
- **Multi-Edge-Regel-5-Invariante:** keine explizite Testabdeckung (kein Verstoß — Wear läuft
  nur auf aktueller Edge; Coverage-Lücke ist im `docs/plans/PHASE2_HANDOVER.md` als Hinweis
  für P3/P4 dokumentiert).
- **Tool-Calls:** diese Session bisher ~20 Tool-Calls (nach Session-Stop).
