---
status: live
purpose: Phasen-Kopf für P3 — Planer (Z5, Z11). Build-Log + Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P3; vor jedem P3-Schritt; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE3_PLAN.md      # Konzept/Plan, aus dem P3 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-08-15
---

# Phase 3 — Planer

> **Status:** P3 gestartet. Step 1 (dieser) erledigt. Nächster Schritt:
> Step 2 (`RouteForecast` + `Bottleneck` Records).
>
> **Konzept:** `docs/plans/PHASE3_PLAN.md` (gelockte Entscheidungen T-D35–T-D49, Schritt-Sequenz,
> Akzeptanzkriterien).
>
> **Kategorie A** (Plan §5/P3, §7). Kern-Orakel: der Planer muss dieselbe Physik aufrufen wie
> der Simulator (Regel 2), darf aber den Simulator nicht aufrufen (Z5-Tautologie-Schutz, T-D47).
> Statische Utility, eine Implementierung, keine Abstraktion.

---

## Build-Log

| Schritt | Status | Commit | Notiz |
|---|---|---|---|
| Step 0.1 — Drift-Commits prüfen | ✅ | — | Kein Drift gefunden (leer) |
| Step 0.2 — P2-Trials verifizieren | ✅ | — | 6 Zeilen vorhanden, kein Eingriff nötig |
| Step 0b — Doc-Drift prüfen | ✅ | — | Kein Drift gefunden (leer) |
| Step 1 — phase3/CLAUDE.md + SESSIONS_ARCHIVE + README erstellen | ✅ | — | Phasen-Kopf erstellt, P3 = 🔄 |
| Step 2 — RouteForecast + Bottleneck Records (Z11, T-D38/T-D39) | ✅ | — | 229 Tests grün (BottleneckTest + RouteForecastTest + alle P1/P2) |
| Step 3 — Planner.predict() Grundgerüst (T-D35–T-D37/T-D44/T-D45) | ✅ | feb4e78 | 245 Tests grün, PlannerTest 17 Tests, Regel 2 intakt (genau eine requiredPowerW). Hinweis: MAX_SPEED_MPS=50 hatte einen Binary-Search-Früh-Exit-Bug — in Step 4 auf 200 erhöht und korrigiert. |
| Step 4 — Soll/Ist-Vergleich (Z11-Kern, T-D38) | ✅ | a03fda0 | 248 Tests grün (+3 neue: Soll/Ist/Trennung/Monotonie/Multi-Edge), MAX_SPEED_MPS=200 (Binary-Search-Fix), condition als Wirkungsgrad-Multiplikator Modell, T-D38 intakt |
| Step 5 — Bottleneck-Klassifikation (T-D39/T-D43) | ✅ | a30c6bd | 256 Tests grün (+8 neue: SPANNUNG/STEIGUNG/KOMBI/KOMBI/sorted/max3/empty/deterministic), computeBottlenecks mit marginalem Zeitbeitrag, Klassifikation nach Art, Top-3 Sortierung |
| Step 6 — Multi-Edge-Regel-5-Invariante (T-D48, P2-Handover E) | ✅ | 5c66d59 | 6 Tests: tokenOnEdgeA/edgeBUntouched, tokenTraversesABtoC, longRunOnEdgeA, manualMoveToEdge, tokenReachesEndOfEdgeA, determinism (alle grün). Kern-Invariante: nur befahrene Kanten degradieren; unberührte bleiben auf 1.0. Nutzt moveToEdge() für kontrollierte Sequenz — Verschleiß pro Tick ist minimal (k=1e-10), daher direkte Edge-Manipulation statt 1000+ Ticks auf einer Kante zu warten. |
| Step 7 — Simulator.runRoute() Test-Harness (T-D47) | ✅ | d31d8f3 | 11 Tests: empty/null/invalid route, singleEdge, twoEdge, degradedCondition, positiveGradient, determinism (2), startSpeedPositive, nonContiguous, Z5AntiPattern-Dokumentation. maxReachMeters=2000 (condition wirkt auf 500m-Kanten). ManualGenerator 10MW/1e9MJ fuel. Z5-Tautologie-Schutz verifiziert (grep Planner→runRoute: leer). |
| Step 8 — Z5-Property-Test ≥ 1000 Fälle (T-D42, Kern-Orakel) | ⏳ | — | — |
| Step 9 — Done-When-Verifikation + Phasen-Abschluss | ⏳ | — | — |

---

## Session stopped

> **P3-Session-Ende** (2026-08-16, vor P3-Abschluss).
> Steps 6 und 7 abgeschlossen. Step 8 (Z5) begonnen aber mit Modell-Design-Konflikt
> gestoppt — Entscheidung für nächste Session offen.

### Step 6 — Multi-Edge-Regel-5-Invariante ✅
- Commit `5c66d59`: `train-core: add multi-edge Rule-5 invariant test (T-D48, Handover E)`
- 6 Tests: tokenOnEdgeA_onlyEdgeADegrades, tokenTraversesABtoC, longRunOnEdgeA, manualMoveToEdge, tokenReachesEndOfEdgeA, determinism (alle grün)
- 262 Tests gesamt, alle grün
- **Kernbefund:** Verschleiß pro Tick minimal (k=1e-10, 40t×20m/s × 0.0125s/substep ≈ 1e-6/substep). Nutzt `moveToEdge()` für kontrollierte Edge-Sequenzen statt ewig auf natürlichen Verschleiß zu warten.

### Step 7 — Simulator.runRoute() Test-Harness ✅
- Commit `d31d8f3`: `train-core: add Simulator.runRoute test harness (T-D47, Z5 Vergleich)`
- 11 Tests: empty/null/invalid route, singleEdge, twoEdge, degradedCondition, positiveGradient, determinism×2, startSpeedPositive, nonContiguous, Z5AntiPattern-Dokumentation
- **maxReachMeters=2000** (condition wirkt auf 500m-Kanten), ManualGenerator 10MW/1e9MJ fuel
- Z5-Tautologie-Schutz verifiziert (grep Planner→runRoute: leer)
- 273 Tests gesamt, alle grün

### Step 8 — Z5-Property-Test ⚠️ OFFEN — Entscheidung needed
- **Modell-Design-Konflikt entdeckt:**
  - Planner: `availableW(maxPower, 0, 1.0, 1.0) × effectiveCondition` — einfacher Effizienz-Multiplikator
  - Simulator: `availableW(maxPower, distance, condition, dt)` — echter Spannungsabfall mit Distanz
  - Selbst bei `condition=1.0, gradient=0`: ~23% Abweichung (Planner rechnet mit Gleichgewichts-Geschwindigkeit ab Start; Simulator simuliert echte Beschleunigung)
- **JUnit-Edge-Cases (startSpeed>0, downhill, longEdge)**: alle >30% Fehler — strukturell nicht innerhalb 5% mit aktuellem Modell
- **jqwik-Kerntest**: cond∈[0.7,1.0], power∈[200kW,3MW], startSpeed≤10 → 5.06% Fehler im ersten Fall (nur knapp über Grenze)
- **Die 5%-Toleranz aus T-D42 ist mit aktuellem Modell-Design nicht durchgängig erreichbar**

**Für nächste Session — Nikinger-Entscheidung (A oder B):**
- **A:** JUnit-Tests auf 10% Tolerance anpassen, 5% nur für jqwik-Kerntest
- **B:** JUnit-Tests auf Monotonie (degraded>Soll) umbauen ohne direkten 5%-Vergleich
- (Edge-Case-Tests mit 10% tolerieren, ohne explizite Entscheidung)
- **Offene Frage:** Ist die 5%-Toleranz in T-D42 wirklich notwendig oder ist die Monotonie (Trend-Gleichheit) das eigentlich wichtige Z5-Indikator?
