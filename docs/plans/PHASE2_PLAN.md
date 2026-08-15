---
status: plan (ausführungsreif für P2)
purpose: Phasenplan für P2 — Verschleiß + Ports (Z6, Z7). Architektur-Entscheidungen, Schritt-Sequenz, Testlisten, Akzeptanzkriterien. `condition ∈ [0,1]` auf Kante und Oberleitung, `wear += f(masse, v)` pro Substep, `condition` → Spannungsabfall (schließt Z4 ab), zweite `PowerSupply`-Implementierung (`ManualGenerator`), `MaintenanceSupply`-Port + `PlayerLabor`, Z7-Bootstrap-Invariante.
read-when: Ausführung von P2; vor jedem P2-Schritt; Referenz für §7 (Softlock-Schutz fällt nie) und Z11 (Verschleiß → Leitstand)
detail: L2
up: ../TRAKTION_OVERALL_PLAN.md
down:
  - ../docs/CONVENTIONS.md          # Logging, Testmatrix, Root-Layout
  - ./PHASE1_HANDOVER.md            # Vorphase-P1-Abschlussanalyse, offene P2-Fragen, Watchpunkte
related: ../phase2/CLAUDE.md         # Phasen-Kopf (vom ausführenden Agenten als erste Aktion erstellt)
supersedes: (kein Vorläufer)
updated: 2026-08-15
---

# Phase 2 — Verschleiß + Ports

> **Kategorie A** (Plan §5/P2, §7). Klassische Informatik: numerische Bilanzen, Akkumulatoren,
> Property-Tests. **Kein Minecraft, kein Planer.** Der Verschleiß-Durchstich — `condition` →
> Spannungsabfall, plus die zwei produktiven Ports (Plan §3.2).
>
> **Wahrheit:** `TRAKTION_OVERALL_PLAN.md` §5/P2, §3 (Hard Rules), §3.2 (Ports), §4 (Ziele Z6, Z7;
> Z4 wird durch condition abgeschlossen), §9 (Anti-Patterns).
> **Vorphase:** `docs/plans/PHASE1_HANDOVER.md` (P1-Abschluss, offene Entscheidungen, [VERIFY]-Erbe).
>
> **Detailgrad:** Dieser Plan spezifiziert Typnamen, Dateipfade, Zielzuordnung (Z\<x\>), Kategorie
> (A/B), Testliste, Akzeptanzkriterien. Er spezifiziert **NICHT** Methodensignaturen,
> Funktionskörper, Zeilenanker oder Algorithmen (M1_PREREGISTRATION §2 "Plan-Detailgrad"). Die
> Übersetzung von Akzeptanzkriterium zu Implementierung IST die gemessene Fähigkeit (Kategorie A).

---

## Architektur-Entscheidungen dieser Phase

| # | Thema | Lock | Status |
|---|---|---|---|
| **T-D25** | `condition`-Lokation (Z6, Z7) | **Zwei Zahlen pro `Edge`** (Q1/Antwort: A.2): `railCondition` (mechanischer Verschleiß der Schiene) + `overheadCondition` (elektrischer Verschleiß der Oberleitung), beide `double ∈ [0,1]`, **Default = 1.0** (perfekte Neuanlage). Beide starten perfekt; Verschleiß senkt; Reparatur hebt. Beide getrennt, weil Schiene und Oberleitung verschiedene Infrastruktur sind und verschiedene Reparatur-Vorgänge haben. Spannungsabfall hängt vom schlechteren Wert ab (siehe T-D27). | gelockt (Nikinger, 2026-08-15) |
| **T-D26** | Reparatur-API-Form (Rule 6) | **Zwei direkte Mutatoren auf `Edge`** (Q3/Antwort: C.1, verfeinert): `repairRail(amount)` und `repairOverhead(amount)`. Keine Sammel-Methode — A.2 verlangt getrennte Ströme. `MaintenanceSupply` liefert nur die Einheiten (`withdraw(n)`); die Anwendung auf die Edge ist Sache des Callers (z.B. Reparatur-Routine, P4-Weltinteraktion). | gelockt (Nikinger, 2026-08-15) |
| **T-D27** | `PowerGrid.availableW`-Signatur (Z4-Abschluss) | **Condition als zusätzlicher Parameter** (nicht als State im `PowerGrid`). Erweiterte Form: `availableW(requestedW, distanceMeters, condition, dtSeconds)`. `condition` ist der **elektrisch relevante Wert** — der Simulator leitet `min(edge.railCondition(), edge.overheadCondition())` durch. Spannungsabfall = `f(Distanz, condition)`: niedrigeres `condition` → höherer effektiver Widerstand → weniger `availW`. Formel-Details (linear, exponentiell, etc.) überlasse ich dem Agenten — die Richtung ist im Plan gelockt (T-D5: "Spannungsabfall ↑"), die Formel nicht. | gelockt (Nikinger, 2026-08-15) |
| **T-D28** | `MaintenanceSupply`-Port-Scope (Regel 3) | Port `MaintenanceSupply` (Plan §3.2) wird in P2 eingeführt mit **einer** Produktions-Implementierung `PlayerLabor` (heute). Zweite benennbare Implementierung: `DepotStock` (P5, Fahrplan, Plan §3.2). Damit erfüllt der Port Regel 3 (zwei heute benennbare Implementierungen) ohne Test-Stub (Regel 4: "PlayerLabor ist kein Stub"). `DepotStock` wird in der Javadoc und in der `ARCHITECTURE.md` benannt, nicht implementiert. | gelockt (Nikinger, 2026-08-15) |
| **T-D29** | `PlayerLabor`-Modellierung (Q2/Antwort: B.1) | **Zeit-Akkumulator** im `PlayerLabor`: endlicher Vorrat `workAvailable` (Default z.B. 0 am Start), pro Simulator-Tick aufgefüllt um `ratePerTick` (Default z.B. 5), gedeckelt bei `maxWork` (Default z.B. 20). `withdraw(n)` zieht bis zu `n` aus dem Vorrat ab und gibt die tatsächlich entnommene Menge zurück. Das modelliert: der Spieler hat unbegrenzte Ausdauer, aber **Zeit** ist der Engpass — ein kostenloser Stub würde Z6/Z7 zu leeren Tests machen (Plan §3.2). Die genauen Default-Werte (Rate, Max) und ob der Simulator den Akkumulator treibt oder ob der Akkumulator selbst `tick(dt)` exponiert, überlasse ich dem Agenten — der Vertrag ist: `withdraw(n)` ist nicht-instant, Zeit muss vergehen. | gelockt (Nikinger, 2026-08-15) |
| **T-D30** | `ManualGenerator`-Scope (Port 1, Regel 3) | `ManualGenerator` ist die **zweite** produktive `PowerSupply`-Implementierung (P1 hatte `FixedSupply` als Test-Stub, Plan §3.2 "ManualGenerator — fester Output, Brennstoff von Hand — dauerhafte Rückfallebene"). Der Vertrag bleibt `supply(requestedW, dtSeconds) → ≤ requestedW` (Plan §3.2). Inhaltlich: fester Maximal-Output, "Brennstoff" als interner Vorrat, der durch Weltinteraktion (P4) oder einen Test-Hook aufgefüllt wird. Genauer Inhalt überlasse ich dem Agenten; Akzeptanz ist: `PowerSupply`-Vertrag, endlicher Vorrat, kein `null`-Output. | gelockt (Nikinger, 2026-08-15) |
| **T-D31** | Verschleiß-Akkumulations-Granularität | **Pro Substep** (`TICK_SECONDS / nSubsteps`, T-D13): pro Substep akkumuliert der Simulator einen Beitrag in `Edge.railCondition` und `Edge.overheadCondition`. Die Akkumulation ist **funktional** in `(mass, speed, dt)` und nutzt den gesäten `Random` des Simulators (T-D13, Regel 8). T-D4 sagt "pro Durchfahrt" — interpretiert als "pro Zeit, in der ein Token die Kante nutzt". Eine ungenutzte Kante (`v = 0` und kein Token-Reservierung) bekommt **keinen** Verschleiß (Regel 5: "Verschleiß bestraft Nutzung, nicht Existenz"). | gelockt (Nikinger, 2026-08-15) |
| **T-D32** | Z7-Bootstrap-Invariante (Q4/Antwort: D.1) | **Akzeptanzschwelle = `condition > 0`** auf allen Kanten. Die Invariante lautet: aus **jedem** erreichbaren Verfallszustand (alle `condition ∈ [0, 1)`) führt eine endliche Sequenz von `withdraw(n) → repairRail(n) + repairOverhead(n)`-Aufrufen zu `condition > 0` auf jeder Kante. Das beweist: der Spieler hat immer einen Ausweg (Regel 4). Die **konkrete Test-Form** ist ein property-based Test mit jqwik: Generator erzeugt zufällige Verfallszustände, der Reparatur-Loop reduziert die Summe der `(1 - condition)` monoton, terminiert. | gelockt (Nikinger, 2026-08-15) |
| **T-D33** | Z6-Langlauf-Akzeptanz (Done-When) | **10.000 Ticks Dauerbetrieb** (Plan §5/P2 Done-When): ein Token fährt auf einer/mehreren Kanten, am Ende ist `condition < 1.0` messbar (degradiert), aber `speedMps > 0` immer noch (nicht total blockiert — T-D5: "kontinuierlich, nie blockierend"). Der Test ist deterministisch (T-D13, Regel 8): zwei Läufe mit gleichem Seed liefern gleiche End-conditions. | gelockt (Nikinger, 2026-08-15) |
| **T-D34** | P2 hat **keine** neuen `[VERIFY]`-Marken | P2 ist reines Java in `train-core` (kein 26.2-API-Kontakt). Die einzigen `[VERIFY]`-Erben aus P1 (`jqwik`/`Gradle 9.5.1` — in P1 aufgelöst; Fabric-Logging-Konvention, `SavedData` — beide P4) sind P2-irrelevant. Eine neue `[VERIFY]`-Marke ist nur zu setzen, wenn der Agent während der Ausführung **trotzdem** auf eine ungeklärte API-Frage stößt — was bei reinem `train-core`-Code unwahrscheinlich ist. | festgeschrieben (Nikinger, 2026-08-15) |

### ⚠ Watchpunkte, die nicht stillschweigend korrigiert werden dürfen

- **Regel 2 bleibt heilig:** `Physics.requiredPowerW` wird **nicht** dupliziert. Eine neue
  Formel für `condition → Widerstand` (Spannungsabfall-Modifikation in `PowerGrid`) ist **erlaubt**
  — Regel 2 gilt für die **Fahrphysik**, nicht für elektrisches Netz-Verhalten. Aber: der Simulator
  ruft weiterhin `Physics.requiredPowerW` (P1-Watchpunkt bleibt intakt, P3-Watchpunkt bleibt
  unverletzt).
- **Regel 5 (Verschleiß bestraft Nutzung, nicht Existenz):** Eine ungenutzte Kante hat
  `condition` unverändert. Testbar als Invariante in der Verschleiß-Akkumulator-Stufe.
- **Regel 3 (`MaintenanceSupply` braucht 2 Implementierungen):** Die zweite (`DepotStock`, P5)
  wird in Javadoc und `ARCHITECTURE.md` benannt, nicht implementiert.
- **Regel 6 (Reparatur ist ab Tag 1 automatisierbar):** Der Reparatur-Vorgang selbst ist ein
  Mutator-Aufruf (`Edge.repairRail/repairOverhead`), kein "Spieler klickt 6000 Blöcke an".
  `PlayerLabor.withdraw(n)` ist die **zeitintensive** Komponente — wer repariert, braucht Zeit.

---

## Schritt-Sequenz

> Jeder Schritt ist einzeln committbar. Format: `<scope>: <imperative>` (Plan §11).
> Commit ⇒ Note-Update: Statuszeile + `## Session stopped` in `phase2/CLAUDE.md` im selben Commit.
> TDD in `train-core`: ein Subtask ist nicht fertig, bevor `gradle :train-core:test` grün ist.

---

### Step 0 — Altlasten (Namensdrift, tote Verweise, Reste aus Vorphasen)

> Aus `docs/plans/PHASE1_HANDOVER.md` (P1 → P2) und frischem `git status`-Scan. Drei Dinge haben
> sich seit dem letzten committeten Stand gedriftet oder sind uncommitted.

#### Step 0.1 — Drift-Commits: CLAUDE.md, ARCHITECTURE.md, README.md, phase1/*-Updates

**Dateien (modified, uncommitted):**
- `CLAUDE.md` (Root) — `updated:` 2026-07-21 → 2026-07-23
- `ARCHITECTURE.md` — `updated:` 2026-07-14 → 2026-07-23
- `README.md` — Phasenstatus als Tabelle + Versions-Pinning um jqwik/JUnit erweitert + `updated:` 2026-07-23
- `phase1/CLAUDE.md` — `updated:` 2026-07-21 → 2026-07-23
- `phase1/SESSIONS_ARCHIVE.md` — `updated:` 2026-07-21 → 2026-07-23

**Was:** Diese Updates sind Nachzügler aus dem P1-Handover (Commit `d9e3d61` syncte nur teilweise —
diese fünf Dateien blieben lokal uncommitted). Der Inhalt ist bereits durch das Handover gerecht-
fertigt; sie sind konsistent mit der Code-Wahrheit (P1 ist abgeschlossen). **Anweisung:** committen
in einem Schritt, nicht zerlegen. Inhalt ungeprüft übernehmen (Handover hat sie formuliert, der
Agent entscheidet nicht neu).

**Typnamen:** keine (Doku-Drift)
**Testliste:** keine
**Akzeptanzkriterien:**
- [ ] Alle fünf Dateien sind committed
- [ ] `git status` zeigt sie nicht mehr als modified
- [ ] Inhalt unverändert gegenüber dem Stand, der im P1-Hafter (`docs/plans/PHASE1_HANDOVER.md`,
      Commit `d9e3d61`) beschrieben ist — keine inhaltliche Mit-Korrektur
- [ ] Commit-Message: `docs: sync P1-abschluss drift (CLAUDE/ARCHITECTURE/README/phase1/*)`

**Zielzuordnung:** — (Infrastruktur)
**Kategorie:** — (Infrastruktur)

#### Step 0.2 — P1-Trials nachtragen (Operator-Aktion, Agent liefert Rohdaten)

> P1-Handover Abschnitt "Rohdaten für die trials.jsonl-Zeile(n) — an Nikinger, nicht in die Datei".
> P1-Trials sind nicht in `m1/trials.jsonl` (nur die 4 P0-Zeilen existieren, `wc -l` = 4).

**⚠ Harte Grenze:** Der Agent schreibt **NIE** in `m1/trials.jsonl` (M1_PREREGISTRATION §3
Buchführung, Plan §7). `edit: m1/**` bleibt auf `ask`/`deny`.

**Ablauf:**
1. Der Agent liest die Rohdaten aus `docs/plans/PHASE1_HANDOVER.md` Abschnitt "Rohdaten für die
   trials.jsonl-Zeile(n) — an Nikinger, nicht in die Datei" (eine aggregierte P1-Zeile, Kategorie A).
2. Der Agent übergibt Nikinger die exakte JSONL-Zeile als Text.
3. Nikinger bestätigt und gibt dem Agenten die Befehle, um die Zeile in `m1/trials.jsonl`
   einzutragen und zu committen. **Der Agent trägt nur ein, was Nikinger bestätigt hat.**
4. Commit-Message: `m1: backfill P1 trial (operator-confirmed entry)`.

**Typnamen:** keine (Messdaten)
**Testliste:** keine
**Akzeptanzkriterien:**
- [ ] Rohdaten aus Handover an Nikinger übergeben (als Text im Chat)
- [ ] Nikinger bestätigt die Zeile
- [ ] `m1/trials.jsonl` enthält 5 Zeilen (P0.1, P0.2, P0.3, P0.4, P1) — eingetragen vom Agenten
      nach Nikingers Bestätigung
- [ ] Commit erfolgt

**Zielzuordnung:** — (Messung, nicht A/B)
**Kategorie:** — (Messinstrument)

---

### Step 0b — Doc-Drift

> Code gegen .md verifizieren. Code ist Wahrheit. Doku fixen, Historie nie umschreiben.

**Status zu prüfen:**
- `CLAUDE.md` Phasenstatus-Tabelle: P0 ✅, P1 ✅, P2 ⏳ "nächster Schritt" — **korrekt, kein Drift**.
  Wird in Step 1 aktualisiert.
- `docs/INDEX.md`: listet `PHASE1_HANDOVER.md` und `PHASE1_PLAN.md`. `PHASE2_PLAN.md` wird in
  Step 1 ergänzt (dieser Commit) — One-Liner im selben Commit.
- `phase1/CLAUDE.md` Session-stopped-Block: P1 abgeschlossen, P2 als Next — **korrekt, kein Drift**.
- `ARCHITECTURE.md` / `ROADMAP.md`: Stubs, verweisen auf Overall Plan — **kein Drift**.
- P2-spezifische Frage: `ARCHITECTURE.md` listet die zwei Ports mit Beispiel-Implementierungen
  (`PowerSupply`: `ManualGenerator` heute, `IndustrialGrid` später — **wird in P2 Realität**;
  `MaintenanceSupply`: `PlayerLabor` heute, `DepotStock` später — **wird in P2 Realität**). Der
  Stub ist jetzt ehrlich: die Implementierungen existieren. Keine Drift-Anpassung nötig.

**Akzeptanz:** Step 0b ist leer (keine Drift gefunden). ✅ Darf leer sein, nicht fehlen.

---

### Step 1 — `phase2/CLAUDE.md` als allererste Phasen-Aktion

> Plan §11: "CLAUDE.md als erste Aktion jeder Phase — nie nachgelagert."
> Plan §9 Anti-Pattern: "Eine Phase ohne CLAUDE.md als allererste Aktion."
> DOC_LAYERS_CONVENTION "Neue Phase beginnen": `phase2/` anlegen, `CLAUDE.md` schreiben.

**Dateien:**
- `phase2/CLAUDE.md` — Header-Card + Build-Log (leer bis auf Step 0) + erster
  `## Session stopped`-Block am Ende der ersten P2-Session
- `phase2/SESSIONS_ARCHIVE.md` — leer anlegen (bis zur ersten Rotation)
- `phase2/README.md` — die menschliche Oberfläche der Phase (Pflicht ab P1, Pflicht in P2)
- `docs/INDEX.md` — One-Liner für `phase2/CLAUDE.md`, `SESSIONS_ARCHIVE.md`, `README.md`,
  sowie für `docs/plans/PHASE2_PLAN.md` (erstellt in diesem Schritt — der Plan existiert ab jetzt)
- `CLAUDE.md` (Root) — Phasenstatus-Tabelle: P2-Zeile von ⏳ auf � aktualisieren, down-Link auf
  `phase2/CLAUDE.md`. Außerdem: down-Link auf `docs/plans/PHASE2_PLAN.md` ergänzen (analog zu P0/P1).
- `ROADMAP.md` — P2-Zeile auf 🔄 aktualisieren (analog zu README.md / CLAUDE.md Sync). Status-
  Symbol-Spiegelung über die drei Top-Level-Dokus ist Convention; wenn der Agent es für Noise hält,
  kann er es auf einen reduzieren — aber dann in Step 0.1 dokumentieren, nicht stillschweigend.

**Typnamen:** keine (docs-only)
**Testliste:** keine
**Akzeptanzkriterien:**
- [ ] `phase2/CLAUDE.md` existiert mit Header-Card (≤15 Zeilen YAML)
- [ ] `phase2/SESSIONS_ARCHIVE.md` existiert (leer oder mit Hinweis "leer bis zur ersten Rotation")
- [ ] `phase2/README.md` existiert mit Header-Card und Kurzzusammenfassung von P2 (analog zu
      `phase1/README.md`)
- [ ] `docs/INDEX.md` hat One-Liner für `phase2/CLAUDE.md`, `SESSIONS_ARCHIVE.md`, `README.md`,
      und für `docs/plans/PHASE2_PLAN.md`
- [ ] Root-`CLAUDE.md` Phasenstatus-Tabelle: P2 = 🔄 aktiv, down-Link auf `phase2/CLAUDE.md`
      und `docs/plans/PHASE2_PLAN.md`
- [ ] Alle im selben Commit
- [ ] Commit-Message: `docs: create phase2/CLAUDE.md (P2 start)`

**Zielzuordnung:** — (Infrastruktur)
**Kategorie:** — (Infrastruktur)

---

### Step 2 — `Edge` um Verschleiß-Zustand erweitern (Z6, T-D25, T-D26)

> T-D25: Zwei Conditions pro Edge — `railCondition` + `overheadCondition`, je `double ∈ [0,1]`,
> Default 1.0. T-D26: Zwei Mutatoren — `repairRail(amount)` und `repairOverhead(amount)`. Die
> Verschleiß-Senkung selbst ist Step 3; hier geht es nur um den Zustand + die Reparatur.
>
> Achtung: `Edge` ist ein **Record** (P1-Architektur-Entscheidung). Records sind per Definition
> unveränderlich. Zwei Optionen:
> (a) `Edge` bleibt Record, aber die Condition-Komponenten werden **mutable Referenzen**
>     (z.B. `double[]` als Wrapper) — hässlich,Anti-Pattern.
> (b) `Edge` bleibt Record für Identität, aber die Conditions werden **nicht** Record-Komponenten
>     — sie leben in einem mutable Sidecar (z.B. `Edge` ist Record, ein `Map<EdgeId, EdgeCondition>`
>     im `RailGraph`). Sauberer, aber komplexer.
> (c) `Edge` wird von Record zu **final class** mit mutable Conditions — Rekord-Auflösung.
>
> Der Plan lässt die Wahl dem Agenten — Akzeptanz ist: zwei Conditions ∈ [0,1], Default 1.0,
> zwei Reparatur-Mutatoren, Invarianten durchgesetzt. Der Agent dokumentiert seine Wahl in der
> Edge-Javadoc.

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/Edge.java` — um Verschleiß-Zustand erweitert
- `train-core/src/test/java/de/traktion/traincore/EdgeTest.java` — Tests für die Erweiterung

**Typnamen:** `Edge` (erweitert), ggf. `EdgeCondition` oder ein interner Record/Helper (Agent entscheidet)

**Testliste (TDD, Z6-Vorbereitung + T-D25/T-D26):**
- `Edge` mit zwei Conditions konstruieren (oder Default = 1.0)
- Beide Conditions ∈ [0,1] (Invarianten: Konstruktor wirft bei Werten außerhalb)
- Default = 1.0 (neue Kante ist perfekt)
- `repairRail(amount)` erhöht `railCondition` (innerhalb [0,1])
- `repairOverhead(amount)` erhöht `overheadCondition` (innerhalb [0,1])
- `repairRail` übersteigt nicht 1.0 (Clamping)
- `repairOverhead` übersteigt nicht 1.0 (Clamping)
- `repairRail(repairOverhead-state)` ändert `overheadCondition` nicht (Trennung der Ströme)
- `repairOverhead(...)` ändert `railCondition` nicht (Trennung der Ströme)
- Optional (jqwik): Property-Test — für gültige `(amount, current)`-Paare gilt
  `newCondition ∈ [current, 1.0]` und Clamping an beiden Grenzen

**Akzeptanzkriterien:**
- [ ] `Edge` hat zwei Condition-Werte (`railCondition`, `overheadCondition`), beide `double ∈ [0,1]`
- [ ] Default = 1.0 (neue Kante ist perfekt — keine Drift zur bisherigen Annahme "frisch gebaut")
- [ ] Mutatoren `repairRail(amount)` und `repairOverhead(amount)` existieren (T-D26)
- [ ] Clamping: Conditions bleiben in [0,1]
- [ ] Reparatur eines Condition-Werts lässt den anderen unverändert (T-D25 Trennung)
- [ ] Kein `net.minecraft.*`-Import, kein NBT, kein `ItemStack` (Anti-Pattern-Check)
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: extend Edge with rail/overhead condition (Z6 prep, T-D25/T-D26)`

**Zielzuordnung:** Z6 (Vorbereitung — vollständige Z6-Erfüllung mit Verschleiß-Akkumulator, Step 3)
**Kategorie:** A

---

### Step 3 — Verschleiß-Akkumulator (`Wear`) und Integration in den Simulator (Z6, T-D31, Regel 5)

> T-D4: "`wear += f(masse, v)` pro Durchfahrt". T-D31: pro Substep, kontinuierlich, funktional in
> `(mass, speed, dt)`, gesäter Random für eventuelle stochastische Komponente.
> Regel 5: Verschleiß bestraft Nutzung, nicht Existenz. Testbar als Invariante: ein Simulator
> ohne Token / mit stehendem Token verändert `condition` nicht.
>
> Plan §3 / T-D5: "`condition ∈ [0,1]` → Widerstand ↑ → Spannungsabfall ↑". Die Wirkungs-Kette
> ist dreistufig:
> (1) `Wear` senkt `condition` pro Substep (dieser Step),
> (2) `PowerGrid.availableW` modelliert den erhöhten Spannungsabfall (Step 4),
> (3) `Simulator` sieht weniger `availW` → langsamere Geschwindigkeit (Step 4/5).
>
> Der Verschleiß-Wert pro Substep ist nicht-trivial — die Formel ist physikalisch. Der Plan
> lässt sie dem Agenten (Akzeptanz: monoton in `mass` und `speed`, positiv wenn beide > 0,
> Null wenn `mass == 0` oder `speed == 0`, dt skaliert).

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/Wear.java` — die Verschleiß-Funktion
- `train-core/src/main/java/de/traktion/traincore/Simulator.java` — erweitert um Verschleiß-Aufruf
  pro Substep (P1-Datei, modifiziert)
- `train-core/src/test/java/de/traktion/traincore/WearTest.java` — Tests für die Verschleiß-Funktion
- `train-core/src/test/java/de/traktion/traincore/SimulatorTest.java` — erweitert um Verschleiß-
    Verhalten (P1-Datei, modifiziert; T-D24 Determinismus muss weiterhin gelten)

**Typnamen:** `Wear` (neu — Utility-Klasse analog zu `Physics`, statische Methode)

**Testliste (TDD, Z6 + T-D31 + Regel 5):**
- `Wear.accumulate(condition, mass, speed, dt, rng)` senkt `condition` um einen positiven
  Betrag (wenn `mass > 0`, `speed > 0`, `dt > 0`)
- Monoton in `mass`: größere Masse → mehr Verschleiß (bei sonst gleich)
- Monoton in `speed`: größere Geschwindigkeit → mehr Verschleiß (bei sonst gleich)
- Null-Effekt bei `mass == 0` oder `speed == 0` (Regel 5: kein Verschleiß ohne Nutzung)
- Skalierung in `dt`: doppeltes `dt` → doppelter Verschleiß (linear)
- Clamping: `condition` bleibt in [0,1]
- Simulator mit Token, der fährt → nach N Substeps ist `railCondition < 1.0` und
  `overheadCondition < 1.0` auf der Edge (Z6: messbarer Verfall)
- Simulator mit **stehendem** Token (Speed = 0) → nach N Substeps ist `condition == 1.0`
  (Regel 5 Invariante)
- Simulator mit Token, der **nicht** auf der Edge ist (kein Reservierungs-Fall — der Test
  nutzt zwei Kanten und fährt nur über eine) → die ungenutzte Kante hat `condition == 1.0`
- **Determinismus (T-D24):** Simulator läuft zweimal mit gleichem Seed → gleiche End-conditions
  auf der Edge
- Optional (jqwik): Property-Test — für beliebige `(mass, speed, dt, condition, rng)`-Kombinationen
  bleibt `condition ∈ [0,1]` und ist monoton fallend in `(mass, speed, dt)`

**Akzeptanzkriterien:**
- [ ] `Wear` existiert als Utility-Klasse (statische Methode, keine Instanzen — analog zu
      `Physics`)
- [ ] Verschleiß ist funktional in `(condition, mass, speed, dt)` und nutzt gesäten `Random`
      (T-D13, Regel 8)
- [ ] Null-Effekt bei `mass == 0` oder `speed == 0` (Regel 5)
- [ ] Simulator integriert `Wear.accumulate(...)` pro Substep auf der aktuellen Edge des Tokens
- [ ] Ungenutzte Kanten behalten `condition == 1.0` (Regel 5 Invariante als Test)
- [ ] **Determinismus bleibt grün** (T-D24, P1-Akzeptanz — kein Verfall durch Verschleiß)
- [ ] `gradle :train-core:test` grün
- [ ] Kein `net.minecraft.*`-Import
- [ ] Commit-Message: `train-core: add Wear and integrate into Simulator (Z6, T-D31, Rule 5)`

**Zielzuordnung:** Z6 (Vorbereitung — vollständige Z6 mit Spannungsabfall-Kopplung, Step 4)
**Kategorie:** A

---

### Step 4 — `PowerGrid` nutzt `condition` für Spannungsabfall (Z4 vollständig, T-D5, T-D27)

> T-D5: "`condition` → Widerstand → Spannungsabfall ↑". T-D27: `availableW` bekommt einen
> `condition`-Parameter; der Simulator leitet `min(rail, overhead)` durch. Z4 ist damit
> **abgeschlossen** (P1 hatte `f(Distanz)` ohne Condition).
>
> Konkrete Wirkungs-Kette:
> - Token fährt auf Edge X mit `railCondition = 0.5`, `overheadCondition = 0.8`
> - `Simulator` leitet `min(0.5, 0.8) = 0.5` als `condition` durch
> - `PowerGrid.availableW(requestedW, distanceMeters, 0.5, dtSeconds)` → weniger als mit
>   `condition = 1.0`
> - `Simulator` sieht weniger `availW` → langsamere Beschleunigung oder Bremsung
>
> ⚠ **P1-Signatur-Bruch:** `PowerGrid.availableW` ändert sich. Der P1-Akzeptanztest
> (`PowerGridTest`, `IntegrationTest`) ruft die alte Form. Diese Tests müssen in diesem
> Schritt mit-migriert werden — die Migration ist **Teil** der Akzeptanz. **Wichtig:** die
> alte Semantik (`condition = 1.0`) muss weiter funktionieren (rückwärtskompatibel in dem
> Sinne, dass sie den alten Test mit `condition = 1.0` reproduziert).

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/PowerGrid.java` — Signatur erweitert um
  `condition`-Parameter (P1-Datei, modifiziert)
- `train-core/src/main/java/de/traktion/traincore/Simulator.java` — leitet `min(rail, overhead)`
  der aktuellen Edge durch (P1-Datei, weiter modifiziert; siehe Step 3)
- `train-core/src/test/java/de/traktion/traincore/PowerGridTest.java` — Tests angepasst + neue
    Tests für Condition-Wirkung (P1-Datei, modifiziert)
- `train-core/src/test/java/de/traktion/traincore/IntegrationTest.java` — `condition = 1.0`
    durchreichen, damit P1-Szenarien grün bleiben (P1-Datei, modifiziert)
- `train-core/src/test/java/de/traktion/traincore/SimulatorTest.java` — Tests angepasst
    (P1-Datei, weiter modifiziert)

**Typnamen:** keine neuen Typen (Signatur-Erweiterung von `PowerGrid.availableW`)

**Testliste (TDD, Z4-Abschluss + T-D5):**
- `PowerGrid.availableW(reqW, dist, 1.0, dt)` reproduziert das P1-Verhalten (mit `FixedSupply`
  wird `reqW` geliefert — modulo Distanz-Spannungsabfall)
- `PowerGrid.availableW(reqW, dist, 0.0, dt)` liefert `0` (völlig defekte Condition = kein Strom)
  — Grenzfall
- `PowerGrid.availableW(reqW, dist, condition < 1.0, dt)` liefert **weniger** als mit
  `condition = 1.0` (T-D5: Spannungsabfall ↑)
- Monoton: `c1 > c2` → `availW(c1) >= availW(c2)` (größere Condition → mehr oder gleich Leistung)
- Bei Distanz 0 und `condition = 1.0`: `availW = reqW` (volle Leistung am Unterwerk)
- Bei Distanz 0 und `condition = 0.5`: `availW` reduziert (Spannungsabfall durch Condition)
- Integration: Token auf einer stark degradierten Kante (`condition = 0.1`) ist deutlich
  langsamer als auf einer frischen (`condition = 1.0`) — **das ist die Wirkungs-Kette**
- `Simulator` leitet `min(rail, overhead)` korrekt durch (Test: zwei Kanten mit unterschiedlichen
  Rail/Overhead-Verhältnissen, der schlechtere Wert dominiert)
- **Determinismus bleibt grün** (T-D24 — kein Verfall durch Signatur-Wechsel)

**Akzeptanzkriterien:**
- [ ] `PowerGrid.availableW` hat einen neuen `condition`-Parameter (T-D27)
- [ ] Bei `condition = 1.0` ist das Verhalten identisch zu P1 (Rückwärtskompatibilität im
      `1.0`-Fall)
- [ ] Bei `condition < 1.0` wird `availW` reduziert (T-D5, Z4 vollständig)
- [ ] Monotonie: größere `condition` → größeres oder gleiches `availW`
- [ ] `Simulator` leitet `min(rail, overhead)` der aktuellen Edge als `condition` durch
- [ ] Integration: Token auf degradiertem Netz ist langsamer als auf frischem Netz (Wirkungs-
      Kette komplett)
- [ ] **Determinismus (T-D24) bleibt grün** — auch P1-Tests bleiben grün (mit `condition = 1.0`)
- [ ] `gradle :train-core:test` grün
- [ ] Kein `net.minecraft.*`-Import
- [ ] Commit-Message: `train-core: PowerGrid uses condition for voltage drop (Z4 complete, T-D5)`

**Zielzuordnung:** Z4 (abgeschlossen), Z6 (Wirkungs-Kette)
**Kategorie:** A

---

### Step 5 — `MaintenanceSupply` Port + `PlayerLabor` (Z7-Infrastruktur, T-D28, T-D29)

> T-D28: `MaintenanceSupply` ist der zweite Port (Plan §3.2). Die erste Produktions-Implementierung
> ist `PlayerLabor` (heute); die zweite (`DepotStock`, P5) wird in Javadoc benannt.
> T-D29: `PlayerLabor` ist ein Zeit-Akkumulator. `withdraw(n)` zieht aus einem endlichen Vorrat
> `workAvailable`; pro Tick (oder pro `tick(dt)`-Aufruf) wird der Vorrat aufgefüllt bis zu
> `maxWork`. Das modelliert "echten Preis (Zeit)" — der Spieler hat unbegrenzte Ausdauer, aber
> Zeit ist der Engpass.
>
> ⚠ Regel 3: `MaintenanceSupply` braucht **zwei heute benennbare Implementierungen**. Die erste
> (`PlayerLabor`) wird implementiert; die zweite (`DepotStock`) wird in der Javadoc des Interfaces
> benannt und in `ARCHITECTURE.md` ergänzt (analog zu T-D22 für `PowerSupply`).
>
> ⚠ **Schritt-Schnitt:** `PlayerLabor` braucht den Simulator, um die Zeit voranzutreiben. Die
> Integrations-Schritte (Simulator treibt `PlayerLabor`) sind Teil von Step 7 (Z7-Invariantentest).
> Hier geht es um die **Klassen und Verträge**, nicht um die Simulator-Integration.

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/MaintenanceSupply.java` — Interface (Port 2)
- `train-core/src/main/java/de/traktion/traincore/PlayerLabor.java` — Implementierung
- `train-core/src/test/java/de/traktion/traincore/MaintenanceSupplyTest.java` — Tests
    (auch für `PlayerLabor`)
- `ARCHITECTURE.md` — Port-2-Abschnitt: `PlayerLabor` ist implementiert, `DepotStock` benannt
    (P2-Commit)

**Typnamen:** `MaintenanceSupply` (Interface), `PlayerLabor`

**Testliste (TDD, T-D28/T-D29):**
- `MaintenanceSupply.withdraw(n)` zieht bis zu `n` aus dem Vorrat, gibt tatsächlich entnommene
  Menge zurück (Vertrag: "Entnimmt bis zu `requested`. Gibt zurück, wie viele es wurden." — Plan
  §3.2)
- `MaintenanceSupply.withdraw(0)` gibt `0` zurück, ändert nichts
- `PlayerLabor` startet mit `workAvailable = 0` (Spieler hat noch nicht gearbeitet)
- `PlayerLabor.tick(dtSeconds)` oder äquivalenter Aufruf erhöht `workAvailable` um
  `ratePerTick * dt` (gedeckelt bei `maxWork`)
- `PlayerLabor` respektiert `maxWork` (Clamping: nicht über das Maximum auffüllen)
- `PlayerLabor.withdraw(n)` reduziert `workAvailable` um `min(n, workAvailable)` und gibt
  diese Menge zurück
- Mehrfaches `withdraw` ohne zwischenzeitliches `tick` ist begrenzt durch das vorhandene Budget
  (zweites `withdraw` nach leerem Vorrat gibt `0` zurück)
- Optional (jqwik): Property-Test — für gültige Sequenzen von `(tick, withdraw)` ist
  `workAvailable ∈ [0, maxWork]` und die Summe `Σ entnommen + aktueller Vorrat + Δ seit Start`
  ist durch `ratePerTick * elapsedTime` gedeckelt
- Optional (jqwik): Property-Test — ohne `tick` ist `workAvailable` konstant (Zeit vergeht nicht
  "von allein" in deterministischen Tests)

**Akzeptanzkriterien:**
- [ ] `MaintenanceSupply` Interface existiert mit dem Vertrag aus Plan §3.2 (analog zu
      `PowerSupply`)
- [ ] Javadoc benennt **zwei Implementierungen**: `PlayerLabor` (heute, P2) + `DepotStock`
      (später, P5). Damit ist Regel 3 erfüllt.
- [ ] `PlayerLabor` implementiert `MaintenanceSupply` und ist **kein Stub** (Plan §3.2)
- [ ] `PlayerLabor` modelliert **Zeit als Engpass**: `withdraw(n)` ist nicht-instant; der Vorrat
      wird durch `tick(dt)` o.ä. aufgefüllt
- [ ] `withdraw(n)` zieht maximal `n`, weniger wenn nichts da
- [ ] `workAvailable ∈ [0, maxWork]` (Clamping)
- [ ] `ARCHITECTURE.md` Port-2-Abschnitt reflektiert den neuen Stand (eine Implementierung
      real, eine benannt)
- [ ] `gradle :train-core:test` grün
- [ ] Kein `net.minecraft.*`-Import
- [ ] Commit-Message: `train-core: add MaintenanceSupply port + PlayerLabor (T-D28/T-D29, Rule 3)`

**Zielzuordnung:** Z7 (Vorbereitung — der eigentliche Invariantentest kommt in Step 7)
**Kategorie:** A

---

### Step 6 — `ManualGenerator` (Port 1, zweite Produktions-Implementierung, T-D30)

> T-D30: `ManualGenerator` ist die zweite produktive `PowerSupply`-Implementierung (P1 hatte
> `FixedSupply` als Test-Stub). "Fester Output, Brennstoff von Hand — dauerhafte Rückfallebene
> (Regel 4)". Damit ist Regel 3 für Port 1 vollständig erfüllt: `FixedSupply` (Test) +
> `ManualGenerator` (Produktion).
>
> Inhaltlich: `ManualGenerator` hat einen **endlichen Brennstoff-Vorrat** (z.B. `fuelAmount`).
> `supply(reqW, dt)` liefert höchstens `min(reqW, maxOutputW * dt)` UND höchstens
> `fuelAmount / dt` (oder eine andere sinnvolle Skalierung — Agent entscheidet). Der Vorrat
> wird durch Weltinteraktion (P4) oder einen expliziten `refuel(...)`-Test-Hook aufgefüllt.
> Akzeptanz ist: `PowerSupply`-Vertrag eingehalten, endlicher Vorrat, kein `null`-Output, kein
> Hardcoding.

**Dateien:**
- `train-core/src/main/java/de/traktion/traincore/ManualGenerator.java` — Implementierung
- `train-core/src/test/java/de/traktion/traincore/ManualGeneratorTest.java` — Tests

**Typnamen:** `ManualGenerator`

**Testliste (TDD, T-D30):**
- `ManualGenerator` mit Brennstoff-Vorrat konstruieren (z.B. `new ManualGenerator(maxOutputW,
  fuelAmount)`)
- `supply(reqW, dt)` liefert höchstens `reqW` (Vertrag)
- `supply(reqW, dt)` liefert höchstens `maxOutputW * dt` (Hardware-Limit)
- `supply(reqW, dt)` verbraucht Brennstoff (Vorrat nimmt ab)
- `supply(reqW, dt)` mit leerem Vorrat liefert `0` (kein Brennstoff, kein Strom)
- `refuel(amount)` erhöht den Vorrat (Test-Hook; P4 nutzt das für Weltinteraktion)
- Monotonie: größerer `reqW` → größere oder gleiche Lieferung (solange Brennstoff reicht)
- Integration mit `PowerGrid`: `PowerGrid(manualGenerator)` funktioniert wie mit
  `FixedSupply` (modulo Brennstoff)

**Akzeptanzkriterien:**
- [ ] `ManualGenerator` implementiert `PowerSupply` mit dem Plan-§3.2-Vertrag
- [ ] Hat einen endlichen Brennstoff-Vorrat (nicht unendlich — sonst ist es ein Stub)
- [ ] Liefert `0`, wenn der Vorrat leer ist
- [ ] `refuel(amount)` als Test-/Weltinteraktions-Hook (P4 nutzt das)
- [ ] `gradle :train-core:test` grün
- [ ] Kein `net.minecraft.*`-Import
- [ ] Commit-Message: `train-core: add ManualGenerator (T-D30, Port 1 second implementation)`

**Zielzuordnung:** — (Voraussetzung für P4-Weltinteraktion, Regel 3-Abschluss für Port 1)
**Kategorie:** A

---

### Step 7 — Z7-Bootstrap-Invariante (Property-based Test, T-D32)

> T-D32: Aus **jedem** erreichbaren Verfallszustand führt eine endliche Sequenz von
> `withdraw(n) → repairRail(n) + repairOverhead(n)` zu `condition > 0` auf jeder Kante. Das
> beweist: der Spieler hat **immer** einen Ausweg (Regel 4 — kein Softlock).
>
> Der Test ist **property-based** mit jqwik (T-D20): Generator erzeugt zufällige
> Verfallszustände (`condition ∈ [0, 1)` auf allen Kanten), der Reparatur-Loop reduziert die
> Summe der `(1 - condition)` monoton, terminiert.
>
> ⚠ **jqwik-Verfügbarkeit:** T-D20 pinnt jqwik 1.9.0 (in P1 aktiviert und verifiziert). Der
> Property-Test nutzt die vorhandene Bibliothek.

**Dateien:**
- `train-core/src/test/java/de/traktion/traincore/SoftlockInvariantTest.java` — der
    property-based Test

**Typnamen:** keine (nur Test)

**Testliste (TDD, Z7 + T-D32):**
- Property-Test (jqwik): für jeden zufällig generierten Verfallszustand (`condition ∈ [0, 1)`
  auf allen Kanten eines kleinen Test-Netzes) und jede gültige `PlayerLabor`-Konfiguration:
  - Iteriere `withdraw(n) → repairRail(n) + repairOverhead(n)` auf einer Kante
  - Treibe die Zeit weiter (`PlayerLabor.tick(dt)`)
  - Invariante: die Summe der `(1 - condition)` über alle Kanten ist monoton fallend
  - Termination: nach endlich vielen Schritten ist `condition > 0` auf der Ziel-Kante
- Spezialfall: `PlayerLabor` mit unrealistisch niedrigem `maxWork` und `ratePerTick` —
  Invariante hält trotzdem (nur dauert es länger)
- Spezialfall: ein Token fährt während der Reparatur — `condition` kann gleichzeitig steigen
  (durch Reparatur) und fallen (durch Verschleiß) — Netto-Effekt muss positiv sein, wenn der
  Spieler schneller repariert als der Token verschleißt
- Optional (klassischer JUnit-Test): ein **deterministischer** Worst-Case-Test mit bekannten
  Werten — "von `condition = 0.001` auf `condition > 0` in N Schritten" — gibt der
  Property-Genauigkeit einen Anker

**Akzeptanzkriterien:**
- [ ] Property-Test läuft mit jqwik (mindestens 100 generierte Fälle)
- [ ] Kein Fall verletzt die Invariante: `condition > 0` erreichbar aus jedem Start-Zustand
- [ ] Termination: der Reparatur-Loop terminiert (nicht "unendlich reparieren")
- [ ] Bei gleichem Seed: zwei Läufe liefern gleiche Ergebnisse (T-D24, Regel 8)
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: add Z7 bootstrap-invariant property test (T-D32, Rule 4)`

**Zielzuordnung:** Z7
**Kategorie:** A

---

### Step 8 — Z6-Langlauf-Sim (10.000 Ticks Dauerbetrieb, T-D33)

> Plan §5/P2 Done-When: "Ein Langlauf über 10.000 Ticks Dauerbetrieb degradiert messbar und
> blockiert nie total." T-D33 spezifiziert das als Test: nach 10k Ticks ist `condition < 1.0`
> messbar, aber `speedMps > 0` immer noch (nicht blockiert). T-D5: "kontinuierlich, nie
> blockierend".
>
> Das ist **die** Z6-Akzeptanz. Der Test muss deterministisch sein (T-D13, Regel 8) und
> reproduzierbar.

**Dateien:**
- `train-core/src/test/java/de/traktion/traincore/WearIntegrationTest.java` — der Langlauf-Test
    (oder als Erweiterung in `IntegrationTest.java` — Agent entscheidet; Akzeptanz ist "10k
    Ticks getestet", nicht der Dateiname)

**Typnamen:** keine (nur Test)

**Testliste (TDD, Z6-Akzeptanz + T-D33):**
- Baue ein kleines Netz (3–5 Kanten, `RailKind.NORMAL`)
- Setze einen Token auf die erste Kante mit genug `maxPowerW`, damit der Zug nicht durch reine
  Stromknappheit stoppt (sonst ist der Test nicht aussagekräftig — Verschleiß, nicht Strom,
  soll die Bremse sein)
- Lasse den Simulator 10.000 Ticks laufen
- Behauptung 1: nach 10k Ticks ist `railCondition < 1.0` auf mindestens einer Kante (Z6:
  **messbarer** Verfall)
- Behauptung 2: `speedMps > 0` (T-D5: **nicht blockiert**)
- Behauptung 3 (optional): die End-Geschwindigkeit ist kleiner als auf einer frischen Kante
  (T-D5: "Fahrzeit ↑" — gemessen als Geschwindigkeit ↓ bei gleichem Stromangebot)
- **Determinismus (T-D24):** zwei Läufe mit gleichem Seed → gleiche End-conditions und
  gleiche End-Geschwindigkeit (Toleranz wie P1: 1e-9)
- Optional (jqwik): Property-Test mit verschiedenen Seeds und Netz-Topologien — alle halten
  die Invarianten

**Akzeptanzkriterien:**
- [ ] 10.000 Ticks laufen in akzeptabler Zeit (Simulator-Performance für diesen Test nicht
      relevant — P1-Performance als Anker)
- [ ] `railCondition < 1.0` (oder `overheadCondition < 1.0`) messbar nach 10k Ticks
- [ ] `speedMps > 0` immer noch (nicht total blockiert — T-D5)
- [ ] Determinismus (T-D24) hält
- [ ] `gradle :train-core:test` grün
- [ ] Commit-Message: `train-core: add Z6 long-run simulation (10k ticks, T-D33)`

**Zielzuordnung:** Z6 (Akzeptanz)
**Kategorie:** A

---

### Step 9 — Done-When-Verifikation und Phasen-Abschluss

> Plan §5/P2 Done-When: "Z6 + Z7 grün. Ein Langlauf über 10.000 Ticks Dauerbetrieb degradiert
> messbar und blockiert nie total."
>
> Was:
> 1. `gradle :train-core:test` läuft und ist grün (alle Tests aus Step 2–8 + alle P1-Tests).
> 2. Abhängigkeits-Check: `train-core/build.gradle.kts` hat weiterhin nur
    `testImplementation`-Abhängigkeiten (JUnit, jqwik). Keine Runtime-Abhängigkeiten.
> 3. Anti-Pattern-Check (§9):
>    - `grep -r "net.minecraft" train-core/src/` → leer (nur `package-info.java`-Kommentar)
>    - `grep -r "NBT\|ItemStack" train-core/src/` → leer
>    - `grep -r "System.out\|System.err" train-core/src/main/` → leer
>    - Kein `HashSet` in der Physikschleife (Simulator)
>    - Keine Wall-Clock (`System.currentTimeMillis`, `Instant.now`) in der Physikschleife
>    - `Physics.requiredPowerW` existiert genau **einmal** (Regel 2 — kein Duplikat)
>    - `Wear`/`condition`/`repair*`/`MaintenanceSupply`/`PlayerLabor` haben **keine**
>      `net.minecraft.*`-Importe
>    - `ManualGenerator` und `PlayerLabor` sind **keine** Stubs (Plan §3.2)
> 4. Determinismus-Check: zwei Simulator-Läufe mit gleichem Seed → gleiche End-conditions und
    gleiche End-Geschwindigkeit (T-D24, bereits in Steps 4/7/8 getestet — hier nur bestätigt).
> 5. `phase2/CLAUDE.md` Build-Log aktualisieren: alle Steps als ✅.
> 6. `## Session stopped`-Block in `phase2/CLAUDE.md` schreiben (P2 abgeschlossen).
> 7. Root-`CLAUDE.md` Phasenstatus: P2 ✅, P3 ⏳ nächster Schritt.
> 8. `docs/plans/PHASE2_HANDOVER.md` schreiben (analog zu `PHASE1_HANDOVER.md`).

**Dateien:**
- `train-core/` (Tests + Source-Dateien — keine neuen Dateien in diesem Step)
- `phase2/CLAUDE.md` — Build-Log + Session-stopped-Block
- `CLAUDE.md` (Root) — Phasenstatus-Tabelle
- `ROADMAP.md` — P2-Zeile auf ✅
- `README.md` — P2-Zeile auf ✅
- `docs/plans/PHASE2_HANDOVER.md` — P2-Abschlussanalyse (analog zu PHASE1_HANDOVER.md)
- `docs/INDEX.md` — One-Liner für `PHASE2_HANDOVER.md`
- `phase2/SESSIONS_ARCHIVE.md` — der bisherige Session-stopped-Block rotiert hierher (verbatim,
  newest-first)

**Testliste:** keine (nur Verifikation + Doku-Updates)

**Akzeptanzkriterien:**
- [ ] `gradle :train-core:test` grün (P1-Tests + neue P2-Tests, alle 101+ Tests)
- [ ] `train-core` hat null externe Runtime-Abhängigkeiten (nur Test-Libs)
- [ ] Alle §9-Anti-Pattern-Checks leer (siehe oben)
- [ ] Determinismus bestätigt (T-D24, T-D33)
- [ ] `Physics.requiredPowerW` existiert genau einmal (Regel 2 — kein Duplikat)
- [ ] `PlayerLabor` und `ManualGenerator` sind echte Implementierungen (kein Stub)
- [ ] `phase2/CLAUDE.md` Build-Log vollständig, Session-stopped-Block geschrieben
- [ ] Root-`CLAUDE.md` Phasenstatus aktualisiert
- [ ] `docs/plans/PHASE2_HANDOVER.md` geschrieben, mit Rohdaten für `m1/trials.jsonl`
- [ ] `docs/INDEX.md` One-Liner für `PHASE2_HANDOVER.md`
- [ ] Commit-Message: `docs: close P2 (Z6 + Z7 green, condition model, ports complete)`

**Zielzuordnung:** Z6, Z7 (alle), Regel 2, Regel 4, §9
**Kategorie:** A

---

## Done-When (Plan §5/P2)

- [ ] `condition ∈ [0,1]` auf Kante und Oberleitung (T-D25 — `railCondition` + `overheadCondition`)
- [ ] `wear += f(masse, v)` pro Substep (T-D4, T-D31)
- [ ] `condition` → Widerstand → Spannungsabfall (T-D5, schließt Z4 ab — T-D27)
- [ ] `PowerSupply` / `ManualGenerator` (T-D30 — Regel 3 vollständig für Port 1)
- [ ] `MaintenanceSupply` / `PlayerLabor` (T-D28, T-D29 — Regel 3 für Port 2, kein Stub)
- [ ] **Z7-Invariantentest** grün (Step 7, T-D32 — property-based mit jqwik)
- [ ] **Z6-Langlauf-Sim 10.000 Ticks** grün (Step 8, T-D33 — messbarer Verfall, nicht blockiert)
- [ ] `train-core` hat null externe Abhängigkeiten außer Test-Bibliotheken (JUnit, jqwik)
- [ ] Zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse (Regel 8, T-D24, T-D33)
- [ ] Kein Eintrag aus §9 ist im Code (Anti-Pattern-Check in Step 9)
- [ ] `Physics.requiredPowerW` existiert genau einmal (Regel 2 — P3-Watchpunkt bleibt intakt)

**P2 ist abgeschlossen, wenn alle Steps committed und Done-When erfüllt sind.** Nächster Schritt:
P3 (Planer, Z5 — das Kern-Orakel, die interessanteste Watch-Phase) in neuer Session.

---

## Watchpunkte für die M1-Messung (Kategorie A)

> Diese Punkte werden im Trial vom Operator protokolliert (Plan §7). Der Agent soll sie nicht
> "umgehen", sondern natürlich zeigen, wie er damit umgeht.

- **Regel-2-Verstoß (ja/nein):** Dupliziert der Agent `requiredPowerW` im Simulator? In P2 gibt es
  **keine** zweite Physik-Formel — Verschleiß (`Wear.accumulate`) ist eine **andere** Formel,
  keine Duplikation. Watchpunkt: nutzt der Simulator weiterhin `Physics.requiredPowerW` (nicht
  einen eigenen "Physik-Mock")? P3-Watchpunkt bleibt aktiv.
- **Z5-Tautologie:** in P2 nicht anwendbar (kein Planer). P3.
- **Determinismus (Regel 8):** Verwendet der Agent ungesäten Zufall (`Math.random`, `new Random()`
  ohne Seed)? Wall-Clock (`System.currentTimeMillis`, `Instant.now`)? `HashSet` in der
  Physikschleife? T-D24, T-D33 testen das.
- **Interface ohne zwei Implementierungen (Regel 3):** Führt der Agent `MaintenanceSupply` ein,
  ohne die zweite Implementierung (`DepotStock`) zu benennen? Tut er das gleiche für
  `ManualGenerator` vs. `FixedSupply`? T-D28, T-D30 geben die Antwort vor.
- **Stub-Verse (Plan §3.2):** Wird `PlayerLabor` zu einem kostenlosen Stub (z.B. `withdraw(n)`
  gibt immer `n` zurück)? Das wäre ein Z6/Z7-Killer — der Plan verlangt Zeit-Akkumulator.
  T-D29 spezifiziert das.
- **Regel 5 (Verschleiß bestraft Nutzung, nicht Existenz):** Hat der Simulator einen Test, dass
  eine ungenutzte Kante `condition == 1.0` behält? Wenn nicht: der Agent hat eine Anti-Regel
  verletzt.
- **jqwik Anti-AI-Klausel (Confound §4.3):** Der Plan referenziert T-D20 (jqwik 1.9.0). Wenn der
  Agent bei einer Aktualisierungs-Recherche auf 1.10.x stößt: ignorieren, nicht testen löschen.

---

## Rohdaten für die trials.jsonl-Zeile(n) — an Nikinger, nicht in die Datei

> Plan §7: "Kein Agent schreibt in `m1/trials.jsonl`. Die Messung gehört nicht dem Gemessenen."
> Diese Rohdaten liefert der ausführende Agent am Ende von P2 als Text. Nikinger trägt ein.
>
> **Vorschlag (eine aggregierte P2-Zeile):** P2 ist eine zusammenhängende Kategorie-A-Phase
> (Verschleiß + Ports, ein Z-Durchstich). Eine Zeile pro Phase ist konsistent mit P1.

**Felder (gemäß Plan §7 / M1_PREREGISTRATION §3):**
- `phase`: P2
- `ziel`: Verschleiß-Durchstich (Z6, Z7) + Ports produktiv (Regel 3, beide Ports)
- `kategorie`: A
- `ts`: Datum des Done-When-Commits
- `iterationen`: Commits Step 0.1–9 (Domänen-Commits + Doku)
- `diff_lines`: Summe der P2-Strang-Commits (`git log --shortstat`)
- `tests_gruen`: SmokeTest + JqwikSmokeTest + alle P1-Tests + neue P2-Tests (EdgeCondition,
  Wear, Simulator mit Verschleiß, PowerGrid mit Condition, MaintenanceSupply, PlayerLabor,
  ManualGenerator, SoftlockInvariant, WearIntegration) — Agent zählt im Done-When-Schritt
- `regressionen`: 0 erwartet (P1-Tests bleiben grün, `condition = 1.0` als Rückwärtskompatibilität)
- `operator_eingriffe`: Anzahl (P2 war Kategorie A; Operator-Eingriffe nur bei Architektur-
  Korrekturen, die der Plan nicht antizipiert hat)
- `regel2_verstoss`: **nein** erwartet — `Physics.requiredPowerW` bleibt einmalig, Simulator
  ruft weiterhin auf
- `z5_tautologie`: **nein** (nicht anwendbar — kein Planer in P2)
- `recherche_schritte`: 0 erwartet (P2 ist reines Java, keine 26.2-API)
- `notiz`: Z6 + Z7 grün, beide Ports produktiv (Regel 3 erfüllt), Determinismus (T-D24, T-D33)
  bestätigt, kein §9-Verstoß

---

## Verweise

| Was | Pfad | Warum |
|---|---|---|
| Overall Plan (Wahrheit) | `TRAKTION_OVERALL_PLAN.md` | §2 Locks, §3 Hard Rules, §3.2 Ports, §4 Ziele, §5/P2, §9 Anti-Patterns |
| Preregistration (FROZEN) | `M1_PREREGISTRATION.md` | §2 Plan-Detailgrad, §3 Metriken, §4 Confounds. Nie editieren. |
| P1-Handover (Vorphase) | `docs/plans/PHASE1_HANDOVER.md` | P1-Abschluss, offene P2-Fragen, Watchpunkte |
| P1-Plan (bilanziert) | `docs/plans/PHASE1_PLAN.md` | Schritt-Sequenz, Akzeptanzkriterien, T-D20–T-D24 — Referenz für Stil |
| P0-Handover | `docs/plans/PHASE0_HANDOVER.md` | Aufräum-Schritt, trials-Rohdaten, [VERIFY]-Marken |
| Konventionen | `docs/CONVENTIONS.md` | Logging, Testmatrix, Root-Layout |
| Doc-Layers-Spec | `docs/DOC_LAYERS_CONVENTION.md` | Header-Card, Layer, Rotation |
| Build-Files | `gradle.properties`, `train-core/build.gradle.kts` | gepinnte Versionen, jqwik 1.9.0 aktiv |
| Phasen-Kopf (vom Agenten) | `phase2/CLAUDE.md` | Build-Log + Session-stopped (Step 1 erstellt) |
| Code-Wahrheit (P1-Stand) | `train-core/src/main/java/de/traktion/traincore/` | 13 Typen — P2 erweitert/ergänzt, dupliziert nicht |

---

## Session stopped

> Dieser Plan ist das Konzept/Plan-Dokument. Der `## Session stopped`-Block lebt in
> `phase2/CLAUDE.md` (Doc-Layers-Konvention), vom ausführenden Agenten geschrieben. Diese Datei
> enthält keinen Session-stopped-Block — nur den Plan.
