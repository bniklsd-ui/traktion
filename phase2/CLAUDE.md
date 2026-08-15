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

> **Status:** P2 gestartet. Steps 0.1, 0.2, 0b erledigt. Step 1 (diese Session) erstellt
> `phase2/CLAUDE.md`. Nächster Schritt: Step 2 (Edge um Verschleiß-Zustand erweitern).
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
| Step 1 — phase2/CLAUDE.md + SESSIONS_ARCHIVE + README erstellen | 🔄 | (dieser Commit) | Phasen-Kopf erstellt, P2 = 🔄 |

---

## Session stopped — 2026-08-15 (P2 Step 1: Phase2-Kopf erstellt)

### Completed (diese Session)
- **Step 0.1 — Drift-Commits:** fünf Dateien committed (6db2cfe).
- **Step 0.2 — P1-Trials:** Rohdaten an Nikinger geliefert, Modell korrigiert (glm-5.2 →
  minimax-coding-plan/MiniMax-M2.7), Zeile eingetragen und committed (718b6db).
- **Step 0b — Doc-Drift:** keine Drift gefunden (leer).
- **Step 1 — phase2/CLAUDE.md:** erstellt mit Header-Card, Build-Log (Steps 0.1/0.2/0b ✅,
  Step 1 🔄), Session-stopped-Block.

### P2 Steps (Plan §5/P2)

- [ ] Step 2 — `Edge` um Verschleiß-Zustand erweitern (Z6, T-D25, T-D26)
- [ ] Step 3 — `Wear` + Integration in Simulator (Z6, T-D31, Regel 5)
- [ ] Step 4 — `PowerGrid` nutzt `condition` für Spannungsabfall (Z4 vollständig, T-D5, T-D27)
- [ ] Step 5 — `MaintenanceSupply` Port + `PlayerLabor` (Z7-Infrastruktur, T-D28, T-D29)
- [ ] Step 6 — `ManualGenerator` (Port 1, zweite Produktions-Implementierung, T-D30)
- [ ] Step 7 — Z7-Bootstrap-Invariante (Property-based Test, T-D32)
- [ ] Step 8 — Z6-Langlauf-Sim (10.000 Ticks, T-D33)
- [ ] Step 9 — Done-When-Verifikation + Phasen-Abschluss

### Done-When P2 (Plan §5/P2)
- [ ] Z6 + Z7 grün in `train-core`
- [ ] Langlauf degradiert messbar, blockiert nie total
- [ ] Beide Ports produktiv (`ManualGenerator`, `PlayerLabor`), Regel 3 erfüllt

### Open questions / blockers
- **Keine** — P2 ist Category A (reines Java), keine 26.2-API-Kontakte.
- **Tool-Calls:** diese Session bisher ~12 Tool-Calls.
