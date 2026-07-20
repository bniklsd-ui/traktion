---
status: live
purpose: Phasen-Kopf für P1 — train-core Durchstich (Z1–Z4). Build-Log + der aktuelle Session-stopped-Block. Genau ein Block hier.
read-when: Session-Start in P1; vor jedem P1-Schritt; bei Rotation des Session-stopped-Blocks
detail: L1
up: ../CLAUDE.md
down:
  - ../docs/plans/PHASE1_PLAN.md      # Konzept/Plan, aus dem P1 gebaut wird
  - ./SESSIONS_ARCHIVE.md             # alte Session-stopped-Blöcke
updated: 2026-07-20
---

# Phase 1 — `train-core`: Durchstich

> **Status:** P1 läuft. Step 0 (Altlasten) ✅ · Step 0b (Doc-Drift) ✅ · Step 1 (jqwik) ✅ ·
> Step 2 (dieser Phasen-Kopf) ✅. Nächster Schritt: Step 3 (RailGraph, Z1).
>
> **Konzept:** `docs/plans/PHASE1_PLAN.md` (gelockte Entscheidungen T-D20–T-D24, Schritt-Sequenz,
> Akzeptanzkriterien).
>
> **Kategorie A** (Plan §5/P1, §7). Klassische Informatik: Graphen, Physik, Energiebilanz,
> Zustandsautomaten. Kein Minecraft, kein Verschleiß, kein Planer.

---

## Build-Log

| Schritt | Status | Commit | Notiz |
|---|---|---|---|
| Step 0.1 — build-traktion.md committen (deny→ask) | ✅ | d4916c9 | Permission-Änderung committet |
| Step 0.2 — fabric.mod.json Java 21→25 | ✅ | 188bea2 | Manifest an Code-Wahrheit angeglichen |
| Step 0.3 — P0-Trials Rohdaten an Nikinger | ⏳ | — | Rohdaten übergeben, warte auf Bestätigung |
| Step 0b — Doc-Drift prüfen | ✅ | — | Keine Drift gefunden (leer, wie vom Plan erwartet) |
| Step 1 — jqwik 1.9.0 einkommentieren (T-D20) | ✅ | 560b178 | [VERIFY] aufgelöst: jqwik läuft unter Gradle 9.5.1 |
| Step 2 — phase1/CLAUDE.md + SESSIONS_ARCHIVE + README | ✅ | (dieser Commit) | Phasen-Kopf erstellt |
| Step 3 — RailGraph (Z1) | ⏳ | — | nächster Schritt |
| Step 4 — Consist (T-D7) | ⏳ | — | |
| Step 5 — Physics.requiredPowerW (Regel 2, Z3 prep) | ⏳ | — | |
| Step 6 — PowerGrid + PowerSupply (Z4 ohne condition, T-D22) | ⏳ | — | |
| Step 7 — Simulator (Z3, T-D13, T-D24) | ⏳ | — | |
| Step 8 — BlockSection (Z2, T-D23) | ⏳ | — | |
| Step 9 — Integration (A→B, Stromknappheit, Z2+Z3) | ⏳ | — | |
| Step 10 — Done-When-Verifikation + Phasen-Abschluss | ⏳ | — | |

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
  ein handgeschriebener Test oft übersieht. Korrigiert auf `unaryMinusIsTotal` (echte Invariante).
  `.jqwik-database` zur `.gitignore` hinzugefügt (Build-Artefakt).
- **Step 2** (dieser Commit): `phase1/CLAUDE.md` + `phase1/SESSIONS_ARCHIVE.md` +
  `phase1/README.md` erstellt. `docs/INDEX.md` One-Liner ergänzt. Root-`CLAUDE.md`
  Phasenstatus: P1 = 🔄 aktiv, down-Link auf `phase1/CLAUDE.md`.

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
- **Step 0.3 — P0-Trials:** Rohdaten an Nikinger übergeben (vier JSONL-Zeilen für P0.1, P0.2,
  P0.3, P0.4). Warte auf Bestätigung. Dann Eintrag in `m1/trials.jsonl` (mit `ask`-Permission)
  und Commit `m1: backfill P0 trials (operator-confirmed entries)`.
- **Step 3 — RailGraph (Z1):** `RailGraph`, `Node`, `Edge`, `RailKind` (fünf Werte, T-D21).
  TDD: Invarianten (kein verwaister Knoten, keine Kante ohne zwei Endpunkte, `RailKind` gesetzt,
  `gradient`/Länge endlich). Optional jqwik-Property-Test für Invarianten unter beliebigen
  Mutations-Sequenzen.

### Open questions / blockers
- **Step 0.3 blockiert auf Nikingers Bestätigung** der vier JSONL-Zeilen. Siehe Chat-Output
  dieser Session. `diff_lines`-Definition (nur Insertions vs. Insertions+Deletions) und
  `recherche_schritte`-Zählungen sind Schätzungen — Nikinger kann anpassen.
- **[VERIFY] Fabric-Logging-Konvention in 26.2:** bleibt bis P4 (nicht P1-relevant).
- **Tool-Calls:** Diese Session benutzte ~22 Tool-Calls (Lese-Kontext + Step 0–2 + jqwik-Verify).
