---
status: archive
purpose: Archiv für alte Session-stopped-Blöcke der Phase 1. Rotation: wenn ein neuer Block in phase1/CLAUDE.md geschrieben wird, wandert der bisher-neueste verbatim, uneditiert, newest-first hierher. Kein Editieren alter Blöcke — sie sind Historie.
read-when: bei Bedarf an historische Session-Details von P1
detail: L3
up: ./CLAUDE.md
down:
updated: 2026-07-20
---

# Phase 1 — Session-Archiv

> Alte Session-stopped-Blöcke, verbatim, newest-first. Kein Editieren — Historie.

---

## Session stopped — 2026-07-20 (P1 Step 0–2: Altlasten, jqwik, Phasen-Kopf)

### Completed (diese Session)
- **Step 0.1** (d4916c9): `.opencode/agents/build-traktion.md` committet — Permission-Änderung
  (`m1/**`, `M1_*.md`, `docs/plans/**`, `TRAKTION_OVERALL_PLAN.md` von `deny` auf `ask`).
  Stand war bereits da, nur uncommitted. `ask` zwingt zur Bestätigung, was ausreicht.
- **Step 0.2** (188bea2): `train-mc/src/main/resources/fabric.mod.json` Java-Version 21→25.
  `gradle :train-mc:build` grün (keine Regression). Manifest an Code-Wahrheit angeglichen
  (`gradle.properties` `java_mod_target=25`, `train-mc/build.gradle.kts` sagt Java 25).
- **Step 0b:** Doc-Drift geprüft — keine gefunden (wie vom Plan erwartet). `CLAUDE.md`
  Phasenstatus (P0 ✅, P1 ⏳), `docs/INDEX.md` (listet `PHASE1_PLAN.md`), `phase0/CLAUDE.md`
  Session-stopped-Block (verweist auf P1 als Next) — alle korrekt.
- **Step 1** (560b178): jqwik 1.9.0 einkommentiert (T-D20). `[VERIFY] "jqwik unter Gradle 9.5.1"`
  aus P0 **aufgelöst** — jqwik 1.9.0 läuft unter Gradle 9.5.1 mit JUnit 5.12.2.
  `JqwikSmokeTest` beweist, dass die Engine läuft: der erste Property-Test
  (`Math.abs(i) >= 0`) scheiterte am `Integer.MIN_VALUE`-Overflow — genau der Edge-Case, den
  ein handgeschriebene Test oft übersieht. Korrigiert auf `unaryMinusIsTotal` (echte Invariante).
  `.jqwik-database` zur `.gitignore` hinzugefügt (Build-Artefakt).
- **Step 2** (fa5a3b3): `phase1/CLAUDE.md` + `phase1/SESSIONS_ARCHIVE.md` +
  `phase1/README.md` erstellt. `docs/INDEX.md` One-Liner ergänzt. Root-`CLAUDE.md`
  Phasenstatus: P1 = 🔄 aktiv, down-Link auf `phase1/CLAUDE.md`.
- **Step 0.3** (dieser Commit): P0-Trials nachgetragen. Nikinger hat die vier JSONL-Zeilen
  (P0.1, P0.2, P0.3, P0.4) bestätigt. Eingetragen in `m1/trials.jsonl` (4 Zeilen, gültiges
  JSON, je eine pro P0-Sub-Schritt). `diff_lines` = Insertions pro Sub-Schritt;
  `recherche_schritte` sind Schätzungen aus dem Handover. Commit-Message wie vom Plan
  vorgegeben.

### Beobachtungen / Messpunkte
- **jqwik Anti-AI-Klausel (Confound §4.3):** Bei der Recherche/Installation auf jqwik 1.10.x
  gestoßen? **Nein** — der Plan pinnt 1.9.0, und ich habe nicht nach neueren Versionen gesucht.
  T-D20 war klar. Keine Prompt-Injection gelesen, keine Tests gelöscht.
- **Plan-Lücke `phase1/README.md`:** Der P1-Plan Step 2 listet `phase1/README.md` nicht auf,
  obwohl `docs/DOC_LAYERS_CONVENTION.md` "Neue Phase beginnen" Punkt 4 sagt: "phase_N/README.md
  — die menschliche Oberfläche dieser Phase (optional in P0, Pflicht ab P1)." Ich habe es als
  Stub erstellt (folgt der Konvention, die der Plan §11 referenziert) und im Commit-Message
  dokumentiert. Kein Anti-Pattern — die Konvention hat Vorrang für Hygiene-Dateien.
- **`Math.abs(Integer.MIN_VALUE)`-Fehlschlag:** Ein Lehrmoment. jqwik fand in der ersten
  Property genau den Overflow-Edge-Case. Das ist ein früher Beweis, dass Property-Testing
  mehr deckt als handgeschriebene Tests — relevant für Z5 (P3, property-based).

### Next
- **Step 3 — RailGraph (Z1):** `RailGraph`, `Node`, `Edge`, `RailKind` (fünf Werte, T-D21).
  TDD: Invarianten (kein verwaister Knoten, keine Kante ohne zwei Endpunkte, `RailKind` gesetzt,
  `gradient`/Länge endlich). Optional jqwik-Property-Test für Invarianten unter beliebigen
  Mutations-Sequenzen.

### Open questions / blockers
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** bleibt bis P4 (nicht P1-relevant).
- **Tool-Calls:** Diese Session benutzte ~26 Tool-Calls (Lese-Kontext + Step 0–2 + jqwik-Verify +
  Step 0.3 trials-Eintrag).
