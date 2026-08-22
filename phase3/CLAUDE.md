---
status: live
purpose: Phasen-Kopf für P3 — Planer (Z5, Z11). Build-Log + Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P3; vor jedem P3-Schritt; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE3_PLAN.md      # Konzept/Plan, aus dem P3 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-08-22
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
| Step 8 — Z5-Property-Test ≥ 1000 Fälle (T-D42, Kern-Orakel) | 🔄 | — | Sub-Step 8.1 ✅ (8244593): JUnit-Edge-Cases recastet (Monotonie statt 5%). Sub-Step 8.2 ✅ (7a03dff): Z5_KERNELBEREICH-Konstanten + Klassen-Javadoc. Sub-Step 8.3 ✅ (PHASE3_HANDOVER.md mit Modell-Gap-Sektion, T-D53). Sub-Step 8.4 (Done-When + Phasen-Abschluss) in Folge-Session. 278 Tests grün. |
| Step 9 — Done-When-Verifikation + Phasen-Abschluss | ⏳ | — | Sub-Steps 8.3 ✅ (Handover-Skelett mit Modell-Gap). 8.4 + Step 9 offen: §9-Anti-Pattern-Checks final, Root-CLAUDE.md/ROADMAP/README Status-Updates, SESSIONS_ARCHIVE rotieren. |

---

## Session stopped

> **P3-Handover-Session** (2026-08-22, Plan-Agent M3).
> `docs/plans/PHASE3_HANDOVER.md` erstellt mit Modell-Gap-Sektion (Sub-Step 8.3, T-D53).
> Sub-Steps 8.1 (8244593) + 8.2 (7a03dff) bereits abgeschlossen.
> Sub-Step 8.4 + Step 9 (Phasen-Abschluss) folgen in separater Session.

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

---

### M3 Plan-Agent Konsultation (2026-08-16) — Empfehlung ausstehend

> **Modell-Rollen-Trennung seit P2:** M3 ist Plan-Agent (kognitive Aufgaben, kein Domänen-Code).
> Nikinger hat M3 wegen der schwierigen Modellierungs-Entscheidung in Step 8 beauftragt.

**Diagnose (Code-Wahrheit):**
- `Planner.computeRouteTravelTime` (Planner.java:177-201) projiziert **augenblickliches
  Gleichgewicht pro Kante**: `v = Lösung(P_requiredPowerW == availablePower)` über die ganze
  Kante gehalten.
- `Simulator.stepToken` (Simulator.java:206-248) macht **echtes semi-implizites Euler** mit
  `F = excessW / v`, `a = F / m`, `v += a*dt`, `x += v*dt`, dazu **distanzabhängigen
  Spannungsabfall** via `powerGrid.availableW(..., distanceOnEdge, ...)`.
- **Zwei strukturelle Lücken im Planner-Modell:**
  1. **Beschleunigungsphase fehlt** — der Simulator startet bei `v=0` (oder `startSpeedMps`)
     und beschleunigt über viele Substeps; der Planner ist sofort am Gleichgewicht.
  2. **Distanzabhängiger Spannungsabfall fehlt** — der Planner nutzt `distance=0` durchgehend
     und skaliert die Leistung mit `× condition` als Multiplikator; der Simulator hat
     `effectiveReach = maxReachMeters × condition` und einen linearen Abfall in `distance`.
- Konsequenz: Simulator ist **strukturell langsamer** als Planner — auch bei `condition=1.0,
  gradient=0` durch die Beschleunigungsphase. Die "23% Abweichung" im M2.7-Befund ist genau
  diese Beschleunigungs-Lücke.
- **T-D35 hat "kein Sub-Tick" festgeschrieben** — das ist die Modellwahl, die diese Lücke
  erzeugt. Sie ist kein Implementierungs-Bug, sondern eine bewusste Designentscheidung, die
  jetzt empirisch an der 5%-Grenze scheitert.

**Bewertung der Optionen (M3-Analyse):**

| | A (10% JUnit, 5% jqwik) | B (Monotonie) | C (Planner anreichern) |
|---|---|---|---|
| Z5 als Zahlen-Orakel? | ja im Kern | nein | ja |
| Regel-2-Verstoß möglich? | nein | nein | Risiko (muss über Physics) |
| T-D35-Konformität | gedehnter Kernbereich | faktisch verworfen | muss revidiert werden |
| Studien-Integrität (M1 §6) | neutral | Z5 weichgespült | neutral |
| Aufwand | gering | gering | mittel-hoch (Steps 3-5 neu) |

**M3-Empfehlung: Variante A, mit Konkretisierungen — und C als ehrliche Folge-Perspektive.**

- **Kerntest (jqwik) bleibt 5%**: Die im aktuellen `Z5PropertyTest.java` schon vorgenommene
  Generator-Einengung (`cond ∈ [0.7, 1.0]`, `grad ∈ [-0.03, 0.03]`, 500m-Kanten,
  `power ≥ 200 kW`) ist **kein Trick**, sondern die ehrliche Beschreibung des Bereichs,
  in dem das Gleichgewichtsmodell eine gute Approximation ist. Der 5%-Test ist Z5.
- **JUnit-Edge-Cases recasten, nicht nur Toleranzen hochsetzen**:
  - `z5EdgeCase_longEdge_within5Percent` (2000m-Kante bei `condition=0.9`): testet
    distanzabhängigen Spannungsabfall — eine **Funktion, die der Planner per Design nicht
    hat**. Test **entfernen** mit Kommentar "structural limitation".
  - `z5EdgeCase_gradientDownhillAndUphill_within5Percent`: ±0.03. **Behalten mit 10%** (hier
    ist die Lücke klein), ABER umbenennen/ausrichten auf "Beschleunigungsphasen-Gap".
  - `z5EdgeCase_startSpeedPositive_within5Percent`: `startSpeed=10`. **Behalten mit 10%**.
- **P3-HANDOVER: Befund als Studienergebnis** — Z5 im Kernbereich grün ≤ 5%; außerhalb
  divergiert der Planer **strukturell** (zwei dokumentierte Lücken). Das ist eine
  Modellierungsentscheidung aus T-D35, kein Implementierungs-Bug.
- **Folgephase-Empfehlung**: T-D35 revidieren — "Planner darf eine grobe numerische
  Integration pro Kante durchführen (z.B. 10-20 Sub-Steps mit konstantem dt im
  Sekunden-Bereich, kein Sub-Tick wie der Simulator)". Das ist Option C, für P3.5 oder zu
  Beginn von P4, wenn numerische Genauigkeit außerhalb des Kernbereichs gebraucht wird.

**Was Nikinger entscheiden muss:**
1. **A mit den Konkretisierungen** (Kerntest 5%, JUnit-Edge-Cases recasten, Handover-Doku)? —
   M3-Empfehlung.
2. **B** (Monotonie-only, Z5 als Kern-Orakel aufgeben)? — ehrlicher, aber schwächt die Studie.
3. **C jetzt** (Planner reichert das Modell an, T-D35 revidieren)? — sauberste Lösung, aber
   Steps 3-5 müssen neu; P3 wird faktisch zwei Phasen.

**Trial-Rohdaten-Vorschlag (für Nikinger, nicht in `m1/trials.jsonl` schreiben):**
> `notiz`: "Z5 grün im Kernbereich (cond ∈ [0.7, 1.0], grad ∈ [-0.03, 0.03], power ≥ 200 kW,
> kurze Kanten). JUnit-Edge-Cases strukturell außerhalb der Planner-Modell-Annahme —
> longEdge-Test entfernt, andere zu Monotonie umgebaut. Strukturelle Beschränkung des
> Gleichgewichtsmodells in PHASE3_HANDOVER dokumentiert. T-D35 wird nicht geändert
> (P3-Step-Reichweite), aber Folgephase P3.5/P4 als Anschluss empfohlen."

**Step 8 wartet auf Nikinger-Entscheidung. Keine Code-Änderung durch M3.**

---

### Plan-Agent Sub-Plan-Erstellung (2026-08-16) — Build-Agent-Session startklar

> **Nikinger-Entscheidung (2026-08-16):** Empfehlung A mit Konkretisierungen angenommen.
> Sub-Plan `docs/plans/PHASE3_STEP8_PLAN.md` erstellt (Header-Card, T-D51–T-D54,
> Sub-Step-Sequenz 8.1–8.4 mit Akzeptanzkriterien und Commit-Messages).
> `PHASE3_PLAN.md` aktualisiert: T-D35 mit empirischem Befund annotiert, T-D42 präzisiert,
> Step 8 auf Sub-Plan verwiesen, Done-When angepasst, Header-Card `down:`-Link auf Sub-Plan.
> `docs/INDEX.md` One-Liner für `PHASE3_STEP8_PLAN.md` ergänzt.

**Nächste Build-Agent-Session (M2.7) startet direkt mit:**

1. `phase3/CLAUDE.md` (Build-Log-Tabelle, Session-Block) lesen — sieht ⏳-Status und M3-Befund.
2. `docs/plans/PHASE3_STEP8_PLAN.md` lesen — Header-Card → Sub-Step-Sequenz.
3. Sub-Step 8.1 (`train-core: recast Z5 JUnit edge-cases`) ausführen — longEdge entfernen,
   gradient/startSpeed zu Monotonie/Plausibilität umbenennen.
4. Nach jedem Sub-Step: `gradle :train-core:test` grün, dann Operator fragen (T-D49).

**Atomare Commits für die nächste Session (Sub-Plan):**
- `train-core: recast Z5 JUnit edge-cases (M3 Befund, Monotonie statt 5%)` — ✅ Committed `8244593`
- `train-core: document Z5 kernelbereich in test (T-D51, M3 Befund)` — ✅ Committed `7a03dff`
- `docs: create PHASE3_HANDOVER skeleton with Modell-Gap section (T-D53)` — ✅ Committed in P3-Handover-Session
- `docs: close P3 (Z5 + Z11 green, …, Modell-Gap dokumentiert)` — ⏳ offen (Sub-Step 8.4 + Step 9)

**M3-Session-Block oben bleibt sichtbar** als Diagnose-Quelle; Build-Agent liest ihn on-need-to-know.

---

### P3-Handover-Session (2026-08-22, Plan-Agent M3)

> **Diese Session:** P3 systematisch gegen Plan §4/§9 ausgewertet.
> Kein §9-Verstoß im Code (Regel 2 intakt, Z5-Tautologie-Schutz intakt, alle
> Anti-Pattern-Checks leer). Z11-Kern grün, Z5 als Monotonie im Kernelbereich grün.
> Modell-Gap dokumentiert (Beschleunigungsphase, distanzabhängiger Spannungsabfall).
> `docs/plans/PHASE3_HANDOVER.md` mit Modell-Gap-Sektion erstellt (T-D53, Sub-Step 8.3).
> `docs/INDEX.md` One-Liner ergänzt + `updated:` auf 2026-08-22.
>
> **Status formal: teilweise** — Sub-Steps 8.3 (dieses Dokument) ✅; Sub-Step 8.4 +
> Step 9 offen (Phasen-Abschluss: Root-CLAUDE.md Status P3 = ✅, ROADMAP/README
> Status-Updates, SESSIONS_ARCHIVE rotieren, Build-Log final).
>
> **Nächste Session (Build-Agent M2.7 oder Operator):** P3-Abschluss durchziehen.
> Siehe `docs/plans/PHASE3_HANDOVER.md` Session-stopped-Abschnitt für die volle
> Schritt-Liste.
