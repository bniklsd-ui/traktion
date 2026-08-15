# Traktion

Fabric-Mod. Wahrheit steht in `TRAKTION_OVERALL_PLAN.md`. Lies `docs/INDEX.md` als Karte.
Referenzierte Dateien werden nicht automatisch geladen.

## Einstieg für jede Session

1. **`docs/INDEX.md`** — L0-Karte. Eine Zeile pro Doku, Navigation über up/down-Links in den
   Header-Cards. **Keine Verzeichnissuche.**
2. **`CLAUDE.md`** — Single Source of Truth. Projekt-Identität, Phasenstatus, harte Grenzen.
3. **`phase<N>/CLAUDE.md`** — Phasen-Kopf. Lies zuerst den `## Session stopped`-Block — er sagt
   dir, wo die letzte Session endete. (Aktuell: `phase0/CLAUDE.md`.)
4. **`docs/plans/PHASE<N>_PLAN.md`** — Konzept/Plan, aus dem die Phase gebaut wird. Schritt-Sequenz,
   Akzeptanzkriterien. (Aktuell: `docs/plans/PHASE0_PLAN.md`.)
5. **`TRAKTION_OVERALL_PLAN.md`** — §2 (Locks), §3 (Hard Rules), §4 (Ziele), §9 (Anti-Patterns).

## On-Need-to-Know

Referenzierte Dateien werden **nicht** automatisch geladen. Öffne sie selbst, wenn eine
Header-Card oder ein Verweis dich dorthin schickt. Lies das Minimum, das die Layer erlauben,
aber überspringe nie das Verständnis des Codes, den eine Änderung berührt.

## Modell-Politik

Zwei Modelle, klar getrennte Rollen (festgeschrieben 2026-08-15 nach P2-Abschluss,
Operator-Eingriff in `m1/trials.jsonl` P2-Zeile, `operator_eingriffe: 1`):

- **`minimax-coding-plan/MiniMax-M2.7` — Standard-Arbeitstier (Domäne).**
  Implementiert Phasen-Steps, Tests, Bugfixes, Note-Updates, Drift-Nachzüge.
  Schreibt **keinen** Phasenplan, **kein** Handover, **keine** §9-Anti-Pattern-Analysen.

- **`minimax-coding-plan/MiniMax-M3` — Plan-Agent (kognitive Aufgaben).**
  Schreibt `docs/plans/PHASE<N>_PLAN.md` (Phasenplan-Entwurf), `docs/plans/PHASE<N>_HANDOVER.md`
  (Phasen-Abschluss-Analyse). Macht §9-Anti-Pattern-Auswertungen, schwieriges Debugging,
  Studien-Design-Entscheidungen, Methodendiskussionen. Schreibt **keinen** Domänen-Code
  (`train-*/src/`-Änderungen), **keine** Tests.

**Trennregel (verbindlich):** Wenn eine Aufgabe ansteht, die in die andere Rolle fällt,
wird das Modell gewechselt — entweder durch Operator-Anweisung oder durch Eskalation
des Build-Agent an den Plan-Agent. Kein Modell "macht mal eben" die Rolle der anderen
mit — das war der Auslöser für den P2-Operator-Eingriff.

> **Historie:**
> - **GLM 5.2 (NVIDIA NIM)** wurde bis einschließlich P1 genutzt, aber aufgrund
>   schwieriger Nutzung abgelöst. Bezahlter Token-Plan von Minimax als Nachfolger.
> - **M3 für Handover** — 2026-08-15. Build-Agent (M2.7) wollte `PHASE2_HANDOVER.md`
>   autonom schreiben; Operator hat eingegriffen und Plan-Agent (M3) beauftragt —
>   Commit `ef321e6`. Seither gilt die Trennregel oben. In `m1/trials.jsonl` P2-Zeile
>   als `operator_eingriffe: 1` dokumentiert.

## Harte Grenzen (Plan §3, gekürzt)

- Kein `net.minecraft.*`, kein NBT, kein `ItemStack` in `train-core`.
- Die Physikformel existiert genau **EINMAL**. Planer und Simulator rufen dieselbe Funktion.
- Der Planer ruft **NIE** den Simulator auf. Sonst ist Z5 tautologisch.
- Fixed dt, geordnete Iteration, gesäter Zufall. Kein Wall-Clock, kein `HashSet` in der Physikschleife.
- Kein Interface ohne zwei heute benennbare Implementierungen.
- Kein roher OpenGL-Call. Nur Blaze3D.

## Arbeitsweise

- **Code ist Wahrheit** > Konzept-Dokument > Status-Prosa. Bei Drift: Doku fixen, sagen.
- **TDD in `train-core`.** Ein Subtask ist nicht fertig, bevor `gradle :train-core:test` grün ist.
- **Atomare Commits**, Format `<scope>: <imperative>`.
- **Commit ⇒ Note-Update:** Statuszeile + `## Session stopped` im selben Commit.
- **Neue `.md` ⇒** L1-Header-Card + One-Liner in `docs/INDEX.md` im selben Commit.
- **`[VERIFY]` bleibt stehen**, bis jemand tatsächlich verifiziert hat.

## Anti-Patterns (Plan §9)

Wenn du auf eines stößt — auch im eigenen Entwurf: **anhalten, benennen, fragen.**
Nicht stillschweigend korrigieren, nicht umgehen. Diese Momente sind Messpunkte.

## Handover

Nach ~20–30 Tool-Calls: `## Session stopped`-Block schreiben, dann anhalten.
