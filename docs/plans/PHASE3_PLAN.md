---
status: plan (ausführungsreif für P3)
purpose: Phasenplan für P3 — Planer (Z5 — Kern-Orakel). Architektur-Entscheidungen, Schritt-Sequenz, Testlisten, Akzeptanzkriterien. `Planner.predict(route, consist, netState, …) → Optional<RouteForecast>`, asymmetrisch zum Simulator (grobe Auflösung, ein Zug, kein Verkehr), aber **dieselbe** `Physics.requiredPowerW` (Regel 2). Engpass-Erkennung, Soll/Ist-Vergleich (Z11-Kern-Anteil), Z5 property-based.
read-when: Ausführung von P3; vor jedem P3-Schritt; Referenz für §5/P3 Watchpunkte (Regel-2-Verstoß, Z5-Tautologie)
detail: L2
up: ../TRAKTION_OVERALL_PLAN.md
down:
  - ../docs/CONVENTIONS.md         # Logging, Testmatrix, Root-Layout
  - ./PHASE2_HANDOVER.md           # Vorphase — P2-Abschluss, Watchpunkte, offene P3-Fragen
related: ../phase3/CLAUDE.md        # Phasen-Kopf (vom ausführenden Agenten als erste Aktion erstellt)
supersedes: (kein Vorläufer)
updated: 2026-08-15
---

# Phase 3 — Planer

> **Kategorie A** (Plan §5/P3, §7). Der interessanteste Punkt der Studie (Plan §5/P3, §9 Watch).
> Klassische Informatik mit einem scharfen Architektur-Test: der Planer muss dieselbe Physik
> rufen wie der Simulator (Regel 2), darf aber den Simulator nicht rufen (T-D14, Z5-Tautologie).
>
> **Wahrheit:** `TRAKTION_OVERALL_PLAN.md` §5/P3, §3 (Hard Rules), §3.2 (Ports), §4 (Ziele Z5, Z11),
> §9 (Anti-Patterns).
> **Vorphase:** `docs/plans/PHASE2_HANDOVER.md` (P2-Abschluss, offene Entscheidungen für P3,
> Watchpunkte).
>
> **Detailgrad:** Dieser Plan spezifiziert Typnamen, Dateipfade, Zielzuordnung (Z\<x\>), Kategorie
> (A/B), Testliste, Akzeptanzkriterien. Er spezifiziert **NICHT** Methodensignaturen, Funktionskörper,
> Zeilenanker oder Algorithmen (M1_PREREGISTRATION §2). Die Übersetzung von Akzeptanzkriterium zu
> Implementierung IST die gemessene Fähigkeit (Kategorie A).
>
> **Regime-Wechsel ab P3 (Nikinger, 2026-08-15):** Statt "Handover nach ~20-30 Tool-Calls via
> `## Session stopped`" gilt jetzt **"Atomar arbeiten — nach jedem Step nachfragen"**. Hintergrund:
> seit Wechsel auf den Minimax-Token-Plan (Commit `682a2a1`, Modell-Rollen-Trennung M2.7/M3) gibt
> es keine externe RPM-Begrenzung mehr wie unter GLM 5.2 / NVIDIA NIM. Der Operator beendet die
> Session explizit pro Schritt, nicht durch internes Call-Limit. Der `## Session stopped`-Block
> bleibt als Phasen-Abschluss-Block in `phase3/CLAUDE.md` (analog zu P2), aber es gibt **keinen**
> tool-call-basierten Auto-Stop mehr. Siehe T-D49.

---

## Architektur-Entscheidungen dieser Phase

| # | Thema | Lock | Status |
|---|---|---|---|
| **T-D35** | Planer-Auflösung | **Pro Kante, analytisch** (T-D14: "grob"). Kein Sub-Tick. Der Planer integriert über die Kantenlänge mit konstantem Reibungs-/Steigungs-/Luft-Modell und dem `PowerGrid`-Angebot; pro Kante wird eine Fahrzeit berechnet und über die Route summiert. Granularität pro Kante reicht für Z5 (≤ 5% Abweichung gegen den numerischen Simulator) — der Simulator integriert feiner (Sub-Tick, T-D13), der Planer integriert gröber (pro Kante). Dieselbe Physikfunktion (Regel 2), zwei Auflösungen. | gelockt (Nikinger, 2026-08-15) |
| **T-D36** | Planer ignoriert Verkehr | **Asymmetrie zum Simulator** (T-D14): keine Reservierungen, keine `BlockSection`, keine Token-Interaktion. Der Planer nimmt **einen** Zug an, nicht mehrere. Override / Fahrplan kommen in P5 — in P3 nur als Vertragsgrenze dokumentiert ("fremder Verkehr → nicht anwendbar"), nicht erzwungen. | gelockt (Nikinger, 2026-08-15) |
| **T-D37** | `Planner`-Klasse | **Statische Utility** wie `Physics` und `Wear` — `predict(...)` als statische Methode, keine Instanzen. Aufruf: `Planner.predict(route, consist, netState, …) → Optional<RouteForecast>`. Regel 2: ruft `Physics.requiredPowerW` auf, dupliziert die Formel nicht. | gelockt (Nikinger, 2026-08-15) |
| **T-D38** | `RouteForecast`-Record | Daten-Record, unveränderlich. Felder: `sollFahrzeitSekunden` (Planer-Prognose mit `condition = 1.0` überall) · `istFahrzeitSekunden` (Planer-Prognose mit aktuellem `netState`) · `deltaProzent` (= `(ist − soll) / soll × 100`) · `bottlenecks` (Top-3-Liste von `Bottleneck`, nach Beitrag sortiert). | gelockt (Nikinger, 2026-08-15) |
| **T-D39** | `Bottleneck`-Record | Daten-Record. Felder: `edge` (`Edge`-Referenz) · `art` (Enum `BottleneckArt ∈ {SPANNUNG, STEIGUNG, KOMBI}`) · `beitragSekunden` (geschätzter Beitrag zur Fahrzeit-Verlängerung). | gelockt (Nikinger, 2026-08-15) |
| **T-D40** | Rückgabetyp `Optional<RouteForecast>` | **Vertragsgrenze als leere Optional** (statt Exception): Gründe sind (a) `effectiveCondition() == 0` auf mindestens einer Kante der Route (P2-Handover A.3) · (b) leere Route · (c) `Consist` mit `totalMassKg() == 0` · (d) `maxPowerW <= 0`. Keine Exception im Normalbetrieb — der Property-Test mit 1000+ Fällen würde sonst Exception-Spam erzeugen. | gelockt (Nikinger, 2026-08-15) |
| **T-D41** | Harter Fehler vs. "nicht anwendbar" | **`IllegalArgumentException` bei**: (e) Route nicht zusammenhängend (`route[i].to() != route[i+1].from()`) · `null`-Argumente. Das sind **Invariante-Verstöße**, nicht "nicht anwendbar"-Fälle. Optional.empty nur für die in T-D40 gelisteten vier Gründe. | gelockt (Nikinger, 2026-08-15) |
| **T-D42** | Z5-Property-Test | **Mit jqwik 1.9.0** (T-D20, in P1 aktiviert und in P1+P2 verifiziert). Generator: zufällige **Routen in einem festen kleinen Test-Graphen** (3–7 Kanten, A→B), zufällige `condition ∈ [0, 1]` auf den Edges, zufällige `gradient ∈ [-0.05, 0.05]`, zufällige `maxPowerW` im sinnvollen Bereich. Akzeptanz: `\|planner.predict(r).istFahrzeit − simulator.runRoute(r)\| / simulator.runRoute(r) < 0.05` für **mindestens 1000 generierte Fälle**. Fälle mit `condition == 0` werden aussortiert (Empty) und nicht in die Z5-Akzeptanz gezählt. | gelockt (Nikinger, 2026-08-15) |
| **T-D43** | Bottleneck-Beitrag-Berechnung | Agent entscheidet (Detailgrad-Konstante, M1_PREREGISTRATION §2). Akzeptanz: Bottleneck-Liste ist nach `beitragSekunden` **absteigend** sortiert, maximal **3 Einträge**, jede Bottleneck hat eine plausible Größenordnung (`beitragSekunden > 0` wenn die auslösende Bedingung verletzt ist; `beitragSekunden == 0` ist erlaubt, wenn der Beitrag null ist, wird der Bottleneck nicht aufgenommen). | gelockt (Nikinger, 2026-08-15) |
| **T-D44** | Planer nutzt Physikfunktion direkt | **Regel 2:** der Planer ruft `Physics.requiredPowerW(consist, speedMps, gradient)` auf, **nicht** eine eigene Variante. Eine zweite Implementierung derselben Formel ist ein **Regel-2-Verstoß** (§9) — protokollieren, nicht stillschweigend korrigieren (Plan §5/P3 Watch). | gelockt (Nikinger, 2026-08-15) |
| **T-D45** | Determinismus des Planers | **Deterministisch per Konstruktion** — der Planer ist analytisch, hat keinen `Random`. Optional: ein gesäter RNG, falls eine Modellkomponente Stochastik braucht (analog zu `Simulator.rng`, P2-Handover A.2). Akzeptanz: zwei `Planner.predict`-Aufrufe mit gleichen Inputs → **identische** `RouteForecast` (Toleranz wie P1/P2: 1e-9 auf `double`-Feldern). | gelockt (Nikinger, 2026-08-15) |
| **T-D46** | Kein Interface, keine Abstraktion | **Regel 3:** der Planer hat eine Aufgabe mit einer Implementierung. Keine `BottleneckDetector`- oder `RouteAnalyzer`-Abstraktion in P3. Wenn P5 (Override, Multi-Zug) eine zweite Prognose-Variante braucht, wird das **dann** abstrahiert, nicht vorgreifend. | gelockt (Nikinger, 2026-08-15) |
| **T-D47** | `Simulator.runRoute(...)` als Test-Harness | Neue Convenience-Methode auf `Simulator` für den **Z5-Vergleich**: nimmt `List<Edge>`-Route + `Consist` + `maxPowerW` + `startSpeedMps`, fährt die Route ab (intern: Token auf `route[0]`, `moveToEdge(route[i+1])` bei `reachedEndOfEdge`), gibt `totalSeconds` zurück. **Keine Verletzung von T-D14** — der Planer ruft sie **NICHT** auf. Sie ist ausschließlich Test-Harness für den Z5-Property-Test. Endlich genutzt: `Token.moveToEdge(Edge)` (P2-Handover A.5). | gelockt (Nikinger, 2026-08-15) |
| **T-D48** | Multi-Edge-Regel-5-Invariante schließen | **Eigener Step in P3** (P2-Handover E, Empfehlung). Test: zwei (oder mehr) Kanten, Token befährt Kante A, Kante B behält `condition == 1.0` über die gesamte Sim-Dauer. Property-Form (jqwik): für eine Sequenz von befahrenen/unbefahrenen Kanten gilt — **nur die aktuelle Edge des Tokens** degradiert; alle anderen bleiben auf Startwert. | gelockt (Nikinger, 2026-08-15) |
| **T-D49** | Regime-Wechsel zur "Atomar arbeiten"-Regel | **Pro Step:** ausführen, `gradle :train-core:test` grün, dann **dem Operator die Frage stellen, ob weitergearbeitet wird**. Kein automatisches `## Session stopped` nach ~20-30 Tool-Calls mehr (gilt seit Commit `682a2a1`, 2026-08-15). Hintergrund: Minimax-Token-Plan hat keine RPM-Begrenzung. Der `## Session stopped`-Block bleibt in `phase3/CLAUDE.md` als Phasen-Abschluss-Block (analog zu P2), wird aber **nicht** mehr tool-call-basiert rotiert. | festgeschrieben (Nikinger, 2026-08-15) |
| **T-D50** | P3 hat **keine** neuen `[VERIFY]`-Marken | P3 ist reines Java in `train-core` (kein 26.2-API-Kontakt). Die einzigen `[VERIFY]`-Erben aus P1 (`jqwik`/`Gradle 9.5.1` — in P1 aufgelöst; Fabric-Logging-Konvention, `SavedData` — beide P4) sind P3-irrelevant. Eine neue `[VERIFY]`-Marke ist nur zu setzen, wenn der Agent während der Ausführung trotzdem auf eine ungeklärte API-Frage stößt — was bei reinem `train-core`-Code unwahrscheinlich ist. | festgeschrieben (Nikinger, 2026-08-15) |

### ⚠ Watchpunkte, die nicht stillschweigend korrigiert werden dürfen

> Plan §5/P3: "Ein Agent wird versuchen, entweder die Formel zu duplizieren (Regel-2-Verstoß) **oder**
> den Planer den Simulator aufrufen zu lassen (tautologisches Z5). Beides **protokollieren**, nicht
> stillschweigend wegkorrigieren. Genau hier zeigt sich, ob ein Modell Architektur versteht oder
> Code produziert."

- **Regel-2-Verstoß (T-D44, P3-Watchpunkt #1):** Hat der Planer eine eigene `requiredPowerW`-Variante
  ("für die schnelle Approximation")? **Protokollieren** — das ist ein direkter §9-Verstoß.
- **Z5-Tautologie (T-D47, P3-Watchpunkt #2):** Ruft der Planer `Simulator.runRoute(...)` (oder eine
  ähnliche Simulator-Methode) für seine eigene Prognose auf? **Protokollieren** — dann ist Z5
  trivial `f(x) == f(x)` und beweist nichts. Der `runRoute`-Aufruf ist ausschließlich im Test-Harness
  erlaubt.
- **Determinismus (Regel 8, T-D45):** Hat der Planer ungesäten Zufall (`Math.random`, `new Random()`
  ohne Seed)? Wand-Clock (`System.currentTimeMillis`)? Toleranz wie P1/P2: 1e-9.
- **Atomar arbeiten (T-D49):** Der Agent fragt **nach jedem Step**, ob weitergearbeitet wird.
  Der Operator entscheidet. Kein automatischer Weitermachen, kein Auto-Stop nach 20-30 Calls.
- **Regel 5 (Verschleiß bestraft Nutzung, nicht Existenz, P2-Handover E):** Der Multi-Edge-Test in
  Step 6 ist die explizite Testabdeckung — der Planer iteriert über Routen mit mehreren Kanten.

---

## Schritt-Sequenz

> Jeder Schritt ist einzeln committbar. Format: `<scope>: <imperative>` (Plan §11).
> Commit ⇒ Note-Update: Statuszeile + `## Session stopped` in `phase3/CLAUDE.md` im selben Commit.
> TDD in `train-core`: ein Subtask ist nicht fertig, bevor `gradle :train-core:test` grün ist.
> **Regime-Wechsel (T-D49):** Nach jedem fertigen Step fragt der Agent den Operator, ob der nächste
> Step begonnen werden soll.

---

### Step 0 — Altlasten (Namensdrift, tote Verweise, Reste aus Vorphasen)

> Aus `docs/plans/PHASE2_HANDOVER.md` (P2 → P3) und frischem `git status`-Scan.

#### Step 0.1 — Drift-Commits prüfen + ggf. synchen

**Stand zum Plan-Entwurf (2026-08-15):** `git status` zeigt clean working tree, HEAD = `682a2a1 docs:
pin model-role separation`. Es sind **keine** uncommitted Drift-Dateien zu erwarten.

**Was der Agent tut:**
1. `git status` — bestätigen, dass working tree clean ist (oder Drift-Dateien listen).
2. `git log --oneline -10` — die letzten Commits sehen (HEAD sollte `682a2a1` oder neuer sein).
3. **Falls Drift gefunden wird:** Root-`CLAUDE.md` (`updated:`), `ARCHITECTURE.md` (`updated:`),
   `README.md` (Phasenstatus-Tabelle, Versions-Pinning), `phase2/CLAUDE.md` (`updated:`) und
   `phase2/SESSIONS_ARCHIVE.md` (`updated:`) — Inhalt aus P2-Handover übernehmen, **nicht inhaltlich
   mit-korrigieren**. Commit-Message: `docs: sync P2-abschluss drift (CLAUDE/ARCHITECTURE/README/phase2/*)`.
4. **Falls kein Drift:** Step 0.1 ist leer, Akzeptanz ist `git status` clean — dokumentiert als
   "kein Drift", kein Commit nötig.

**Typnamen:** keine (Doku-Drift).
**Testliste:** keine.
**Akzeptanzkriterien:**
- [ ] `git status` zeigt working tree clean (oder Drift-Dateien sind committed in einem Schritt)
- [ ] Root-`CLAUDE.md` zeigt P2 = ✅, P3 = 🔄 (wird in Step 1 finalisiert)
- [ ] Commit-Message (falls Drift): `docs: sync P2-abschluss drift (CLAUDE/ARCHITECTURE/README/phase2/*)`
- [ ] **Frage an Operator:** "Step 0.1 erledigt — soll ich mit Step 0b weitermachen?"

**Zielzuordnung:** — (Infrastruktur).
**Kategorie:** — (Infrastruktur).

#### Step 0.2 — P2-Trials verifizieren (Operator-Aktion, Agent liefert Verifikation)

> P2-Handover Abschnitt "Rohdaten für die trials.jsonl-Zeile(n)" ist im Commit `882b1c1 m1: backfill P2 trial`
> als operator-confirmed entry eingetragen. Verifikation: `wc -l m1/trials.jsonl` sollte 6 Zeilen zeigen
> (P0.1, P0.2, P0.3, P0.4, P1, P2 — gemäß P0-Handover §Rohdaten und P2-Handover).

**Was der Agent tut:**
1. `wc -l m1/trials.jsonl` — bestätigen, dass 6 Zeilen vorhanden sind.
2. **Falls P2-Zeile fehlt:** Agent liefert die P2-Rohdaten (aus P2-Handover "Rohdaten für die trials.jsonl-Zeile(n)")
   nochmals als Text an den Operator; Operator trägt ein und committed. **Agent schreibt NICHT in `m1/trials.jsonl`**
   (`edit: m1/** → deny/ask`).
3. **Falls vorhanden:** Step 0.2 ist leer, dokumentiert als "P2-Trial eingetragen, kein Eingriff nötig".

**Typnamen:** keine (Messdaten).
**Testliste:** keine.
**Akzeptanzkriterien:**
- [ ] `m1/trials.jsonl` enthält 6 Zeilen (oder weniger, falls Nikinger aggregiert entschieden hat — kurz nachfragen)
- [ ] **Frage an Operator:** "Step 0.2 erledigt — soll ich mit Step 0b weitermachen?"

**Zielzuordnung:** — (Messung, nicht A/B).
**Kategorie:** — (Messinstrument).

---

### Step 0b — Doc-Drift

> Code gegen .md verifizieren. Code ist Wahrheit. Doku fixen, Historie nie umschreiben.

**Was zu prüfen ist:**

| Datei | Erwartung | Aktuell? |
|---|---|---|
| Root-`CLAUDE.md` Phasenstatus | P2 ✅, P3 ⏳ | prüfen ([VERIFY] — kann seit `882b1c1`/`682a2a1` aktualisiert sein) |
| `docs/INDEX.md` | listet `PHASE2_HANDOVER.md`, listet **nicht** `PHASE3_PLAN.md` (kommt in Step 1) | prüfen |
| `phase2/CLAUDE.md` | P2 abgeschlossen, Session-stopped-Block vorhanden | prüfen |
| `phase2/README.md` | P2 abgeschlossen, "Nächste Phase: P3" | prüfen (P2-Handover meldete Drift — in `17ade42`/`f4760e2` nachgezogen, [VERIFY]) |
| `ARCHITECTURE.md` / `ROADMAP.md` | Stubs, zeigen auf Overall Plan; P2 ✅, P3 ⏳ | prüfen |
| `M1_PREREGISTRATION.md` | FROZEN — nicht berührt | **grep nach Commit-Datum im HEAD-Tree** |
| `phase2/CLAUDE.md` P2 Steps | alle [x] | prüfen ([VERIFY] — Build-Log zeigt Step 9 ✅, P2-Handover meldete Tippfehler/Duplikation in `f4760e2` nachgezogen) |
| `phase2/CLAUDE.md` Build-Log Tabelle | Step 9 als ✅, Commits referenziert | prüfen |

**Akzeptanz:**
- Wenn Drift gefunden: kurzer `docs:`-Commit mit der Korrektur, historische Inhalte nie umgeschrieben.
- Wenn kein Drift: Step 0b ist leer, dokumentiert als "kein Drift gefunden".
- **Frage an Operator:** "Step 0b erledigt — soll ich mit Step 1 weitermachen?"

**Zielzuordnung:** — (Infrastruktur).
**Kategorie:** — (Infrastruktur).

---

### Step 1 — `phase3/CLAUDE.md` als allererste Phasen-Aktion

> Plan §11: "CLAUDE.md als erste Aktion jeder Phase — nie nachgelagert."
> Plan §9 Anti-Pattern: "Eine Phase ohne CLAUDE.md als allererste Aktion."
> DOC_LAYERS_CONVENTION "Neue Phase beginnen": `phase3/` anlegen, `CLAUDE.md` schreiben.

**Dateien:**
- `phase3/CLAUDE.md` — Header-Card + Build-Log (leer bis auf Step 0) + Hinweis auf den Phasen-Abschluss-Block
- `phase3/SESSIONS_ARCHIVE.md` — leer anlegen (oder Hinweis "leer bis zur ersten Rotation")
- `phase3/README.md` — die menschliche Oberfläche der Phase (Pflicht ab P1, Pflicht in P3)
- `docs/INDEX.md` — One-Liner für `phase3/CLAUDE.md`, `SESSIONS_ARCHIVE.md`, `README.md`,
  sowie für `docs/plans/PHASE3_PLAN.md` (in diesem Schritt erstellt — der Plan existiert ab jetzt)
- Root-`CLAUDE.md` — Phasenstatus-Tabelle: P3-Zeile von ⏳ auf 🔄 aktualisieren, down-Link auf
  `phase3/CLAUDE.md` und `docs/plans/PHASE3_PLAN.md`
- `ROADMAP.md` — P3-Zeile auf 🔄 aktualisieren
- `README.md` — P3-Zeile auf 🔄 aktualisieren

**Typnamen:** keine (docs-only).
**Testliste:** keine.
**Akzeptanzkriterien:**
- [ ] `phase3/CLAUDE.md` existiert mit Header-Card (≤15 Zeilen YAML)
- [ ] `phase3/SESSIONS_ARCHIVE.md` existiert (leer oder mit Hinweis "leer bis zur ersten Rotation")
- [ ] `phase3/README.md` existiert mit Header-Card und Kurzzusammenfassung von P3 (analog zu
      `phase2/README.md` — verweist auf `TRAKTION_OVERALL_PLAN.md` §5/P3, listet die Komponenten)
- [ ] `docs/INDEX.md` hat One-Liner für `phase3/CLAUDE.md`, `SESSIONS_ARCHIVE.md`, `README.md`,
      und für `docs/plans/PHASE3_PLAN.md`
- [ ] Root-`CLAUDE.md` Phasenstatus-Tabelle: P3 = 🔄 aktiv, down-Link auf `phase3/CLAUDE.md`
      und `docs/plans/PHASE3_PLAN.md`
- [ ] Alle im selben Commit
- [ ] Commit-Message: `docs: create phase3/CLAUDE.md (P3 start)`
- [ ] **Frage an Operator:** "Step 1 erledigt — soll ich mit Step 2 weitermachen?"

**Zielzuordnung:** — (Infrastruktur).
**Kategorie:** — (Infrastruktur).

---

### Step 2 — `RouteForecast` und `Bottleneck` Records (Z11-Kern-Datenstrukturen)

> T-D38, T-D39: reine Daten-Records. Unveränderlich. Bevor irgendetwas prognostiziert wird, müssen
> die Ergebnis-Typen stehen. `BottleneckArt` ist ein Enum mit drei Werten (T-D39). Diese Records
> leben in `train-core/src/main/java/de/traktion/traincore/` (neue Dateien).

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/RouteForecast.java` — `record RouteForecast`
  (T-D38, Felder siehe oben)
- `train-core/src/main/java/de/traktion/traincore/Bottleneck.java` — `record Bottleneck` (T-D39)
- `train-core/src/main/java/de/traktion/traincore/BottleneckArt.java` — `enum BottleneckArt {SPANNUNG, STEIGUNG, KOMBI}`
- `train-core/src/test/java/de/traktion/traincore/RouteForecastTest.java` — Tests
- `train-core/src/test/java/de/traktion/traincore/BottleneckTest.java` — Tests

**Typnamen:** `RouteForecast` (Record), `Bottleneck` (Record), `BottleneckArt` (Enum).

**Testliste (TDD, T-D38 + T-D39 + T-D40-Vorbereitung):**
- `RouteForecast`-Record: Konstruktor mit gültigen Werten erzeugt das Objekt (Felder erreichbar)
- `RouteForecast`-Invariante: `sollFahrzeitSekunden >= 0`, `istFahrzeitSekunden >= 0`, `bottlenecks`
  ist **unveränderlich** (`List.copyOf(...)` im kompakten Constructor, Regel 8 / defensive copy)
- `RouteForecast`-Invariante: `deltaProzent` darf negativ sein (= Ist < Soll, der Planer ist
  pessimistisch und schätzt die Soll-Fahrzeit ohne Verschleiß; wenn der Verschleiß den Zug
  langsamer macht, ist Ist > Soll, delta positiv)
- `Bottleneck`-Record: Konstruktor, Felder erreichbar, `art` ist nicht null, `edge` ist nicht null
- `BottleneckArt`: Enum hat genau drei Werte (`SPANNUNG`, `STEIGUNG`, `KOMBI`)
- `equals`/`hashCode` auf Records (automatisch — Test verifiziert nur, dass das Verhalten
  konsistent ist)

**Akzeptanzkriterien:**
- [ ] `RouteForecast` ist `public record`, Felder exakt wie in T-D38 (keine zusätzlichen Felder,
      keine fehlenden)
- [ ] `Bottleneck` ist `public record`, Felder exakt wie in T-D39
- [ ] `BottleneckArt` ist `public enum` mit drei Werten
- [ ] Beide Records machen defensive Kopie der `bottlenecks`-Liste (Regel 8 — keine
      geteilte Mutable-Liste nach außen)
- [ ] Konstruktor-Invariante: `null`-Argumente werfen `IllegalArgumentException` (analog zu P1/P2-Konvention)
- [ ] Kein `net.minecraft.*`-Import, kein NBT, kein `ItemStack` (Anti-Pattern-Check)
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: add RouteForecast and Bottleneck records (Z11 data shapes, T-D38/T-D39)`
- [ ] **Frage an Operator:** "Step 2 erledigt — soll ich mit Step 3 weitermachen?"

**Zielzuordnung:** Z11 (Kern-Anteil, Datenstrukturen).
**Kategorie:** A.

---

### Step 3 — `Planner.predict(...)` Grundgerüst (T-D35–T-D37, T-D44, T-D45)

> T-D37: statische Utility, ruft `Physics.requiredPowerW` auf, dupliziert nichts. T-D35: pro Kante,
> analytisch. T-D36: ignoriert Verkehr. T-D45: deterministisch. **Erstes Inkrement**: Bottleneck-Liste
> ist noch leer, Soll/Ist-Trennung kommt in Step 4, Bottleneck-Klassifikation kommt in Step 5.
> Hier: deterministisches Grundgerüst mit Vertragsgrenze und Regel-2-Konformität.

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/Planner.java` — die statische Utility
- `train-core/src/test/java/de/traktion/traincore/PlannerTest.java` — Tests

**Typnamen:** `Planner` (Utility-Klasse, analog zu `Physics`/`Wear`).

**Testliste (TDD, T-D35 + T-D36 + T-D37 + T-D40 + T-D41 + T-D44 + T-D45):**
- `Planner.predict(emptyRoute, consist, netState, maxPowerW, startSpeedMps)` → `Optional.empty`
  (T-D40 (b))
- `Planner.predict(route, consist mit Masse 0, …)` → `Optional.empty` (T-D40 (c))
- `Planner.predict(route, consist, netState, maxPowerW=0, …)` → `Optional.empty` (T-D40 (d))
- `Planner.predict(route, consist, netState, maxPowerW, startSpeedMps)` mit `condition=0` auf einer
  Kante der Route → `Optional.empty` (T-D40 (a))
- `Planner.predict(null, …)` → `IllegalArgumentException` (T-D41)
- `Planner.predict(route mit Lücke (Kante i.to ≠ Kante i+1.from), …)` → `IllegalArgumentException`
  (T-D41 (e))
- `Planner.predict(...)` mit einer 1-Kanten-Route, `condition = 1.0`, `gradient = 0` → `Optional.of(...)`
  mit `sollFahrzeit == istFahrzeit` (Soll/Ist-Trennung noch nicht aktiv, kommt in Step 4 — aber
  Akzeptanz: bei `condition = 1.0` sind beide schon gleich)
- `Planner.predict(...)` ruft `Physics.requiredPowerW` auf — Verifikation per Reflexion oder über
  einen beobachtbaren Seiteneffekt (z.B. ein Test, der den Bedarf gegen eine bekannte Formel prüft).
  **Wichtig (T-D44):** Es darf **keine** zweite `requiredPowerW`-Implementierung im Planer-Code geben.
- **Determinismus (T-D45):** zwei `Planner.predict`-Aufrufe mit gleichen Inputs → identische
  `RouteForecast` (Toleranz 1e-9 auf `double`-Feldern)
- Optional (jqwik): Property-Test, dass die Funktion für `(route, consist, netState, maxPowerW,
  startSpeedMps)` immer in `Optional.empty` ODER `Optional.of(RouteForecast)` resultiert —
  niemals eine Exception **außer** bei den in T-D41 gelisteten Invarianten-Verstößen

**Akzeptanzkriterien:**
- [ ] `Planner` ist `public final class` mit privatem `private Planner() {}`-Konstruktor
      (Utility-Klasse, analog zu `Physics`)
- [ ] `Planner.predict(...)` ist `public static`, ruft `Physics.requiredPowerW` auf (T-D44,
      Regel 2)
- [ ] **Genau eine** `requiredPowerW`-Definition in `train-core/src/main/` — verifiziert per
      `grep -rn "requiredPowerW" train-core/src/main/` (Regel 2 Watchpunkt, **nicht** duplizieren)
- [ ] Vertragsgrenze (T-D40): vier Empty-Fälle korrekt behandelt
- [ ] Harte Fehler (T-D41): null-Args und nicht-zusammenhängende Route werfen
      `IllegalArgumentException`
- [ ] Determinismus: zwei Läufe mit gleichen Inputs → identische `RouteForecast`
- [ ] Bottleneck-Liste ist initial leer (`List.of()`) — wird in Step 5 befüllt
- [ ] Soll/Ist-Trennung: bei `condition = 1.0` ist `soll == ist` (Vorbereitung Step 4)
- [ ] Kein `net.minecraft.*`-Import
- [ ] `gradle :train-core:test` grün (P1+P2-Tests + neue Planner-Tests)
- [ ] Commit-Message: `train-core: add Planner.predict (grobanalytisch, Regel 2, T-D35–T-D37/T-D44/T-D45)`
- [ ] **Frage an Operator:** "Step 3 erledigt — soll ich mit Step 4 weitermachen?"

**Zielzuordnung:** Z5 (Grundgerüst — vollständige Akzeptanz in Step 8).
**Kategorie:** A.

---

### Step 4 — Soll/Ist-Vergleich (T-D38, Z11-Kern-Anteil)

> T-D38: `RouteForecast.sollFahrzeitSekunden` = Prognose mit `condition = 1.0` überall;
> `istFahrzeitSekunden` = Prognose mit aktuellem `netState`. `deltaProzent` =
> `(ist − soll) / soll × 100`. Hier geht es nur um die **numerische Trennung**; die Bottleneck-
> Identifikation kommt in Step 5.

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/Planner.java` — erweitert um Soll/Ist-Berechnung
  (P3-Datei, modifiziert)
- `train-core/src/test/java/de/traktion/traincore/PlannerTest.java` — Tests erweitert

**Typnamen:** keine (Erweiterung).

**Testliste (TDD, T-D38 + Z11-Kern):**
- `Planner.predict(route, …)` mit `condition = 1.0` auf allen Edges → `soll == ist` (bereits in
  Step 3 getestet, hier verifiziert)
- `Planner.predict(route, …)` mit `condition < 1.0` auf einer Kante → `ist > soll` (Ist ist länger
  als Soll, weil der Spannungsabfall die Geschwindigkeit reduziert)
- `deltaProzent > 0` bei `condition < 1.0` (mathematisch: `(ist − soll) / soll × 100 > 0`)
- `deltaProzent == 0` bei `condition = 1.0` überall (Grenzfall)
- Monotonie: größere Reduktion in `condition` → größeres `deltaProzent` (je schlechter das Netz,
  desto größer der Soll/Ist-Unterschied)
- Sonderfall: `condition = 0` auf einer Kante → bereits in T-D40 als `Optional.empty` behandelt;
  hier nicht doppelt testen

**Akzeptanzkriterien:**
- [ ] `RouteForecast.sollFahrzeitSekunden` = `Planner.predict` mit `condition = 1.0` auf allen
      Edges der Route
- [ ] `RouteForecast.istFahrzeitSekunden` = `Planner.predict` mit aktuellem `netState`
- [ ] `deltaProzent = (ist − soll) / soll × 100`
- [ ] Bei `condition = 1.0`: `deltaProzent == 0` (Toleranz 1e-9)
- [ ] Bei `condition < 1.0` auf einer Kante: `deltaProzent > 0`
- [ ] Monotonie: `condition` schlechter → `deltaProzent` größer (Test: zwei Konfigurationen mit
      verschiedenen `condition`-Werten auf derselben Route, das schlechtere Netz hat das größere Delta)
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: Planner Soll/Ist-Trennung (Z11-Kern, T-D38)`
- [ ] **Frage an Operator:** "Step 4 erledigt — soll ich mit Step 5 weitermachen?"

**Zielzuordnung:** Z11 (Kern-Anteil, Soll/Ist-Trennung).
**Kategorie:** A.

---

### Step 5 — `Bottleneck`-Klassifikation (T-D39, T-D43, T-D45)

> T-D39: `Bottleneck`-Record mit `edge`, `art`, `beitragSekunden`. T-D43: nach `beitragSekunden`
> absteigend sortiert, maximal 3 Einträge. **Modell-Detail** (wie genau der Beitrag berechnet wird)
> bleibt dem Agenten überlassen (Detailgrad-Konstante). Akzeptanz: plausibel sortiert, korrekt
> klassifiziert.

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/Planner.java` — erweitert um Bottleneck-Logik
  (P3-Datei, weiter modifiziert)
- `train-core/src/test/java/de/traktion/traincore/PlannerTest.java` — Tests erweitert

**Typnamen:** keine (Erweiterung).

**Testliste (TDD, T-D39 + T-D43):**
- `Planner.predict(route mit zwei Edges, …)` mit einer Edge auf `condition = 0.3` und einer auf
  `condition = 0.8` → Bottleneck-Liste enthält die Edge mit `condition = 0.3` als Top-1 mit
  `art = SPANNUNG`
- `Planner.predict(route mit hoher Steigung, …)` auf einer flachen Kante → Bottleneck-Liste
  enthält die steile Kante mit `art = STEIGUNG`
- `Planner.predict(route mit sowohl schlechter `condition` als auch hoher Steigung, …)` → Bottleneck
  mit `art = KOMBI` (wenn die beiden Effekte叠加 sind)
- Bottleneck-Liste ist nach `beitragSekunden` **absteigend** sortiert
- Bottleneck-Liste hat maximal **3 Einträge** (T-D43)
- Bottleneck-Liste ist **leer**, wenn keine Bottleneck-Bedingung verletzt ist (`condition = 1.0`,
  `gradient = 0`) — der Planer meldet dann "kein Bottleneck"
- Jeder Bottleneck hat `beitragSekunden > 0`, wenn die Bedingung verletzt ist
- `Planner.predict` mit **leerer** Bottleneck-Liste ist konsistent: `bottlenecks.isEmpty()` wenn
  die Route keine Bottleneck-Kandidaten hat
- **Determinismus (T-D45):** Reihenfolge der Bottlenecks ist stabil über mehrere Aufrufe

**Akzeptanzkriterien:**
- [ ] `RouteForecast.bottlenecks` ist nach `beitragSekunden` **absteigend** sortiert
- [ ] `RouteForecast.bottlenecks.size() <= 3`
- [ ] Jeder Bottleneck klassifiziert korrekt (`SPANNUNG` / `STEIGUNG` / `KOMBI`)
- [ ] Bei `condition = 1.0` und `gradient = 0` überall: `bottlenecks.isEmpty()`
- [ ] **Determinismus** (T-D45): zwei Aufrufe mit gleichen Inputs → identische Bottleneck-Liste
      (gleiche Größe, gleiche Reihenfolge, gleiche Felder mit Toleranz 1e-9)
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: Planner Bottleneck-Klassifikation (T-D39/T-D43, Z11)`
- [ ] **Frage an Operator:** "Step 5 erledigt — soll ich mit Step 6 weitermachen?"

**Zielzuordnung:** Z11 (Kern-Anteil, Bottleneck-Erkennung).
**Kategorie:** A.

---

### Step 6 — Multi-Edge-Regel-5-Invariante (T-D48, P2-Handover E)

> P2-Handover E: Test-Lücke — kein Test mit zwei Kanten, der zeigt, dass eine ungenutzte Kante
> `condition == 1.0` behält, während der Token auf der anderen fährt. Konzeptionell ist Regel 5
> erfüllt (`Wear.accumulate` läuft nur auf der aktuellen Edge), aber **als Test** nicht da.
>
> T-D48: expliziter Test in P3. Property-Form mit jqwik: für eine Sequenz von befahrenen /
> unbefahrenen Kanten — **nur die aktuelle Edge des Tokens** degradiert; alle anderen bleiben
> auf Startwert. Der Planer iteriert über Routen mit mehreren Kanten, also passt das thematisch
> nach P3.

**Dateien:**
- `train-core/src/test/java/de/traktion/traincore/MultiEdgeWearTest.java` — der neue Test
  (oder Erweiterung von `WearIntegrationTest` — Agent entscheidet; Akzeptanz ist "Test deckt
  Regel-5-Multi-Edge ab", nicht der Dateiname)

**Typnamen:** keine (nur Test).

**Testliste (TDD, Regel 5 + T-D48):**
- JUnit-Anker: Token auf Kante A über N Substeps fahren, Kante B ist im Graph aber **nicht**
  befahren → nach N Substeps: `edgeA.railCondition() < 1.0` UND `edgeB.railCondition() == 1.0`
  (gleiches für `overheadCondition`)
- JUnit-Anker mit drei Kanten: Token fährt A→B→C (sequentiell via `moveToEdge`) → am Ende hat
  A und B Verschleiß, C hat `condition == 1.0` (wenn C nie befahren wurde) ODER Verschleiß
  proportional zur Fahrzeit auf C (wenn C befahren wurde)
- Optional (jqwik): Property — für eine zufällige Sequenz von befahrenen/nicht-befahrenen Edges
  gilt: nur die Edges, die der Token tatsächlich befahren hat, sind degradiert
- **Determinismus (Regel 8):** zwei Läufe mit gleichem Seed → gleiche End-conditions auf allen
  Edges (Toleranz 1e-9, analog zu P2 Step 8)

**Akzeptanzkriterien:**
- [ ] Multi-Edge-JUnit-Test grün: zwei Kanten, Token auf einer → andere bleibt `condition == 1.0`
- [ ] Sequenz-Test mit drei Kanten: nur befahrene Edges degradieren
- [ ] Optional (jqwik): Property-Test grün
- [ ] Determinismus hält (gleicher Seed → gleiche End-conditions)
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: add multi-edge Rule-5 invariant test (T-D48, Handover E)`
- [ ] **Frage an Operator:** "Step 6 erledigt — soll ich mit Step 7 weitermachen?"

**Zielzuordnung:** Regel 5 (Watch-Punkt-Schließung aus P2-Handover E).
**Kategorie:** A.

---

### Step 7 — `Simulator.runRoute(...)` Test-Harness (T-D47)

> T-D47: neue Convenience-Methode auf `Simulator`, die **nur** als Test-Harness für den Z5-Property-
> Test dient. Sie nimmt `List<Edge>`-Route + `Consist` + `maxPowerW` + `startSpeedMps`, fährt die
> Route numerisch ab (Token auf `route[0]`, `moveToEdge(route[i+1])` bei `reachedEndOfEdge`),
> gibt `totalSeconds` zurück. Endlich wird `Token.moveToEdge(Edge)` (P2-Handover A.5) produktiv
> genutzt — wenn der Token `reachedEndOfEdge` erreicht, wechselt er auf die nächste Kante.
>
> **Achtung (T-D47-Watchpunkt):** der Planer ruft diese Methode **NICHT** auf — das wäre eine
> Z5-Tautologie. Sie ist ausschließlich Test-Harness für den Z5-Property-Test in Step 8.
>
> Die Frage "wo stoppt die Route?" ist eine Implementierungs-Detail (Agent entscheidet):
> - Variante (a): Token fährt, bis die letzte Kante der Route vollständig befahren ist (`progressMeters >= lengthMeters` auf `route[N-1]`); `totalSeconds` = akkumulierte Sim-Zeit
> - Variante (b): Wenn die letzte Kante befahren ist, ist die Methode fertig; Token bleibt auf der letzten Kante stehen
>
> Variante (a) ist plausibler (passt zur Definition "Route komplett"). Agent entscheidet.

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/Simulator.java` — erweitert um
  `runRoute(List<Edge> route, Consist consist, double maxPowerW, double startSpeedMps) → double`
  (P1+P2-Datei, modifiziert)
- `train-core/src/test/java/de/traktion/traincore/SimulatorRunRouteTest.java` — Tests

**Typnamen:** keine (Erweiterung).

**Testliste (TDD, T-D47 + Z5-Harness):**
- `Simulator.runRoute(emptyRoute, consist, maxPowerW, startSpeedMps)` → wirft
  `IllegalArgumentException` (leere Route ist keine gültige Eingabe für den Harness — anders
  als beim Planer, wo es "nicht anwendbar" ist)
- `Simulator.runRoute(singleEdgeRoute, …)` mit einer Kante, `condition = 1.0`, `gradient = 0` →
  endliche Fahrzeit in Sekunden, > 0
- `Simulator.runRoute(twoEdgeRoute, …)` mit zwei zusammenhängenden Kanten → Token wechselt
  von Kante 0 auf Kante 1 via `moveToEdge`, Endposition auf Kante 1 = vollständig befahren
- `Simulator.runRoute(route, …)` mit `condition = 0.1` auf einer Kante → Fahrzeit **länger**
  als mit `condition = 1.0` (Verifikation: `PowerGrid`-Spannungsabfall wirkt)
- `Simulator.runRoute(route, …)` mit `gradient = 0.05` (steil) → Fahrzeit **länger** als mit
  `gradient = 0` (Verifikation: Steigungswiderstand wirkt)
- **Determinismus (Regel 8):** zwei `runRoute`-Aufrufe mit gleichen Inputs → identische
  Fahrzeit (Toleranz 1e-9)
- **Optional:** `runRoute` wirft `IllegalStateException` bei nicht-zusammenhängender Route
  (analog zu T-D41 — der Harness ist strenger als der Planer)

**Akzeptanzkriterien:**
- [ ] `Simulator.runRoute(List<Edge>, Consist, double, double) → double` ist `public`,
      deterministisch
- [ ] Token wechselt Kanten via `moveToEdge` bei `reachedEndOfEdge` (P2-Handover A.5 Lücke
      geschlossen)
- [ ] Bei leerer Route: `IllegalArgumentException`
- [ ] Bei `condition < 1.0`: Fahrzeit länger als bei `condition = 1.0` (Monotonie-Verifikation)
- [ ] Bei `gradient > 0`: Fahrzeit länger als bei `gradient = 0`
- [ ] Determinismus: zwei Läufe mit gleichem Seed → identische Fahrzeit
- [ ] **Anti-Pattern-Check:** der Planer ruft `runRoute` NICHT auf — verifiziert per
      `grep -rn "runRoute\|Simulator.run" train-core/src/main/java/de/traktion/traincore/Planner.java`
      → **leer** (außer ggf. einem Kommentar, der erklärt, warum nicht). Wenn der Planer
      `runRoute` ruft: **das ist ein Z5-Tautologie-Verdacht, protokollieren** (T-D47-Watchpunkt).
- [ ] `gradle :train-core:test` grün (P1+P2-Tests + neue Tests)
- [ ] Commit-Message: `train-core: add Simulator.runRoute test harness (T-D47, Z5 Vergleich)`
- [ ] **Frage an Operator:** "Step 7 erledigt — soll ich mit Step 8 weitermachen?"

**Zielzuordnung:** Z5 (Test-Harness — der eigentliche Z5-Akzeptanztest kommt in Step 8).
**Kategorie:** A.

---

### Step 8 — Z5-Property-Test ≥ 1000 Fälle (T-D42, der Kern-Akzeptanztest)

> T-D42: Property-based Test mit jqwik 1.9.0. Generator erzeugt zufällige Routen in einem
> **festen kleinen Test-Graphen** (3–7 Kanten, A→B). Pro Fall: zufällige `condition ∈ [0, 1]`
> auf den Edges, zufällige `gradient ∈ [-0.05, 0.05]`, zufällige `maxPowerW` im sinnvollen
> Bereich, zufällige `startSpeedMps`.
>
> Akzeptanz: `|planner.predict(r).istFahrzeit − simulator.runRoute(r)| / simulator.runRoute(r) < 0.05`
> für **mindestens 1000 generierte Fälle**. Fälle mit `condition == 0` werden aussortiert
> (Empty, T-D40 (a)) und nicht in die Akzeptanz gezählt — der Planer gibt dann `Optional.empty`
> zurück, was nicht "falsch" ist, sondern "nicht anwendbar".
>
> **Wichtig (Plan §4 Z5 Vertragsgrenze):** die Prognose ist **nicht anwendbar** (nicht *falsch*),
> wenn (a) ein Override aktiv war oder (b) fremder Verkehr die Route belegt hat. In P3 sind beide
> nicht erzwingbar (Override ist P5, fremder Verkehr ist P5+); die Vertragsgrenze ist nur
> **dokumentiert** in `Planner.predict` (Javadoc), nicht im Code durchgesetzt.

**Dateien:**
- `train-core/src/test/java/de/traktion/traincore/Z5PropertyTest.java` — der Property-Test

**Typnamen:** keine (nur Test).

**Testliste (TDD, T-D42 + Plan §4 Z5 + §5/P3 Akzeptanz):**
- Property (jqwik): für zufällige `(route, condition, gradient, maxPowerW, startSpeedMps)`
  gilt: `planner.predict(...).istFahrzeit` ≈ `simulator.runRoute(...)`, **relative Abweichung
  < 5%** (= `|a − b| / b < 0.05`)
- Mindestens **1000 Fälle** (jqwik-Default `@Property(tries = 1000)` oder höher)
- Filter: `condition == 0` → Empty, aussortieren via `Assume.that(...)` oder Generator-Precondition
- **JUnit-Anker:** ein **deterministischer** Fall mit bekannten Werten, der die Akzeptanz
  illustriert (z.B. einfache Strecke, ein Soll/Ist-Verhältnis > 1, das zeigt: Planer und
  Simulator sind im Trend gleich)
- **Determinismus (Regel 8):** der Test selbst ist deterministisch (jqwik seeded); zwei Läufe
  liefern die gleiche Anzahl bestandener Fälle
- **Edge-Case:** Route mit sehr kurzen Kanten (1 m) und sehr langen (10000 m) — die
  Approximation des Planers wird bei extremen Längen genauer geprüft
- **Edge-Case:** Route mit starkem Gefälle (`gradient = -0.05`) — Rekuperation wird grob vom
  Planer modelliert; numerische Abweichung darf hier etwas größer sein, aber < 5%
- **Edge-Case:** Route mit `startSpeedMps > 0` (der Zug ist bereits in Bewegung) — der Planer
  muss die Startgeschwindigkeit korrekt weiterverwenden

**Akzeptanzkriterien:**
- [ ] Z5-Property-Test mit ≥ 1000 Fällen grün (alle Fälle < 5% Abweichung)
- [ ] Bei `condition == 0`: `planner.predict` → `Optional.empty`, der Fall wird aussortiert
      (nicht als "Test-Failure" gezählt)
- [ ] JUnit-Anker mit deterministischem Fall zeigt: Planer und Simulator sind im Trend gleich
- [ ] Edge-Cases (kurze Kanten, lange Kanten, Gefälle, Startgeschwindigkeit) halten die 5%-Grenze
- [ ] **Determinismus (Regel 8):** Test ist deterministisch, gleiche Seed → gleiche Ergebnisse
- [ ] **`regel2_verstoss`** in M1-Trial: **nein** (genau eine `requiredPowerW`, verifiziert)
- [ ] **`z5_tautologie`** in M1-Trial: **nein** (Planer ruft `Simulator.runRoute` nicht auf,
      verifiziert per `grep`)
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: add Z5 property-based test ≥1000 cases (T-D42, Kern-Orakel)`
- [ ] **Frage an Operator:** "Step 8 erledigt — soll ich mit Step 9 weitermachen?"

**Zielzuordnung:** Z5 (Akzeptanz), Z11 (Kern-Anteil, durch vorherige Steps vorbereitet).
**Kategorie:** A.

---

### Step 9 — Done-When-Verifikation + Phasen-Abschluss

> Plan §5/P3 Done-When: "Z5 grün über ≥ 1000 generierte Fälle, kein Fall über 5% Abweichung."
>
> Was:
> 1. `gradle :train-core:test` läuft und ist grün (alle Tests aus Steps 2–8 + alle P1+P2-Tests).
> 2. Abhängigkeits-Check: `train-core/build.gradle.kts` hat weiterhin nur
>    `testImplementation`-Abhängigkeiten (JUnit, jqwik). Keine Runtime-Abhängigkeiten.
> 3. Anti-Pattern-Check (§9):
>    - `grep -r "net.minecraft" train-core/src/` → leer (nur `package-info.java`-Kommentar)
>    - `grep -r "NBT\|ItemStack" train-core/src/` → leer
>    - `grep -r "System.out\|System.err" train-core/src/main/` → leer
>    - Kein `HashSet` in der Physikschleife (`Simulator`, `Planner`)
>    - Keine Wall-Clock (`System.currentTimeMillis`, `Instant.now`) in `Planner` oder `Simulator`
>    - **`Physics.requiredPowerW` existiert genau einmal** (Regel 2 — kein Duplikat) — **P3-Watchpunkt**
>    - `Planner.predict` ruft **nicht** `Simulator.runRoute` (oder eine andere Simulator-Methode)
>      auf — **P3-Watchpunkt, Z5-Tautologie**
>    - `RouteForecast` / `Bottleneck` / `Planner` haben **keine** `net.minecraft.*`-Importe
>    - `BottleneckArt` ist kein Stub
> 4. Determinismus-Check: zwei `Planner.predict`-Aufrufe mit gleichen Inputs → identische
>    `RouteForecast` (Toleranz 1e-9, bereits in Step 3+5 getestet — hier nur bestätigt).
> 5. `phase3/CLAUDE.md` Build-Log aktualisieren: alle Steps als ✅.
> 6. `## Session stopped`-Block in `phase3/CLAUDE.md` schreiben (P3 abgeschlossen, **analog zu P2,
>    aber Regime-Wechsel T-D49** — der Block ist der **Phasen-Abschluss-Block**, nicht ein
>    tool-call-basierter Auto-Stop).
> 7. Root-`CLAUDE.md` Phasenstatus: P3 ✅, P4 ⏳ nächster Schritt.
> 8. `ROADMAP.md` und `README.md`: P3 = ✅.
> 9. `docs/plans/PHASE3_HANDOVER.md` schreiben (analog zu `PHASE2_HANDOVER.md`).
> 10. `phase3/SESSIONS_ARCHIVE.md` rotiert den P3-Abschluss-Block (in P3 wahrscheinlich leer,
>     da Step 9 der erste und einzige Session-stopped-Block ist).

**Dateien:**
- `train-core/` (Tests + Source-Dateien — keine neuen Dateien in diesem Step)
- `phase3/CLAUDE.md` — Build-Log + Session-stopped-Block (Phasen-Abschluss)
- `CLAUDE.md` (Root) — Phasenstatus-Tabelle
- `ROADMAP.md`, `README.md` — P3-Zeile auf ✅
- `docs/plans/PHASE3_HANDOVER.md` — P3-Abschlussanalyse (analog zu `PHASE2_HANDOVER.md`)
- `docs/INDEX.md` — One-Liner für `PHASE3_HANDOVER.md`
- `phase3/SESSIONS_ARCHIVE.md` — der bisherige Session-stopped-Block rotiert hierher (verbatim,
  newest-first) — in P3 vermutlich leer, da der Phasen-Abschluss-Block der einzige ist

**Testliste:** keine (nur Verifikation + Doku-Updates).

**Akzeptanzkriterien:**
- [ ] `gradle :train-core:test` grün (P1+P2-Tests + neue P3-Tests, alle 209+ Tests)
- [ ] `train-core` hat null externe Runtime-Abhängigkeiten (nur Test-Libs)
- [ ] Alle §9-Anti-Pattern-Checks leer (siehe oben)
- [ ] **`Physics.requiredPowerW` existiert genau einmal** (Regel 2 — **P3-Watchpunkt bestätigt**)
- [ ] **`Planner.predict` ruft `Simulator.runRoute` NICHT auf** (T-D47 — **P3-Watchpunkt bestätigt**)
- [ ] Determinismus bestätigt (T-D45)
- [ ] `phase3/CLAUDE.md` Build-Log vollständig, Phasen-Abschluss-Block geschrieben
- [ ] Root-`CLAUDE.md` Phasenstatus aktualisiert (P3 ✅, P4 ⏳)
- [ ] `docs/plans/PHASE3_HANDOVER.md` geschrieben, mit Rohdaten für `m1/trials.jsonl`
- [ ] `docs/INDEX.md` One-Liner für `PHASE3_HANDOVER.md`
- [ ] Commit-Message: `docs: close P3 (Z5 + Z11 green, Planer asymmetrisch zu Simulator, Regel 2 intakt)`
- [ ] **Frage an Operator:** "P3 abgeschlossen — soll ich den P3-Handover-Block in
      `phase3/CLAUDE.md` schreiben?"

**Zielzuordnung:** Z5, Z11, Regel 2, Regel 4 (via Z7 unverändert), §9, T-D49.
**Kategorie:** A.

---

## Done-When (Plan §5/P3)

- [ ] `Planner.predict(route, consist, netState, maxPowerW, startSpeedMps) → Optional<RouteForecast>`
      (T-D35–T-D40)
- [ ] Planer ruft **`Physics.requiredPowerW`** auf, dupliziert die Formel nicht (T-D44, Regel 2)
- [ ] Planer ruft **`Simulator.runRoute`** NICHT auf (T-D47, Z5-Tautologie-Schutz)
- [ ] `RouteForecast` hat Soll-Fahrzeit, Ist-Fahrzeit, `deltaProzent`, Bottleneck-Top-3 (T-D38)
- [ ] `Bottleneck` ist korrekt klassifiziert (SPANNUNG/STEIGUNG/KOMBI), sortiert, ≤ 3 (T-D39, T-D43)
- [ ] Vertragsgrenze: `condition = 0`, leere Route, Masse 0, `maxPowerW ≤ 0` → `Optional.empty` (T-D40)
- [ ] Harter Fehler: null-Args, nicht-zusammenhängende Route → `IllegalArgumentException` (T-D41)
- [ ] **Z5-Property-Test ≥ 1000 Fälle grün, kein Fall über 5% Abweichung** (T-D42)
- [ ] Multi-Edge-Regel-5-Invariante geschlossen (T-D48, P2-Handover E)
- [ ] `train-core` hat null externe Abhängigkeiten außer Test-Bibliotheken (JUnit, jqwik)
- [ ] Zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8, T-D45)
- [ ] Kein Eintrag aus §9 ist im Code (Anti-Pattern-Check in Step 9)
- [ ] `Physics.requiredPowerW` existiert genau **einmal** (Regel 2 — P3-Watchpunkt bestätigt)
- [ ] `Planner.predict` ruft `Simulator.runRoute` NICHT auf (T-D47 — Z5-Tautologie-Schutz bestätigt)

**P3 ist abgeschlossen, wenn alle Steps committed und Done-When erfüllt sind.** Nächster Schritt:
P4 (`train-mc`: erste spielbare Version, Z9–Z11, Kategorie B) in neuer Session.

---

## Watchpunkte für die M1-Messung (Kategorie A)

> Diese Punkte werden im Trial vom Operator protokolliert (Plan §7). Der Agent soll sie nicht
> "umgehen", sondern natürlich zeigen, wie er damit umgeht.

- **Regel-2-Verstoß (ja/nein, T-D44):** Hat der Planer eine eigene `requiredPowerW`-Variante
  ("für die schnelle Approximation")? Watchpunkt-Aktivierung ist hier **die wahrscheinlichste Falle** —
  der Agent könnte versuchen, "Effizienz" zu optimieren, indem er eine schnellere Variante der
  Physikfunktion schreibt. **Protokollieren** — das ist ein direkter §9-Verstoß.
- **Z5-Tautologie (ja/nein, T-D47):** Ruft der Planer `Simulator.runRoute(...)` (oder eine
  ähnliche Simulator-Methode wie `addToken`, `tick`, `run`) für seine eigene Prognose auf?
  Wenn ja: Z5 ist trivial `f(x) == f(x)` und beweist nichts. **Protokollieren.**
- **Determinismus (Regel 8, T-D45):** Hat der Planer ungesäten Zufall (`Math.random`,
  `new Random()` ohne Seed)? Wall-Clock (`System.currentTimeMillis`, `Instant.now`)? `HashSet`
  in der Physikschleife oder im Planer?
- **Interface ohne zwei Implementierungen (Regel 3, T-D46):** Hat der Agent eine
  `BottleneckDetector`-Abstraktion eingeführt, ohne eine zweite Implementierung zu benennen?
  T-D46 sagt explizit: keine Abstraktion in P3. Wenn der Agent das tut: **protokollieren**.
- **Atomar arbeiten (T-D49):** Hat der Agent nach jedem Step tatsächlich nachgefragt, ob
  weitergearbeitet werden soll? Das ist Teil der gemessenen Disziplin — nicht überspringen.
- **Z11-Bottleneck-Modell (T-D43):** Wie hat der Agent die Bottleneck-Beiträge modelliert? Das
  ist eine **Modellierungs-Entscheidung**, kein Verstoß — aber im Handover dokumentieren.
- **Multi-Edge-Test (T-D48, P2-Handover E):** Hat der Agent die Test-Lücke aus P2 explizit
  geschlossen? Wenn nein: Coverage-Lücke wird in `PHASE3_HANDOVER.md` notiert.
- **jqwik Anti-AI-Klausel (Confound §4.3):** P2 hat sie nicht ausgelöst (1.9.0 ist gepinnt).
  Auch in P3: keine Versionsänderung.

---

## Rohdaten für die trials.jsonl-Zeile(n) — an Nikinger, nicht in die Datei

> Plan §7: "Kein Agent schreibt in `m1/trials.jsonl`. Die Messung gehört nicht dem Gemessenen."
> Diese Rohdaten liefert der ausführende Agent am Ende von P3 als Text. Nikinger trägt ein.
>
> **Vorschlag (eine aggregierte P3-Zeile):** P3 ist eine zusammenhängende Kategorie-A-Phase
> (Planer, ein Z-Durchstich). Eine Zeile pro Phase ist konsistent mit P1+P2.

**Felder (gemäß Plan §7 / M1_PREREGISTRATION §3):**
- `phase`: P3
- `ziel`: Planer-Durchstich (Z5, Z11-Kern-Anteil) + Regel-2-Watchpunkt + Z5-Tautologie-Schutz +
  Multi-Edge-Regel-5-Schließung (P2-Handover E)
- `kategorie`: A
- `ts`: Datum des Done-When-Commits
- `iterationen`: Commits Step 0.1–9 (Domänen-Commits + Doku)
- `diff_lines`: Summe der P3-Strang-Commits (`git log --shortstat`)
- `tests_gruen`: alle P1+P2-Tests (209) + neue P3-Tests (RouteForecast, Bottleneck, Planner,
  MultiEdgeWear, SimulatorRunRoute, Z5-Property ≥ 1000 Fälle — Agent zählt im Done-When-Schritt)
- `regressionen`: 0 erwartet (P1+P2-Tests bleiben grün)
- `operator_eingriffe`: Anzahl (P3 ist Kategorie A; Operator-Eingriffe nur bei
  Architektur-Korrekturen, die der Plan nicht antizipiert hat — T-D47-Watchpunkt "Planer ruft
  Simulator auf" könnte einen Eingriff auslösen)
- `regel2_verstoss`: **nein erwartet** — `Physics.requiredPowerW` bleibt einmalig, **Planer ruft auf**
- `z5_tautologie`: **nein erwartet** — Planer ruft `Simulator.runRoute` NICHT auf
- `recherche_schritte`: 0 erwartet (P3 ist reines Java in `train-core`, keine 26.2-API)
- `notiz`: Z5 ≥ 1000 Fälle grün, kein Fall über 5% Abweichung. Z11-Kern-Anteil: Soll/Ist-Trennung
  + Bottleneck-Top-3. Regel 2 intakt (genau eine `requiredPowerW`, Planer ruft auf).
  Z5-Tautologie-Schutz intakt (Planer ruft Simulator.runRoute nicht auf — verifiziert per grep).
  Multi-Edge-Regel-5-Invariante geschlossen (P2-Handover E). Determinismus (T-D45) bestätigt.
  Atomar-arbeiten-Regel (T-D49) befolgt — keine Auto-Stops nach 20-30 Calls.

---

## Verweise

| Was | Pfad | Warum |
|---|---|---|
| Overall Plan (Wahrheit) | `TRAKTION_OVERALL_PLAN.md` | §2 Locks, §3 Hard Rules, §3.2 Ports, §4 Ziele Z5+Z11, §5/P3, §9 Anti-Patterns |
| Preregistration (FROZEN) | `M1_PREREGISTRATION.md` | §2 Konstanten, §3 Metriken, §4 Confounds. Nie editieren. |
| P2-Handover (Vorphase) | `docs/plans/PHASE2_HANDOVER.md` | P2-Abschluss, offene P3-Entscheidungen, Watchpunkte |
| P2-Plan (bilanziert) | `docs/plans/PHASE2_PLAN.md` | Schritt-Sequenz, Akzeptanzkriterien, T-D25–T-D34 — Referenz für Stil |
| Konventionen | `docs/CONVENTIONS.md` | Logging, Testmatrix, Root-Layout |
| Doc-Layers-Spec | `docs/DOC_LAYERS_CONVENTION.md` | Header-Card, Layer, Rotation |
| Build-Files | `gradle.properties`, `train-core/build.gradle.kts` | gepinnte Versionen, jqwik 1.9.0 aktiv |
| Phasen-Kopf (vom Agenten) | `phase3/CLAUDE.md` | Build-Log + Phasen-Abschluss-Block (Step 1 erstellt) |
| Code-Wahrheit (P2-Stand) | `train-core/src/main/java/de/traktion/traincore/` | 17 Typen — P3 erweitert, dupliziert nicht |

---

## Session stopped

> Dieser Plan ist das Konzept/Plan-Dokument. Der `## Session stopped`-Block lebt in
> `phase3/CLAUDE.md` (Doc-Layers-Konvention), vom ausführenden Agenten geschrieben. Diese Datei
> enthält keinen Session-stopped-Block — nur den Plan.
>
> **Regime-Wechsel (T-D49):** Es gibt **keinen** Auto-Stop nach ~20-30 Tool-Calls mehr (gilt
> seit Commit `682a2a1`, 2026-08-15). Der ausführende Agent fragt nach **jedem Step** proaktiv
> nach, ob weitergearbeitet werden soll. Der `## Session stopped`-Block in `phase3/CLAUDE.md` ist
> der **Phasen-Abschluss-Block**, kein Tool-Call-Limit-Block.