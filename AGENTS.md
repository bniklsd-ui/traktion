# Traktion

Fabric-Mod. Wahrheit steht in `TRAKTION_OVERALL_PLAN.md`. Lies `docs/INDEX.md` als Karte.
Referenzierte Dateien werden nicht automatisch geladen.

## Einstieg für jede Session

1. **`docs/INDEX.md`** — L0-Karte. Eine Zeile pro Doku, Navigation über up/down-Links in den
   Header-Cards. **Keine Verzeichnissuche.**
2. **`CLAUDE.md`** — Single Source of Truth. Projekt-Identität, Phasenstatus, harte Grenzen.
3. **`docs/plans/PHASE<N>_PLAN.md`** — aktueller Phasenplan. Lies zuerst den
   `## Session stopped`-Block — er sagt dir, wo die letzte Session endete.
4. **`TRAKTION_OVERALL_PLAN.md`** — §2 (Locks), §3 (Hard Rules), §4 (Ziele), §9 (Anti-Patterns).

## On-Need-to-Know

Referenzierte Dateien werden **nicht** automatisch geladen. Öffne sie selbst, wenn eine
Header-Card oder ein Verweis dich dorthin schickt. Lies das Minimum, das die Layer erlauben,
aber überspringe nie das Verständnis des Codes, den eine Änderung berührt.

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
