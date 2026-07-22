---
status: live
purpose: Menschliche/public-Oberfläche der Phase 1 — train-core Durchstich. Kurzüberblick für Contributors, die nicht den ganzen Phasen-Kopf lesen wollen.
read-when: Repo-Besuch; bevor jemand in P1 einsteigt, ohne die Doku-Hierarchie zu kennen
detail: L1
up: ../README.md
down:
  - ./CLAUDE.md   # Phasen-Kopf mit Build-Log + Session-stopped
updated: 2026-07-22
---

# Phase 1 — `train-core`: Durchstich

> **Kategorie A** (Plan §5/P1). Klassische Informatik: Graphen, Physik, Energiebilanz,
> Zustandsautomaten. **Kein Minecraft, kein Verschleiß, kein Planer.**
>
> **Wahrheit:** `docs/plans/PHASE1_PLAN.md` (Schritt-Sequenz, Akzeptanzkriterien, T-D20–T-D24).
> **Phasen-Kopf:** `phase1/CLAUDE.md` (Build-Log + aktueller Session-stopped-Block).

## Ziel

Ein Zug fährt in einem Unit-Test von A nach B und wird langsamer, wenn Leistung fehlt.
Am Ende von P1: Z1–Z4 grün in `train-core`, null externe Abhängigkeiten außer Test-Bibliotheken,
zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8).

## Komponenten (Plan §5/P1)

- `RailGraph` — Knoten, Kanten, `RailKind`, `gradient`, Länge (Z1)
- `Consist` — `carCount`, `tareMassKg`, `payloadMassKg` (T-D7)
- `Physics` — **eine** Funktion `requiredPowerW(consist, speed, gradient)` (Regel 2)
- `PowerGrid` — Bedarf, Angebot über `PowerSupply`, Unterwerk-Reset (Z4 ohne `condition`)
- `Simulator` — fixed-dt-Substep-Schleife, Token bewegt sich, Unterversorgung bremst (Z3, T-D13)
- `BlockSection` — Reservierung, Kollisionsfreiheit, Deadlock-Erkennung (Z2)

## Status

**P1 ist abgeschlossen.** Alle Steps ✅. Z1–Z4 grün in `train-core` (101 Tests), null externe
Runtime-Abhängigkeiten außer Test-Bibliotheken (JUnit, jqwik), Determinismus bestätigt (Regel 8,
T-D24), Regel 2 intakt (genau eine `requiredPowerW`-Definition). Siehe `phase1/CLAUDE.md` Build-Log
für die Schritt-für-Schritt-Übersicht. Nächster Schritt: P2 (Verschleiß + Ports) in neuer Session.
