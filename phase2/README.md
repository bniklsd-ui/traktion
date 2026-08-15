---
status: live
purpose: Menschliche/public-Oberfläche der Phase 2 — Verschleiß + Ports. Kurzüberblick für Contributors, die nicht den ganzen Phasen-Kopf lesen wollen.
read-when: Repo-Besuch; bevor jemand in P2 einsteigt, ohne die Doku-Hierarchie zu kennen
detail: L1
up: ../README.md
down:
  - ./CLAUDE.md   # Phasen-Kopf mit Build-Log + Session-stopped
updated: 2026-08-15
---

# Phase 2 — Verschleiß + Ports

> **Kategorie A** (Plan §5/P2, §7). Klassische Informatik: numerische Bilanzen, Akkumulatoren,
> Property-Tests. **Kein Minecraft, kein Planer.**
>
> **Wahrheit:** `docs/plans/PHASE2_PLAN.md` (Schritt-Sequenz, Akzeptanzkriterien, T-D25–T-D34).
> **Phasen-Kopf:** `phase2/CLAUDE.md` (Build-Log + aktueller Session-stopped-Block).

## Ziel

Verschleiß entsteht aus Nutzung, degradiert kontinuierlich, blockiert nie total. Zwei Ports werden
produktiv: `ManualGenerator` (Strom) und `PlayerLabor` (Instandhaltung). Am Ende von P2: Z6 + Z7
grün in `train-core`, 10.000-Ticks-Langlauf degradiert messbar, Bootstrap-Invariante bewiesen.

## Komponenten (Plan §5/P2)

- `Edge` — erweitert um `railCondition` + `overheadCondition` (T-D25, T-D26)
- `Wear` — Verschleiß-Akkumulator, nutzungsbasiert, pro Substep (T-D31, Regel 5)
- `PowerGrid` — nutzt `condition` für Spannungsabfall (T-D5, T-D27, schließt Z4 ab)
- `Simulator` — integriert Verschleiß pro Substep
- `MaintenanceSupply` — Port 2, Interface (T-D28)
- `PlayerLabor` — erste Produktions-Implementierung von `MaintenanceSupply`, Zeit-Akkumulator (T-D29)
- `ManualGenerator` — zweite Produktions-Implementierung von `PowerSupply`, endlicher Brennstoff (T-D30)
- `SoftlockInvariantTest` — Bootstrap-Invariante, property-based (T-D32)

## Status

**P2 ist gestartet.** Steps 0.1, 0.2, 0b erledigt. Step 1 (Phase-Kopf erstellt) läuft.
Nächster Schritt: Step 2 (Edge um Verschleiß-Zustand erweitern). Siehe `phase2/CLAUDE.md` Build-Log
für die Schritt-für-Schritt-Übersicht.
