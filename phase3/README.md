---
status: live
purpose: Menschliche/public-Oberfläche der Phase 3 — Planer (Z5, Z11). Kurzüberblick für Contributors.
read-when: Repo-Besuch; bevor jemand in P3 einsteigt
detail: L1
up: ../README.md
down:
  - ./CLAUDE.md   # Phasen-Kopf mit Build-Log + Session-stopped
updated: 2026-08-15
---

# Phase 3 — Planer

> **Kategorie A** (Plan §5/P3, §7). Der interessanteste Punkt der Studie (Plan §5/P3, §9 Watch).
> Klassische Informatik mit einem scharfen Architektur-Test: der Planer muss dieselbe Physik
> aufrufen wie der Simulator (Regel 2), darf aber den Simulator nicht aufrufen (Z5-Tautologie).
>
> **Wahrheit:** `docs/plans/PHASE3_PLAN.md` (Schritt-Sequenz, Akzeptanzkriterien, T-D35–T-D49).
> **Phasen-Kopf:** `phase3/CLAUDE.md` (Build-Log + aktueller Session-stopped-Block).

## Ziel

Der Planer (`Planner.predict`) liefert eine Prognose der Fahrzeit für eine Route mit einem Zug.
Er nutzt dieselbe Physikfunktion wie der Simulator (`Physics.requiredPowerW`, Regel 2),
arbeitet aber mit grober Auflösung (pro Kante analytisch, kein Sub-Tick) und ignoriert Verkehr.
Z5 property-based: ≥ 1000 generierte Fälle, < 5% Abweichung gegen den numerischen Simulator.
Z11-Kern-Anteil: Soll/Ist-Vergleich + Bottleneck-Top-3.

## Komponenten (Plan §5/P3)

- `RouteForecast` — Daten-Record: sollFahrzeit, istFahrzeit, deltaProzent, bottlenecks (T-D38)
- `Bottleneck` — Daten-Record: edge, art (SPANNUNG/STEIGUNG/KOMBI), beitragSekunden (T-D39)
- `Planner` — statische Utility, `predict(route, consist, netState, maxPowerW, startSpeedMps) → Optional<RouteForecast>`
- `Simulator.runRoute(...)` — Test-Harness für Z5-Vergleich (T-D47), nicht vom Planer aufrufbar

## Status

**P3 ist gestartet.** Step 1 erledigt (Phasen-Kopf). Nächste Schritte: Step 2 (RouteForecast +
Bottleneck Records), dann Step 3 (Planner-Grundgerüst).
Siehe `phase3/CLAUDE.md` Build-Log für die Schritt-für-Schritt-Übersicht und
`docs/plans/PHASE3_PLAN.md` für die vollständige Planung.
