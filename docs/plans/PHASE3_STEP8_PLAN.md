---
status: plan (ausführungsreif — Empfehlung A, M3-Konsultation 2026-08-16)
purpose: Fokussierter Sub-Plan für P3 Step 8 (Z5-Property-Test) nach M3-Befund. Setzt die in `PHASE3_PLAN.md` Step 8 noch zu grob spezifizierte Akzeptanz um — JUnit-Edge-Cases werden recastet/entfernt, der jqwik-Kerntest bleibt auf 5 % im engen Generator. T-D35 wird nicht geändert (gelockt), aber die Revisions-Notwendigkeit für eine Folgephase ist hier dokumentiert.
read-when: Ausführung von P3 Step 8; nach `docs/plans/PHASE3_PLAN.md`; vor jedem 8.x-Sub-Step; im P3-Handover als Vorlage für die "Modell-Gap"-Sektion
detail: L2
up: ../PHASE3_PLAN.md
down:
  - ./PHASE3_HANDOVER.md                     # entsteht in Step 9, übernimmt die Befund-Sektion-Skelett
  - ../../phase3/CLAUDE.md                    # M3-Session-Block mit Diagnose
related:
  - ../../TRAKTION_OVERALL_PLAN.md            # §4 Z5, §5/P3, §9 Anti-Patterns
  - ../../M1_PREREGISTRATION.md               # §2 Detailgrad, §3 Metriken
supersedes: (kein Vorläufer — ist eine Verschärfung von PHASE3_PLAN.md Step 8)
updated: 2026-08-16
---

# Phase 3 — Step 8 Sub-Plan (Z5 nach M3-Befund)

> **Empfehlung A mit Konkretisierungen** (Nikinger-Entscheidung, 2026-08-16, nach M3-Konsultation).
> Der Build-Agent (M2.7) führt die Sub-Steps 8.1–8.4 nacheinander aus. Keine Rückfragen erwartet —
> der Sub-Plan spezifiziert Akzeptanzkriterien, Test-Struktur und Commit-Messages.

---

## Kontext

**Warum dieser Sub-Plan existiert.** Im WIP-Schritt 8 (`0c7db2b train-core: WIP Z5PropertyTest`)
wurde entdeckt, dass das aktuelle Planer-Modell **strukturell** vom Simulator-Modell divergiert:

| Aspekt | Planner (`Planner.java:177-201`) | Simulator (`Simulator.java:206-248`) |
|---|---|---|
| Geschwindigkeit pro Kante | **augenblickliches Gleichgewicht** aus `Physics.requiredPowerW(v) == availablePower` | **echtes semi-implizites Euler**: `F=excessW/v`, `a=F/m`, `v+=a*dt`, `x+=v*dt` |
| Spannungsabfall | `availableW(..., 0.0, 1.0, 1.0) × condition` (distance = 0) | `availableW(..., distanceOnEdge, condition, dt)` mit `effectiveReach = maxReach × condition` |

**Konsequenz:** Simulator ist strukturell langsamer als Planner — schon bei `condition=1.0, gradient=0`
durch die Beschleunigungsphase, die der Planner wegprojiziert. Bei langen Kanten oder schlechtem Netz
kommt der distanzabhängige Spannungsabfall dazu.

**T-D35** (`"kein Sub-Tick, pro Kante analytisch"`) ist die **Modellwahl**, die diese Lücke erzeugt —
kein Implementierungs-Bug. T-D35 ist gelockt; der Plan-Agent hat das in der M3-Konsultation
explizit als **Finding** gemeldet, nicht stillschweigend geändert. Revisions-Vorschlag siehe unten
(§"T-D35-Revision für P3.5/P4").

**Was dieser Sub-Plan ändert relativ zu `PHASE3_PLAN.md` Step 8:**

- T-D42 wird präzisiert: 5 %-Toleranz bleibt für den **Kerntest** (jqwik, eingeengter Generator),
  die **JUnit-Edge-Cases** werden recastet oder entfernt.
- Die "beliebige"-Route-Forderung in T-D42 (`cond ∈ [0,1]`, `grad ∈ [-0.05, 0.05]`) wird auf den
  realistischen **Kernbereich** eingeengt und im Test dokumentiert.
- Die Handover-Struktur bekommt eine "Modell-Gap"-Sektion (Befund aus M3).
- T-D35 wird **nicht** geändert (gelockt), aber ein Revisions-Hinweis für die Folgephase
  dokumentiert.

**Was dieser Sub-Plan NICHT macht:** Der Planer wird nicht angereichert (Option C der
M3-Konsultation). Das wäre eine neue Modell-Entscheidung, die T-D35 revidieren würde und Steps
3–5 neu bauen müsste. Bleibt der Folgephase vorbehalten.

---

## Architektur-Entscheidungen dieser Sub-Steps

| # | Thema | Lock | Status |
|---|---|---|---|
| **T-D51** | Z5-Kerntest-Generator | **Eingeengter Bereich**: `cond ∈ [0.7, 1.0]`, `grad ∈ [-0.03, 0.03]`, `power ∈ [200 kW, 3 MW]`, `startSpeed ∈ [0, 10] m/s`, Kantenlänge 500 m (Default). Der Kernbereich ist die ehrliche Beschreibung des Bereichs, in dem das Planer-Gleichgewichtsmodell eine gute Approximation des numerischen Simulators ist. Im Test als `Z5_KERNELBEREICH` dokumentiert. | gelockt (Nikinger, 2026-08-16, M3-Empfehlung) |
| **T-D52** | JUnit-Edge-Cases | **Recastet, nicht nur relaxiert**: drei Edge-Cases werden in ihrer Funktion umgebaut: (a) `longEdge`-Test **entfernt** (testet eine Funktion, die der Planner per Design nicht hat), (b) `gradient`-Test wird zu **Monotonie-Test** (`degraded > perfect`, beide Bedingungen geprüft), (c) `startSpeed`-Test wird zu **Monotonie-Test** (`startSpeed=0 ≤ startSpeed=10 ≤ startSpeed=20` → Fahrzeitverhältnis plausibel). | gelockt (Nikinger, 2026-08-16, M3-Empfehlung) |
| **T-D53** | Modell-Gap-Dokumentation im Handover | **Pflicht-Sektion** in `PHASE3_HANDOVER.md` mit drei Befunden: (1) zwei strukturelle Lücken im Planer-Modell (Beschleunigung, distanzabhängiger Spannungsabfall), (2) T-D35 als Ursache, (3) Revisions-Vorschlag für P3.5/P4 mit klarer API-Erweiterung. Die Sektion ist im Sub-Plan als Skelett vorgegeben (siehe unten). | gelockt (Nikinger, 2026-08-16, M3-Empfehlung) |
| **T-D54** | T-D35-Revision (für Folgephase P3.5/P4) | **Vorschlag, keine Umsetzung in P3**: T-D35 wird so erweitert, dass der Planer eine grobe numerische Integration **pro Kante** durchführen darf (z.B. 10–20 Sub-Steps mit `dt` im Sekunden-Bereich, nicht im Sub-Tick-Maßstab des Simulators). Die Integration nutzt weiterhin `Physics.requiredPowerW` (Regel 2) und ist deterministisch (Regel 8). Vorschlag wird in PHASE3_HANDOVER.md "Modell-Gap"-Sektion verbatim übernommen; T-D35 selbst bleibt in dieser Phase **unangetastet**. | vorgeschlagen (M3-Konsultation, 2026-08-16) |

### ⚠ Watchpunkte, die nicht stillschweigend korrigiert werden dürfen

- **Regel-2-Verstoß:** Jegliche Erweiterung des Planers in Sub-Step 8.1/8.2 darf **keine** zweite
  `requiredPowerW`-Implementierung einführen. Die einzige Definition bleibt in `Physics.java:57`.
  Verifikation: `grep -rn "requiredPowerW" train-core/src/main/` zeigt **genau eine** Zeile.
- **Z5-Tautologie:** Der Planer darf auch in der recasteten Form **nicht** `Simulator.runRoute`
  aufrufen — schon gar nicht in Tests. Verifikation: `grep -rn "runRoute" train-core/src/main/java/de/traktion/traincore/Planner.java` → **leer**.
- **Determinismus (Regel 8):** Die Monotonie-Tests in 8.1.b/8.1.c dürfen keinen `Random` einführen,
  keine Wall-Clock, kein HashSet. jqwik-Seed bleibt deterministisch.
- **Locked decisions stay locked:** T-D35 wird in P3 **nicht** geändert. Die Revision ist
  Empfehlung für die Folgephase, kein Eingriff in dieser Phase. Wenn der Build-Agent beim
  Implementieren merkt, dass T-D35 unvermeidlich geändert werden müsste: **STOPP, melden, nicht
  stillschweigend umgehen.**

---

## Sub-Step-Sequenz

> Jeder Sub-Step ist einzeln committbar. Format: `<scope>: <imperative>`.
> Commit ⇒ Build-Log-Eintrag in `phase3/CLAUDE.md` im selben Commit (T-D49, Atomar-arbeiten-Regel).
> TDD in `train-core`: ein Sub-Step ist nicht fertig, bevor `gradle :train-core:test` grün ist.
> **Regime-Wechsel (T-D49):** Nach jedem Sub-Step fragt der Build-Agent den Operator (Nikinger).

---

### Sub-Step 8.1 — JUnit-Edge-Cases recasten

> Drei Tests in `Z5PropertyTest.java` so umbauen, dass sie **nicht mehr** die strukturellen Lücken
> des Planers (Beschleunigung, distanzabhängiger Spannungsabfall) als Test-Ziel haben. Statt
> numerischem 5%-Vergleich: Monotonie- und Plausibilitäts-Tests.

**Dateien:**
- `train-core/src/test/java/de/traktion/traincore/Z5PropertyTest.java`

**Aktionen:**

1. **`z5EdgeCase_longEdge_within5Percent` entfernen.**
   - Test prüft 2000 m-Kante bei `condition=0.9`, was zu Reichweiten-Erschöpfung (`effectiveReach = 2000×0.9 = 1800 m < edge.length`) führt.
   - Der Planner nutzt `distance=0` durchgehend — er **kann** Reichweiten-Erschöpfung gar nicht modellieren.
   - Ersetze den Test durch einen kurzen Kommentar-Block:
     ```java
     // z5EdgeCase_longEdge_within5Percent — ENTFERNT (siehe Sub-Plan T-D52).
     // Hintergrund: Der Planner nutzt `availableW(..., 0.0, 1.0, 1.0) * condition`,
     // also distance=0 durchgehend. Eine 2000-m-Kante bei condition=0.9
     // (effectiveReach=1800 m) erzeugt im Simulator Reichweiten-Erschöpfung, die
     // der Planner per Design nicht abbildet. Test wird in P3.5/P4 mit einem
     // T-D35-revidierten Planner nachgeholt (siehe PHASE3_HANDOVER §Modell-Gap).
     ```

2. **`z5EdgeCase_gradientDownhillAndUphill_within5Percent` → Monotonie-Test.**
   - Umbenennen in `z5EdgeCase_gradient_monotonicityDownhillUphill`.
   - Statt 5%-Vergleich zwischen Planer und Simulator: **drei Plausibilitäts-Assertions**:
     - `assertTrue(sDown > sFlat)`: Simulator: Gefälle → schneller als flach (Rekuperation).
     - `assertTrue(sUp > sFlat)`: Simulator: Steigung → langsamer als flach.
     - `assertTrue(sUp > sDown)`: Simulator: Steigung dauert länger als Gefälle.
   - **Wichtig:** diese Monotonien testen den Simulator allein — kein Planer-Vergleich mehr.
     Der Name `_monotonicityDownhillUphill` macht das klar.

3. **`z5EdgeCase_startSpeedPositive_within5Percent` → Plausibilitäts-Test.**
   - Umbenennen in `z5EdgeCase_startSpeed_producesFiniteTime`.
   - Verbleibt inhaltlich wie der bestehende Test (alle drei startSpeeds > 0, < 300 s). Keine numerische Vergleichs-Assertion mehr.
   - Das ist bereits die richtige Struktur — der bestehende Test prüft nur Plausibilität, nicht 5 %.

**Testliste:**
- `gradle :train-core:test --tests Z5PropertyTest` muss grün sein.
- 4 JUnit-Tests in `Z5PropertyTest.java`: `z5Anchor_degradedSlowerThanPerfect_bothTrendsMatch`,
  `z5EdgeCase_gradient_monotonicityDownhillUphill` (umbenannt),
  `z5EdgeCase_startSpeed_producesFiniteTime` (umbenannt),
  `z5Determinism_plannerAndSimulatorDeterministic`.
- `z5EdgeCase_longEdge_within5Percent` ist **entfernt**, durch Kommentar ersetzt.

**Akzeptanzkriterien:**
- [ ] `z5EdgeCase_longEdge_within5Percent` ist gelöscht, Kommentar dokumentiert das Warum (strukturelle Limitation, Verweis auf PHASE3_HANDOVER §Modell-Gap).
- [ ] `z5EdgeCase_gradientDownhillAndUphill_within5Percent` ist in `_monotonicityDownhillUphill` umbenannt, mit drei Monotonie-Assertions, ohne numerischen Planer-Vergleich.
- [ ] `z5EdgeCase_startSpeedPositive_within5Percent` ist in `_producesFiniteTime` umbenannt, keine numerische Vergleichs-Assertion.
- [ ] `z5Anchor_degradedSlowerThanPerfect_bothTrendsMatch` bleibt unverändert (verwendet bereits Monotonie-Assertions + 5%-Vergleich nur im **Kernbereich**).
- [ ] `z5Determinism_plannerAndSimulatorDeterministic` bleibt unverändert.
- [ ] `gradle :train-core:test` grün.
- [ ] **Frage an Operator:** "Sub-Step 8.1 erledigt — soll ich mit 8.2 weitermachen?"

**Commit-Message:** `train-core: recast Z5 JUnit edge-cases (M3 Befund, Monotonie statt 5%)`

**Zielzuordnung:** Z5 (Akzeptanz im Kernbereich), Z11 (Kern-Anteil).
**Kategorie:** A.

---

### Sub-Step 8.2 — JUnit-Anker + jqwik-Kerntest: Kernelbereich dokumentieren

> Die im bestehenden `Z5PropertyTest.java` schon vorgenommene Generator-Einengung ist nicht
> implementierungs-Zufall, sondern **Z5-Definitionsmenge**. Im Code als Konstante + Kommentar
> festhalten, damit künftige Generator-Erweiterungen bewusst entschieden werden müssen.

**Dateien:**
- `train-core/src/test/java/de/traktion/traincore/Z5PropertyTest.java`

**Aktionen:**

1. **Konstante `Z5_KERNELBEREICH` einführen** (am Anfang der Klasse, nach den existierenden Konstanten):
   ```java
   /**
    * Z5-Kernelbereich — die ehrliche Beschreibung des Bereichs, in dem der
    * Planer (Gleichgewichtsmodell pro Kante, T-D35) eine gute Approximation
    * des numerischen Simulators (semi-implizites Euler, distanzabhängiger
    * Spannungsabfall) ist.
    *
    * <p>Außerhalb dieses Bereichs divergiert der Planer strukturell (zwei
    * Lücken: Beschleunigungsphase, distanzabhängiger Spannungsabfall). Siehe
    * PHASE3_HANDOVER §Modell-Gap und PHASE3_STEP8_PLAN.md T-D52.
    *
    * <p>Werte:
    * <ul>
    *   <li>{@code condMin=0.7, condMax=1.0}: bei condition < 0.7 verstärkt sich
    *       die Beschleunigungsphasen-Lücke (geringere verfügbare Leistung → längere
    *       Anfahrtsphase relativ zur Kantenlänge).</li>
    *   <li>{@code gradMin=-0.03, gradMax=0.03}: bei steiler Steigung dominiert
    *       die Steigungs-Kraft die Beschleunigung — Planer und Simulator kommen
    *       sich wieder näher.</li>
    *   <li>{@code powerMin=200 kW, powerMax=3 MW}: bei sehr kleiner Leistung
    *       ist die Beschleunigung dominant; bei sehr großer Leistung sind
    *       beide Modelle nahe MAX_SPEED_MPS.</li>
    * </ul>
    */
   private static final double KERNEL_COND_MIN = 0.7;
   private static final double KERNEL_COND_MAX = 1.0;
   private static final double KERNEL_GRAD_MIN = -0.03;
   private static final double KERNEL_GRAD_MAX = 0.03;
   private static final double KERNEL_POWER_MIN_W = 200_000.0;
   private static final double KERNEL_POWER_MAX_W = 3_000_000.0;
   private static final double KERNEL_START_SPEED_MAX_MPS = 10.0;
   ```

2. **`conditionArbitrary()`, `gradientArbitrary()`, `powerArbitrary()`, `speedArbitrary()` umstellen:**
   - Die existierenden `between(...)`-Aufrufe durch Verweise auf die `KERNEL_*`-Konstanten ersetzen.
   - Keine Verhaltensänderung — nur dass die Werte zentral dokumentiert sind.

3. **Klassen-Javadoc ergänzen** (am Anfang der Datei, vor `class Z5PropertyTest {`):
   ```java
   /**
    * ... (existierender Javadoc-Block) ...
    *
    * <p><b>Kernelbereich (T-D51, M3-Empfehlung):</b> Die Generatoren sind auf den
    * {@code Z5_KERNELBEREICH} eingeengt. Außerhalb dieses Bereichs divergiert
    * der Planer strukturell (Beschleunigungsphase + distanzabhängiger Spannungsabfall).
    * Das ist eine Modellierungsentscheidung aus T-D35 ("kein Sub-Tick"), kein
    * Implementierungs-Bug. Revisions-Vorschlag siehe PHASE3_STEP8_PLAN.md T-D54.
    *
    * <p>JUnit-Edge-Cases (Sub-Step 8.1): numerische 5%-Vergleiche wurden zugunsten
    * von Monotonie- und Plausibilitätstests recastet, weil die strukturellen Lücken
    * numerisch nicht innerhalb 5 % approximierbar sind.
    */
   ```

**Testliste:**
- Keine Verhaltensänderung — alle Tests müssen weiter grün sein.
- `gradle :train-core:test --tests Z5PropertyTest` grün, gleiche Anzahl Tests (5: 1 jqwik @Property + 4 JUnit, davon 1 JUnit entfernt → 4 JUnit).

**Akzeptanzkriterien:**
- [ ] `KERNEL_*`-Konstanten am Anfang von `Z5PropertyTest.java`, mit Javadoc, das die drei strukturellen Punkte nennt (Beschleunigung, distanzabhängiger Spannungsabfall, T-D35-Ursache).
- [ ] `conditionArbitrary()` etc. nutzen die `KERNEL_*`-Konstanten, nicht mehr harte Zahlen.
- [ ] Klassen-Javadoc zitiert den Kernelbereich-Verweis (PHASE3_HANDOVER §Modell-Gap, PHASE3_STEP8_PLAN.md T-D54).
- [ ] `gradle :train-core:test` grün.
- [ ] **Frage an Operator:** "Sub-Step 8.2 erledigt — soll ich mit 8.3 weitermachen?"

**Commit-Message:** `train-core: document Z5 kernelbereich in test (T-D51, M3 Befund)`

**Zielzuordnung:** Z5 (Akzeptanz-Dokumentation).
**Kategorie:** A.

---

### Sub-Step 8.3 — PHASE3_HANDOVER.md mit Modell-Gap-Sektion (Step 9 Vorbereitung)

> `PHASE3_HANDOVER.md` wird in Step 9 (Done-When) endgültig geschrieben. Aber die
> "Modell-Gap"-Sektion — das Herzstück des M3-Befunds — wird hier als Skelett vorgegeben.
> Der Build-Agent übernimmt es verbatim in Step 9 und ergänzt die anderen Handover-Abschnitte
> (Status, Ziele, §9-Check, Rohdaten).

**Dateien:**
- `docs/plans/PHASE3_HANDOVER.md` (NEU, Skelett mit Header-Card + Skelett-Sektionen)
- `docs/INDEX.md` (One-Liner, im selben Commit)
- `docs/INDEX.md` `updated:` Datumsstempel anpassen

**Header-Card (PHASE3_HANDOVER.md):**
```yaml
---
status: handover (P3 abgeschlossen → P4)
purpose: Phasen-Abschlussanalyse für P3. Status, Delta seit P2-Handover, erreichte Ziele (Z5, Z11), Verstöße gegen §3/§9, offene Entscheidungen für P4 — inkl. der strukturellen Planer-Modell-Lücke (M3-Befund). Kalt-lesbar ohne PHASE3_PLAN.md neu zu lesen.
read-when: vor dem Entwurf von PHASE4_PLAN.md; vor P4-Start; vor dem ersten P4-Schritt
detail: L2
up: ../TRAKTION_OVERALL_PLAN.md
down:
  - ./PHASE3_PLAN.md
  - ./PHASE3_STEP8_PLAN.md
related: ../phase3/CLAUDE.md
updated: 2026-08-16
---
```

**Pflicht-Sektionen (verbatim aus diesem Sub-Plan übernehmen, in Step 9 ergänzen):**

1. **"Ergebnis auf einen Blick"** — Tests grün (Anzahl aus Sub-Step 8.2), Regel 2 intakt, Z5-Tautologie-Schutz intakt, M3-Befund dokumentiert.
2. **"Modell-Gap"** — die drei Befunde aus M3 (siehe unten).
3. **"Erreichte Ziele"** — Z5 grün im Kernelbereich, Z11 grün (Soll/Ist + Bottleneck-Top-3).
4. **"§9 Anti-Pattern-Check"** — alle Punkte leer (analog zu P2-Handover §Verstöße).
5. **"Offene Entscheidungen für P4"** — T-D35-Revisions-Vorschlag als **Empfehlung**, nicht als Forderung; P4 entscheidet.
6. **"Rohdaten für trials.jsonl-Zeile"** — der im M3-Session-Block formulierte Text, **plus** die echte Test-Anzahl nach Step 8.

**Modell-Gap-Sektion (verbatim in PHASE3_HANDOVER.md übernehmen):**

```markdown
## Modell-Gap (M3-Befund, 2026-08-16)

**Befund.** Der Planer (T-D35, "pro Kante analytisch, kein Sub-Tick") divergiert **strukturell**
vom numerischen Simulator. Zwei Ursachen:

1. **Beschleunigungsphase nicht modelliert.** Der Simulator macht pro Substep
   semi-implizites Euler (`F=excessW/v`, `a=F/m`, `v+=a*dt`, `x+=v*dt`); der Zug startet bei
   `v=0` (oder `startSpeedMps`) und beschleunigt über hunderte Substeps. Der Planer projiziert
   **augenblickliches Gleichgewicht** pro Kante — `v` aus `Physics.requiredPowerW(v) == availablePower`,
   über die ganze Kante gehalten. Selbst bei `condition=1.0, gradient=0` ist der Simulator
   dadurch messbar langsamer als der Planer (Anfahrtsphase fehlt).

2. **Distanzabhängiger Spannungsabfall nicht modelliert.** Der Simulator fragt pro Substep
   `powerGrid.availableW(..., distanceOnEdge, ...)` mit `effectiveReach = maxReach × condition`
   — bei langen Kanten oder schlechtem Netz sinkt die Leistung über die Kantenlänge. Der Planer
   nutzt `availableW(..., 0.0, 1.0, 1.0) * condition` (distance = 0 durchgehend, condition als
   konstanter Multiplikator). Auf langen Kanten bei condition < 1.0 fehlt die Reach-Erschöpfung
   im Planer-Modell.

**Konsequenz für Z5:** Die 5%-Toleranz aus T-D42 ist **im Kernelbereich**
(`cond ∈ [0.7, 1.0]`, `grad ∈ [-0.03, 0.03]`, `power ≥ 200 kW`, `startSpeed ≤ 10 m/s`,
kurze Kanten) erreichbar — das ist die ehrliche Beschreibung des Bereichs, in dem die
Gleichgewichts-Approximation gut funktioniert. Außerhalb dieses Bereichs divergiert der
Planer **strukturell**. JUnit-Edge-Cases, die numerische 5%-Vergleiche waren
(`longEdge`, `gradient`, `startSpeed`), wurden in Sub-Step 8.1 recastet zu Monotonie-
und Plausibilitätstests.

**Ursache: T-D35.** Die Modellwahl ("kein Sub-Tick, analytisch pro Kante") ist gelockt
und in P3 nicht geändert. Sie ist keine Fehlentscheidung — sie war die einfachste
Approximation, die Z5 "im Trend" liefern konnte. Für volle 5%-Genauigkeit über den gesamten
Definitionsbereich bräuchte der Planer eine grobe numerische Integration pro Kante.

**Empfehlung für Folgephase (P3.5 oder zu Beginn von P4):** T-D35 erweitern zu
**"Planner darf eine grobe numerische Integration pro Kante durchführen (z.B. 10–20 Sub-Steps
mit dt im Sekunden-Bereich, nicht im Sub-Tick-Maßstab des Simulators). Die Integration ruft
weiterhin `Physics.requiredPowerW` auf (Regel 2) und ist deterministisch (Regel 8)."** Diese
Erweiterung ist API-kompatibel — `Planner.predict(...)` ändert seine Signatur nicht, die
Implementierung wird genauer. **Kein Eingriff in P3** — gelockte Entscheidung bleibt gelockt.

**Studien-Bedeutung (M1 §6):** Z5 ist das Kern-Orakel der Studie. Die Kernbereich-Einengung
ist kein "Scheitern" — sie ist ein ehrlicher Bericht dessen, was die in T-D35 gewählte
Modell-Auflösung leisten kann. Die M1-Auswertung in P6 muss diese Grenze benennen, nicht
verschleiern.
```

**Akzeptanzkriterien:**
- [ ] `PHASE3_HANDOVER.md` existiert mit Header-Card (≤15 Zeilen YAML), Datum `2026-08-16`.
- [ ] "Modell-Gap"-Sektion ist verbatim übernommen.
- [ ] Andere Sektionen sind als Platzhalter/Stub vorhanden (Status, Ziele, §9-Check, Rohdaten) — Step 9 füllt sie aus.
- [ ] `docs/INDEX.md` hat einen One-Liner für `PHASE3_HANDOVER.md` (analog zu P0/P1/P2).
- [ ] `docs/INDEX.md` `updated:` Datumsstempel auf 2026-08-16.
- [ ] **Frage an Operator:** "Sub-Step 8.3 erledigt — soll ich mit Sub-Step 8.4 (Step 9 Done-When) weitermachen?"

**Commit-Message:** `docs: create PHASE3_HANDOVER skeleton with Modell-Gap section (T-D53)`

**Zielzuordnung:** Z5 (Handover-Dokumentation).
**Kategorie:** A.

---

### Sub-Step 8.4 — Step 9 Done-When + Phasen-Abschluss

> Aus `PHASE3_PLAN.md` Step 9 (Zeilen 574–632) **unverändert** übernehmen. Der Build-Agent führt
> alle §9-Checks, den Determinismus-Bestätigungstest, die `train-core`-Dependency-Prüfung und
> das finale Build-Log + Phasen-Abschluss durch.

**Dateien:** (aus PHASE3_PLAN.md Step 9)
- `train-core/` (Tests bestätigt, keine neuen Dateien)
- `phase3/CLAUDE.md` (Build-Log finalisieren, Phasen-Abschluss-Block)
- `CLAUDE.md` (Root, Phasenstatus: P3 ✅, P4 �)
- `ROADMAP.md`, `README.md` (P3 = ✅)
- `docs/plans/PHASE3_HANDOVER.md` (Sektionen aus 8.3 vervollständigen mit echten Zahlen)
- `phase3/SESSIONS_ARCHIVE.md` (M3-Block rotieren, wenn zukünftige Sessions ihn ersetzen)
- `docs/INDEX.md` (`updated:` anpassen)

**Aktionen:**

1. Alle §9-Anti-Pattern-Checks aus PHASE3_PLAN.md Step 9 durchgehen (grep, Verifikation).
2. `gradle :train-core:test` final grün.
3. `PHASE3_HANDOVER.md` Sektionen aus 8.3-Skelett auffüllen (Status, Ziele, §9-Check, Rohdaten).
4. `phase3/CLAUDE.md` Build-Log: Step 8 + 9 als ✅, Commits referenziert.
5. Root-`CLAUDE.md` Phasenstatus: P3 ✅, P4 ⏳.
6. Rohdaten-Text für `m1/trials.jsonl` an Nikinger übergeben (nicht selbst schreiben).
7. **Frage an Operator:** "P3 abgeschlossen — soll ich den P3-Handover-Block in `phase3/CLAUDE.md` final schreiben?"

**Akzeptanzkriterien:** (aus PHASE3_PLAN.md Step 9, 11 Punkte — alle erfüllt)
- `Physics.requiredPowerW` genau einmal (Regel 2 — verifiziert per grep, **P3-Watchpunkt**)
- `Planner.predict` ruft `Simulator.runRoute` NICHT auf (T-D47 — verifiziert per grep, **P3-Watchpunkt**)
- `PHASE3_HANDOVER.md` vollständig, mit Rohdaten für `m1/trials.jsonl`
- `docs/INDEX.md` One-Liner für `PHASE3_HANDOVER.md` (bereits in 8.3)

**Commit-Message:** `docs: close P3 (Z5 + Z11 green, Planer asymmetrisch zu Simulator, Regel 2 intakt, Modell-Gap dokumentiert)`

**Zielzuordnung:** Z5, Z11, Regel 2, Regel 4 (via Z7 unverändert), §9, T-D49.
**Kategorie:** A.

---

## Done-When (P3-Step-8-Sub-Plan)

- [ ] Sub-Step 8.1: `longEdge`-Test entfernt, `gradient`/`startSpeed` zu Monotonie recastet.
- [ ] Sub-Step 8.2: `Z5_KERNELBEREICH` als Konstante + Klassen-Javadoc im Test.
- [ ] Sub-Step 8.3: `PHASE3_HANDOVER.md` Skelett mit "Modell-Gap"-Sektion.
- [ ] Sub-Step 8.4: Step 9 Done-When komplett, P3 abgeschlossen.
- [ ] **Z5-Kerntest (jqwik) ≤ 5 % Abweichung im Kernelbereich** (T-D42 präzisiert via T-D51).
- [ ] **JUnit-Edge-Cases strukturell konsistent** (Monotonie statt 5%-Vergleich — T-D52).
- [ ] **Modell-Gap im Handover dokumentiert** (T-D53).
- [ ] **T-D35 nicht geändert** (gelockt, T-D54 nur als Empfehlung für Folgephase).
- [ ] `Physics.requiredPowerW` genau einmal (Regel 2 — P3-Watchpunkt bestätigt).
- [ ] `Planner.predict` ruft `Simulator.runRoute` NICHT auf (T-D47 — P3-Watchpunkt bestätigt).
- [ ] Determinismus bestätigt (Regel 8, T-D45).

**P3 ist abgeschlossen, wenn alle Sub-Steps committed und Done-When erfüllt sind.** Nächster
Schritt: P4 (`train-mc`: erste spielbare Version, Z9–Z11, Kategorie B) in neuer Session.

---

## T-D35-Revision für Folgephase P3.5/P4 (Vorschlag, nicht P3-Implementierung)

> **Status: Vorschlag.** Wird im PHASE3_HANDOVER.md "Modell-Gap"-Sektion verbatim übernommen.
> Implementierung in P3.5 oder zu Beginn von P4, **nicht** in P3.

**Vorgeschlagene T-D35-Erweiterung (zu `PHASE3_PLAN.md` Zeile 45):**

> **T-D35 (rev)** | Planer-Auflösung | **Pro Kante analytisch ODER grobe numerische Integration
> pro Kante.** Der Planer darf eine grobe numerische Integration über die Kantenlänge
> durchführen (z.B. 10–20 Sub-Steps mit `dt` im Sekunden-Bereich, nicht im Sub-Tick-Maßstab
> des Simulators). Die Integration ruft weiterhin `Physics.requiredPowerW` auf (Regel 2) und
> ist deterministisch (Regel 8). Asymmetrie zum Simulator bleibt: pro Kante gröber, **kein
> Verkehr**, **kein Override**, **kein Sub-Tick im Sinne von T-D13**. Z5-Akzeptanz erweitert
> sich auf den vollen Definitionsbereich (`cond ∈ [0, 1]`, `grad ∈ [-0.05, 0.05]`,
> `startSpeed ∈ [0, 50]`).

**Implementierungs-Skizze (für die Folgephase, nicht P3):**

```java
// Im Planner, ersetzt/erweitert computeRouteTravelTime():
private static double computeEdgeTravelTime(
        Edge edge, Consist consist, PowerGrid netState,
        double maxPowerW, double currentSpeedMps) {
    final int SUB_STEPS_PER_EDGE = 20;       // konstant, grob
    final double dt = 1.0;                   // 1s pro Sub-Step, kein Sub-Tick
    double v = currentSpeedMps;
    double x = 0.0;
    while (x < edge.lengthMeters()) {
        double reqW = Physics.requiredPowerW(consist, v, edge.gradient());
        double availW = netState.availableW(maxPowerW, x, edge.effectiveCondition(), dt);
        // Kraft aus Überschuss, semi-implizites Euler (wie Simulator, nur grober)
        double excessW = availW - reqW;
        double effectiveV = Math.max(v, EPS_V);  // EPS_V = 0.01
        double a = (excessW / effectiveV) / consist.totalMassKg();
        v = Math.max(0.0, v + a * dt);
        x += v * dt;
    }
    return x / currentSpeedMps;  // Näherung; exakter ist Akkumulator
}
```

**Risiko / Trade-off:**
- Performance: 20 Sub-Steps pro Kante × N Kanten ≈ 200 Iterationen für eine 10-Kanten-Route.
  Im Millisekunden-Bereich — kein Problem für die UI-Prognose.
- Regel 2 bleibt intakt: nur `Physics.requiredPowerW` wird gerufen.
- Determinismus bleibt intakt: keine Random-Quelle, kein Wall-Clock.

---

## Rohdaten für die trials.jsonl-Zeile — an Nikinger, nicht in die Datei

> (Bereits im M3-Session-Block von `phase3/CLAUDE.md` entworfen; hier nochmal verbatim für die
> Build-Agent-Session, damit der Agent die endgültige Fassung am Ende von Step 9.4 an Nikinger
> übergeben kann.)

**Felder:**
- `phase`: P3
- `ziel`: Planer-Durchstich (Z5, Z11-Kern-Anteil) + Regel-2-Watchpunkt + Z5-Tautologie-Schutz +
  Multi-Edge-Regel-5-Schließung (P2-Handover E) + **Modell-Gap-Dokumentation (M3-Befund)**
- `kategorie`: A
- `iterationen`: Commits Step 0.1–9 inkl. der drei 8.x-Sub-Steps
- `tests_gruen`: alle P1+P2-Tests (208) + neue P3-Tests (RouteForecastTest, BottleneckTest,
  PlannerTest, MultiEdgeWearTest, SimulatorRunRouteTest, Z5PropertyTest reduziert um 1 JUnit)
- `regel2_verstoss`: **nein erwartet** — `Physics.requiredPowerW` einmalig
- `z5_tautologie`: **nein erwartet** — Planer ruft `Simulator.runRoute` nicht auf
- `notiz`: Z5 grün im **Kernelbereich** (`cond ∈ [0.7, 1.0]`, `grad ∈ [-0.03, 0.03]`,
  `power ≥ 200 kW`, `startSpeed ≤ 10 m/s`). JUnit-Edge-Cases strukturell recastet zu Monotonie-
  und Plausibilitätstests (Sub-Step 8.1). Zwei strukturelle Planer-Lücken im Handover
  dokumentiert: Beschleunigungsphase und distanzabhängiger Spannungsabfall. T-D35 bleibt
  gelockt; T-D35-Revision als Empfehlung für P3.5/P4 dokumentiert. Determinismus bestätigt
  (T-D45, T-D51). Atomar-arbeiten-Regel (T-D49) befolgt — keine Auto-Stops nach 20-30 Calls.

---

## Verweise

| Was | Pfad | Warum |
|---|---|---|
| Overall Plan (Wahrheit) | `TRAKTION_OVERALL_PLAN.md` | §2 Locks, §3 Hard Rules, §4 Ziele Z5+Z11, §5/P3, §9 Anti-Patterns |
| Preregistration (FROZEN) | `M1_PREREGISTRATION.md` | §2 Konstanten, §3 Metriken, §4 Confounds. Nie editieren. |
| P3-Hauptplan | `docs/plans/PHASE3_PLAN.md` | Vollständige Schritt-Sequenz Steps 0–9, T-D35–T-D50 — T-D51–T-D54 in diesem Sub-Plan präzisiert |
| P2-Handover (Vorphase) | `docs/plans/PHASE2_HANDOVER.md` | P2-Abschluss, offene P3-Entscheidungen, Watchpunkte |
| M3-Konsultations-Block | `phase3/CLAUDE.md` Session-stopped | Diagnose + Empfehlung A, 2026-08-16 |
| Phasen-Kopf (vom Build-Agent) | `phase3/CLAUDE.md` | Build-Log + Phasen-Abschluss-Block (Step 1 erstellt) |
| Code-Wahrheit (P3-Stand) | `train-core/src/main/java/de/traktion/traincore/` | 21 Typen — P3 erweitert (RouteForecast, Bottleneck, BottleneckArt, Planner), dupliziert nicht |

---

## Session stopped

> Dieser Sub-Plan ist das Konzept/Plan-Dokument für Step 8 nach dem M3-Befund. Der
> `## Session stopped`-Block für die Sub-Plan-Erstellung (M3 Plan-Agent) lebt in
> `phase3/CLAUDE.md`. Diese Datei enthält keinen eigenen Session-stopped-Block — sie ist
> ausführungsreif und wartet auf die Build-Agent-Session (M2.7).
