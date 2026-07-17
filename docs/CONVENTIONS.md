---
status: live
purpose: Welche Konventionen aus dem Referenzprojekt übernommen werden und welche bewusst verworfen sind — das Destillat, nicht das Referenz-Repo
read-when: Session-Start; vor jeder neuen .md; vor jedem Commit; bei Zweifel, ob eine Regel gilt
detail: L2
up: ./CLAUDE.md
down:
  - ./docs/DOC_LAYERS_CONVENTION.md   # generische Spec des Doc-Layers-Systems (Header-Card, Layer, Rotation)
updated: 2026-07-16
---

# Traktion — Konventionen

> **Herkunft:** Destilliert aus dem Referenzprojekt (Trading-Bot, Python) in P0.1.
> Das Referenz-Repo selbst wird nach P0 nicht mehr gelesen (Plan §5/P0.1, Anti-Contamination).
> Diese Datei ist die einzige Spur — bewusst gekürzt, bewusst selektiv.
>
> **Code ist Wahrheit** über Konzept-Dokument über Status-Prosa. [VERIFY] bleibt stehen,
> bis jemand tatsächlich verifiziert hat.
>
> **P0.2 Step 4 (2026-07-14):** Logging- und Testmatrix-Abschnitte festgeschrieben (Plan §7).

---

## Übernommen

> Konventionen, die hier gelten, weil sie das Projekt testbar, navigierbar und auditierbar machen.

- **L1-Header-Cards** — YAML-Frontmatter (≤15 Zeilen) oben in jeder lebenden `.md`.
  Gilt hier: ein Agent entscheidet aus ≤15 Zeilen, ob der Body lesenswert ist. Token-Disziplin
  bei einem langlaufenden Projekt mit vielen Doku-Dateien.

- **`docs/INDEX.md`** — L0-Karte, eine Zeile pro Doku, Glyph + Größe + read-when-Hook.
  Gilt hier: Navigationseinstieg für jede Session. Verhindert Directory-Scans.

- **`## Session stopped`** — genau ein Block pro Phasen-`CLAUDE.md`, Rotation ins Archiv.
  Gilt hier: Handover an eine kalte Folgesession. Ohne ihn startet jede Session von null.

- **`[VERIFY]`-Konvention** — alles, was gegen echte API oder As-Built-Code geprüft werden muss,
  ist markiert und bleibt markiert, bis jemand verifiziert hat.
  Gilt hier: Plan §2 hat mehrere [VERIFY]-Marken (Java-Version, PersistentState-API, jqwik).
  Der Planungschat hat sie nicht aufgelöst — die Ausführung muss es tun.

- **"Code ist Wahrheit"** — getesteter Code > Konzept-Dok > Status-Prosa. Bei Konflikt: Code
  vertrauen, Doku fixen, datierte Notiz hinterlassen.
  Gilt hier: der Plan ist Konzept-Ebene. Die Ausführung produziert die Wahrheit.

- **Commit ⇒ Note-Update** — Statuszeile + `## Session stopped` im selben Commit.
  Gilt hier: der Commit ist die atomare Einheit, die Notiz ist sein Begleittext.

- **Atomare Commits** — Format `<scope>: <imperative>`, eine logische Änderung pro Commit.
  Gilt hier: die Git-History ist das Messinstrument (M1-Strang). Große Commits verwaschen sie.

- **Vorregistrierung vor Messung** — Hypothese, Metriken, Entscheidungsregel stehen fest,
  bevor das erste Ergebnis existiert. Danach: nie editieren, nur superseded durch neues
  datiertes Dokument.
  Gilt hier: `M1_PREREGISTRATION.md` ist committed (a40e818) und frozen. Die Git-History ist
  der Beweis gegen Confirmation-Shopping.

- **Anti-Shopping-Regel** — Δ, P, Metriken, Entscheidungsregel werden nicht nachträglich
  angepasst, um ein Ergebnis freundlicher zu machen.
  Gilt hier: Plan §6 + M1_PREREGISTRATION §6 verbieten es explizit.

- **Rausch-Regel** — ist die Streuung innerhalb eines Setups größer als der Abstand zwischen
  Setups, wird der Abstand als "nicht unterscheidbar" berichtet.
  Gilt hier: Plan §6, M1_PREREGISTRATION §3. Verhindert, dass Rauschen als Signal berichtet wird.

- **Drop-Order** — unter Zeitdruck fällt zuerst, was oben steht. Nie umsortieren.
  Gilt hier: Plan §10 fixiert die Reihenfolge. Z7 (Softlock-Schutz) und P3 (Planer) fallen nie.

- **Doc-Layers-Konvention** — L0 (INDEX) → L1 (Header-Card) → L2 (Body) → L3 (Archive).
  Neue `.md` ⇒ Header-Card + One-Liner in `docs/INDEX.md` im selben Commit.
  Gilt hier: strukturiert das gesamte Doku-System. Wird in P0.2 voll ausgefaltet
  (`docs/DOC_LAYERS_CONVENTION.md` [VERIFY — noch nicht erstellt]).

- **"Read enough, then act"** — lies das Minimum, das die Layer erlauben, aber überspringe nie
  das Verständnis des Codes, den eine Änderung berührt.
  Gilt hier: verhindert sowohl überflüssiges Lesen als auch blinde Edits.

- **"Locked decisions stay locked"** — gelockte Entscheidungen werden nicht mid-Implementation
  re-litigiert. Widersprüchliche Evidenz wird als Finding gemeldet, nie stillschweigend umgesetzt.
  Gilt hier: Plan §2 (T-D1…T-D16) sind gelockt. Der Agent baut, er entscheidet nicht neu.

- **"Report outcomes faithfully"** — Tests scheitern → sagen und Output zeigen. Übersprungene
  Schritte benennen. Verifizierte Ergebnisse ohne Hedging zustehen.
  Gilt hier: die M1-Messung lebt von ehrlichen Rohdaten. Ein falsches "it works" verfälscht sie.

- **"Root cause over symptom"** — vor jedem Fix: jeden Caller des zu berührenden Objekts finden.
  An der gemeinsamen Stelle einmal fixen, nicht pro Symptom.
  Gilt hier: verhindert, dass ein Bug-Fix ein zweiter Bug wird.

- **"Write handovers for a cold reader"** — mit dem Outcome führen, keine Session-Shorthand,
  nächster Schritt spezifisch genug zum Starten ohne alles neu zu lesen.
  Gilt hier: jede Session kann die letzte sein. Der Handover überbrückt den Kontextverlust.

- **"Act vs. ask"** — reversible, in-Phase-Schritte ohne zu fragen ausführen; stoppen bei
  destruktiven, out-of-scope oder echten Scope-Changes.
  Gilt hier: balanciert Geschwindigkeit gegen Sicherheit.

- **Handover nach ~20–30 Tool-Calls** — via `## Session stopped`, dann anhalten.
  Gilt hier: Plan §11. Verhindert endlose Sessions mit abnehmender Qualität.

---

## Bewusst verworfen

> Konventionen des Referenzprojekts, die hier NICHT gelten, mit Begründung.
> Dieser Abschnitt ist der wichtigere — wenn er dünn ist, wurde nicht nachgedacht.

- **pytest-Zählungen** ("P1 52 · P2 62 · P4 94 …") — das Referenzprojekt zählt Tests pro Phase
  als Status-Indikator.
  Verworfen: Traktion verwendet Gradle/JUnit, nicht pytest. Test-Zählungen pro Phase sind hier
  kein Status-Indikator; `gradle :train-core:test` grün/rot ist die Wahrheit.

- **`requests`-Konvention** ("requests for HTTP, NOT urllib or httpx") — das Referenzprojekt
  standardisiert seine HTTP-Bibliothek.
  Verworfen: Traktion hat keinen HTTP-Client. `train-core` hat NULL externe Abhängigkeiten
  außer Test-Bibliotheken (Plan §1, §3 Regel 1). `train-mc` spricht Minecraft, nicht HTTP.

- **Keyring-Konvention** ("Credentials live ONLY in the OS keyring") — das Referenzprojekt
  lagert Credentials im OS-Keyring.
  Verworfen: Traktion hat keine Credentials. Kein Broker, keine API-Keys, kein Live-System.
  Die M1-Messung hat ihre eigene Auth-Story (NVIDIA NIM Free Tier, in der Config), die nichts
  mit dem Mod-Code zu tun hat.

- **Envelope-Kontrakt** ("Every broker adapter method returns an Envelope JSON object") —
  kanonisches Response-Format des Referenzprojekts.
  Verworfen: Traktion hat keinen Broker, keinen Adapter, kein Response-Format. Der Kern kennt
  `double`, kein JSON-Envelope (Plan §3 Regel 7).

- **Phasennummerierte Verzeichnisse** (`phase1_broker_wrapper/`, `phase2_persistence/`, …) —
  das Referenzprojekt organisiert Code nach Phasen in Top-Level-Verzeichnissen.
  Verworfen: Traktion organisiert nach Architektur-Schnitt (`train-core/` + `train-mc/`), nicht
  nach Phasen. Phasen sind zeitliche Bauabschnitte, keine Code-Struktur (Plan §1, §5).

- **Broker-/VETO-/Gate-Semantik** — das Referenzprojekt hat eine 5-Gate-Pipeline mit VETO-Kaskade.
  Verworfen: Traktion hat keine Gates, keine VETOs, keinen Broker. Die "Hard Rules" (Plan §3)
  sind anderer Natur: kein `net.minecraft.*` im Kern, Physik existiert genau einmal, kein Softlock.

- **Jede Trading-Domänenlogik** — Spread/Drift/Momentum, Bull/Bear/Judge-Debatte, Sizing,
  Reward-Score, Lessons-Loop, Scheduler-Daemon, Token-Budget.
  Verworfen: Traktion simuliert ein Schienennetz. Graph, Physik, Energie, Verschleiß, Planer,
  Fahrplan. Keine Gemeinsamkeit mit Trading außer der Methodik (vorregistrierte Messung).

- **"So wenig AI wie möglich"** — das Kernprinzip des Referenzprojekts.
  Verworfen als Prinzip, aber: Traktion hat ohnehin keine AI im Code. Die AI ist der Agent, der
  baut — nicht etwas im Mod. Das Prinzip ist hier nicht anwendbar, weil es keine AI-Komponente
  gibt, die man minimieren könnte.

- **Python-Spezifische Konventionen** — Type-Hints via `from __future__ import annotations`,
  Docstrings auf jeder öffentlichen Funktion, `logging` nach stderr, eine Datei = eine
  Verantwortung.
  Verworfen als Python-Konventionen. Die Prinzipien dahinter gelten aber analog in Java:
  TDD, keine Netzwerk-Calls in Unit-Tests, slf4j (nicht `System.out`, Plan §7), atomare Commits.
  Die Übernahme steht oben; die Python-spezifische Formulierung fällt weg.

- **`SESSIONS_ARCHIVE.md`-Rotation** — das Referenzprojekt rotiert Session-Blöcke in ein Archiv.
  ~~Verworfen in dieser Form~~ **Revision 2026-07-14:** Die Verwurfung war voreilig — sie beruhte
  darauf, dass `phase0/` nicht existierte und die generische Spec fehlte. Jetzt gilt die
  Rotation wie im Referenzprojekt: aktueller Block in `phase_N/CLAUDE.md`, alte Blöcke verbatim
  in `phase_N/SESSIONS_ARCHIVE.md`, newest-first. Siehe `docs/DOC_LAYERS_CONVENTION.md`.

- **FastAPI/Web-UI** — das Referenzprojekt hat ein read-mostly Dashboard.
  Verworfen: Traktion hat keinen Web-Server. Der Leitstand (Z11) ist ein Minecraft-Block, kein
  HTTP-Endpunkt.

- **Lightstreamer/yFinance** — Streaming- und Daten-Feeds des Referenzprojekts.
  Verworfen: Traktion hat keine externen Daten-Feeds. Alle Daten entstehen in der Simulation.

- **"Never test against real money first"** — das Referenzprojekt tradet reales Geld.
  Verworfen: Traktion hat kein Geld, kein Live-System, keine Demo. Die "Hard Rule" hier ist:
  kein `net.minecraft.*` in `train-core` (Plan §3 Regel 1).

---

## Logging (Plan §7)

> **Festgeschrieben in P0.2 Step 4.** Gilt für allen Code ab P1.

- **`slf4j` in `train-mc`, nie `System.out`.** Plan §7: "Code-Logging: `slf4j`, nie `System.out`."
  Fabric bringt slf4j mit; `train-mc` nutzt den Fabric-Logger (`LoggerFactory.getLogger(...)`).
- **`train-core` hat kein Logging-Framework.** Der Kern ist plain Java, null externe Abhängigkeiten
  außer Test-Bibliotheken (Plan §1, §3 Regel 1). Fehler im Kern werden über Exceptions und
  Test-Failures sichtbar, nicht über Log-Zeilen. Braucht ein Kern-Test Logging, ist der Test falsch.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** `LoggerFactory.getLogger(ModInitializer.class)`
  ist das etablierte Muster aus 1.21.x. Ob sich in 26.2 etwas geändert hat, ist ungeprüft.
  Die [VERIFY]-Marke bleibt stehen, bis P0.4 oder P4 echte 26.2-Quellen prüft.
- **Kein `System.out` / `System.err` im Produktivcode.** Ausnahme: `train-core`-Test-Code darf
  `System.out` für Debug-Ausgaben nutzen, die nicht in die Produktion gehen. Aber: besser
  `System.err` für Fehler, oder gar nichts und stattdessen eine Assertion.

---

## Testmatrix (Plan §7)

> **Festgeschrieben in P0.2 Step 4.** Gilt für die gesamte M1-Messung (Plan §6).

| Kategorie | Ort | Grün heißt | Metriken |
|---|---|---|---|
| **A** | `train-core` | `gradle :train-core:test` grün, Z-Ziel erfüllt | Iterationen bis grün · Diff-Zeilen · Regressionen · Property-Fälle bestanden · **Regel-2-Verstoß ja/nein** · **Z5-Tautologie ja/nein** |
| **B** | `train-mc` | manueller Smoke im Client + Z9/Z10 | Iterationen bis lauffähig · halluzinierte API-Aufrufe · Recherche-Schritte (Quellen gelesen?) · manuelle Eingriffe des Operators |

**Prinzipien:**

- **Kein Trial ohne vorher notierte Erwartung.** Wer erst nach dem Ergebnis weiß, was er erwartet
  hat, hat nichts gemessen (Plan §7).
- **A und B werden getrennt ausgewertet.** Wer A und B zu einer Zahl mittelt, löscht das einzige
  interessante Ergebnis der ganzen Studie (Plan §6). Kategorie A misst klassische Informatik
  (Graphen, Zustandsautomaten, numerische Bilanzen); Kategorie B misst 26.2-Spezifikum, das kein
  Modell im Training gesehen hat.
- **`m1/trials.jsonl` wird vom Operator geführt, nicht vom Agenten.** Plan §7: "Kein Agent schreibt
  in `trials.jsonl`. Die Messung gehört nicht dem Gemessenen." Felder siehe Plan §7.
- **Kategorie A ist deterministisch.** Fixed dt, geordnete Iteration, gesäter Zufall (Regel 8).
  Zwei Läufe mit gleichem Seed liefern bitgleiche Ergebnisse — das ist testbar als Invariante.
- **Kategorie B ist nicht deterministisch.** Client-Start, Rendering, Chunk-Load — das ist
  manueller Smoke, kein Unit-Test. Die Metriken erfassen deshalb auch *Recherche-Schritte* und
  *halluzinierte API-Aufrufe*, weil 26.2 hinter dem Trainingsschnitt liegt (Plan §6).

---

## Root-Layout

> **Festgeschrieben 2026-07-16.** Das Projekt-Root bleibt flach — Gradle und die Einstiegs-Doku
> erzwingen das. Aber es gibt eine Ordnung: jede Datei hat eine Kategorie und einen Verbleibgrund.

### Kategorien

| Kategorie | Dateien | Verbleibgrund |
|---|---|---|
| **Einstieg** | `AGENTS.md`, `CLAUDE.md`, `README.md` | Harness-neutraler Einstieg, Single Source of Truth, GitHub-Oberfläche |
| **Wahrheit** | `TRAKTION_OVERALL_PLAN.md`, `ROADMAP.md`, `ARCHITECTURE.md` | Mission, Phasen, Schnitt — verweisen aufeinander |
| **Messung** | `M1_PREREGISTRATION.md` | FROZEN (Plan §6), muss im Root bleiben — Git-History ist der Beweis |
| **Gradle-Build** | `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties` | Gradle erwartet diese im Root |
| **Gradle-Wrapper** | `gradlew`, `gradlew.bat`, `gradle/` | Standard-Gradle-Wrapper — `gradlew.bat` bleibt (Windows-Contributor-kompatibel) |
| **Harness** | `opencode.json` | Projekt-spezifische opencode-Konfiguration (Permissions, Agent-Referenz) |
| **Git** | `.gitignore` | Git |

### Was nicht ins Root gehört

- **Build-Artefakte:** `.gradle/`, `build/`, `train-core/build/`, `train-mc/build/`, `train-mc/.gradle/`
  — ignoriert via `.gitignore`.
- **Loom Dev-Run:** `train-mc/run/`, `train-mc/logs/` — ignoriert. Ausnahme: archivierte Crash-Logs
  können tracked bleiben (als historischer Beweis), aber neue Logs werden nicht committed.
- **Operator-Infrastruktur:** `example_project/`, `.opencode/auth.json`, `.opencode/node_modules/`,
  `skills-lock.json` — ignoriert.
- **Phasen-Doku:** `phase<N>/` — eigene Verzeichnisse, nicht im Root.
- **Konzept-Doku:** `docs/` — eigenes Verzeichnis.

### Regel

- **Keine neuen Dateien im Root ohne Kategorie.** Wenn eine Datei nicht in eine der obigen Kategorien
  passt, gehört sie nicht ins Root — sie gehört nach `docs/`, `phase<N>/` oder wird gar nicht committed.
- **Root bleibt flach.** Keine Unterverzeichnisse außer den Modul-Verzeichnissen (`train-core/`,
  `train-mc/`), `docs/`, `phase<N>/`, `m1/`, `gradle/`.

---

## Meta

`docs/DOC_LAYERS_CONVENTION.md` wurde in P0.2 erstellt (2026-07-14) — die generische Spec der
Doc-Layers-Konvention, aus dem Referenzprojekt übernommen aber projekt-agnostisch. Die Header-Card
dieser Datei zeigt dorthin (down-Link).
