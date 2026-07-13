---
status: live
purpose: Welche Konventionen aus dem Referenzprojekt übernommen werden und welche bewusst verworfen sind — das Destillat, nicht das Referenz-Repo
read-when: Session-Start; vor jeder neuen .md; vor jedem Commit; bei Zweifel, ob eine Regel gilt
detail: L2
up: ./CLAUDE.md
down:
  - ./docs/DOC_LAYERS_CONVENTION.md   # [VERIFY — in P0.2 zu erstellen] Header-Card-Format, Layer-Modell
updated: 2026-07-13
---

# Traktion — Konventionen

> **Herkunft:** Destilliert aus dem Referenzprojekt (Trading-Bot, Python) in P0.1.
> Das Referenz-Repo selbst wird nach P0 nicht mehr gelesen (Plan §5/P0.1, Anti-Contamination).
> Diese Datei ist die einzige Spur — bewusst gekürzt, bewusst selektiv.
>
> **Code ist Wahrheit** über Konzept-Dokument über Status-Prosa. [VERIFY] bleibt stehen,
> bis jemand tatsächlich verifiziert hat.

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
  Verworfen in dieser Form: Traktion hat pro Phase ein `CLAUDE.md` mit genau einem
  `## Session stopped`-Block. Ob eine separate Archivdatei nötig wird, entscheidet sich in P1,
  wenn die erste Phase abgeschlossen wird. Vorläufig: ein Block pro Phasen-`CLAUDE.md`.

- **FastAPI/Web-UI** — das Referenzprojekt hat ein read-mostly Dashboard.
  Verworfen: Traktion hat keinen Web-Server. Der Leitstand (Z11) ist ein Minecraft-Block, kein
  HTTP-Endpunkt.

- **Lightstreamer/yFinance** — Streaming- und Daten-Feeds des Referenzprojekts.
  Verworfen: Traktion hat keine externen Daten-Feeds. Alle Daten entstehen in der Simulation.

- **"Never test against real money first"** — das Referenzprojekt tradet reales Geld.
  Verworfen: Traktion hat kein Geld, kein Live-System, keine Demo. Die "Hard Rule" hier ist:
  kein `net.minecraft.*` in `train-core` (Plan §3 Regel 1).

---

## Meta

Diese Datei wird in P0.2 um `docs/DOC_LAYERS_CONVENTION.md` ergänzt (die generische Spec der
Doc-Layers-Konvention, aus dem Referenzprojekt übernommen aber projekt-agnostisch). Die
Header-Card dieser Datei zeigt dann dorthin. Bis dahin gilt der Header-Card-Standard wie hier
praktiziert.
