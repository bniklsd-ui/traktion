---
status: live
purpose: Generische Spec des Doc-Layers-Systems — Layer-Modell, Header-Card-Format, Rotationsregel für Session-stopped-Blöcke. Projekt-agnostisch, gilt für alle Phasen.
read-when: vor Anlage einer neuen .md; vor Anlage eines phase_N/-Verzeichnisses; bei jeder Rotation eines Session-stopped-Blocks; bei Zweifel, wo ein Block lebt
detail: L2
up: ./CLAUDE.md
down:
updated: 2026-07-14
---

# Doc-Layers-Konvention — generische Spec

> **Herkunft:** Destilliert aus dem Referenzprojekt (Trading-Bot) in P0.1, ausgefaltet in P0.2.
> Diese Datei ist die Quelle, aus der die Layer-Regeln sprechen. `docs/CONVENTIONS.md` referenziert
> hierher. Projekt-agnostisch — gilt für jede Phase N.

---

## Layer-Modell

```
ROOT-LEVEL (repo-weit, selten angetastet)
  CLAUDE.md                        — SSoT, Projekt-Identität, Phasenstatus-Tabelle. Entry-Point.
                                     KEIN Session-stopped-Block hier. Schlank bleiben.
  README.md                        — menschliche Übersicht + Machine-Setup
  ARCHITECTURE.md                  — System-Layer/Komponenten/Risiken
  ROADMAP.md                       — Phasen-Übersicht P0–PN + Status pro Phase
  docs/INDEX.md                    — L0-Karte: eine Zeile pro Doku im ganzen Repo
  docs/DOC_LAYERS_CONVENTION.md    — diese Datei. Die generische Spec.

PER-PHASE (ein Set pro phase_N/-Verzeichnis)
  phase_N/CLAUDE.md                — Phasen-Kopf (living doc). Header-Card + Build-Log +
                                     der AKTUELLE Session-stopped-Block. Genau EIN Block
                                     hier — der neueste.
  phase_N/README.md                — menschliche/public-Oberfläche dieser Phase
  phase_N/SESSIONS_ARCHIVE.md      — alte Session-stopped-Blöcke. Rotation: wenn ein neuer
                                     Block in CLAUDE.md geschrieben wird, wandert der
                                     bisher-neueste verbatim, uneditiert, newest-first hierher.
                                     Archiv hat keine Größenbeschränkung. CLAUDE.md schon (~40KB).

SNAPSHOTS (datiert, frozen — nie editiert, nur superseded)
  docs/concepts/*.md               — Konzept/Plan, aus dem eine Phase gebaut wurde
                                     (gelockte Entscheidungen, Specs)
  *_HANDOVER.md                    — Session-zu-Session- oder Phase-zu-Phase-Kontexttransfer
  docs/sessions/*.md               — alte Root-CLAUDE.md-Narrative, komplett rausrotiert
```

---

## Header-Card-Format

Jede lebende `.md` beginnt mit einer YAML-Frontmatter-Header-Card (≤15 Zeilen).

Pflichtfelder:
- `status:` — `live` · `plan` · `frozen` · `archive`
- `purpose:` — eine Zeile, wofür die Datei da ist
- `read-when:` — wann die Datei gelesen werden soll (Hook für den Agenten)
- `detail:` — `L0` · `L1` · `L2` · `L3` (Body-Tiefe)
- `up:` — übergeordnete Datei (die, die hierher verweist)
- `down:` — untergeordnete Dateien (die, auf die diese verweist). Leer erlaubt.
- `updated:` — Datum der letzten inhaltlichen Änderung (YYYY-MM-DD)

Optionale Felder:
- `supersedes:` — Vorgängerdatei, die diese ersetzt (bei Snapshots)
- `related:` — Querverweis ohne Hierarchie

Die Header-Card ist L1-tauglich: ein Agent entscheidet aus ≤15 Zeilen, ob der Body
lesenswert ist. Token-Disziplin bei einem langlaufenden Projekt mit vielen Doku-Dateien.

---

## Rotationsregel (Session-stopped-Blöcke)

> **Der eine Satz:** Aktueller Session-Block → `phase_N/CLAUDE.md`.
> Alles Ältere → `phase_N/SESSIONS_ARCHIVE.md`, verbatim, newest-first.

**Ablauf:**
1. Eine Session endet. Der `## Session stopped`-Block wird in `phase_N/CLAUDE.md` geschrieben.
2. War dort schon ein Block (der bisher-neueste), wird dieser **verbatim, uneditiert** nach
   `phase_N/SESSIONS_ARCHIVE.md` verschoben — an den Anfang (newest-first).
3. In `phase_N/CLAUDE.md` steht danach genau ein Block: der neue, aktuelle.
4. Das Archiv wächst nach unten. Keine Größenbeschränkung. Kein Editieren alter Blöcke —
   sie sind Historie.

**Warum:** `phase_N/CLAUDE.md` ist der Entry-Point für die nächste Session. Er muss schlank
bleiben (~40KB Cap). Das Archiv trägt die Last. Ohne Rotation wird der Phasen-Kopf
unlesbar, und jede Session startet mit einem überladenen Entry-Point.

**Genau ein Block pro `phase_N/CLAUDE.md`** — nie zwei, nie null. Eine Session ohne
`## Session stopped`-Block ist ein Anti-Pattern (Plan §9).

---

## Neue .md anlegen

1. Header-Card schreiben (≤15 Zeilen YAML, Pflichtfelder siehe oben).
2. One-Liner in `docs/INDEX.md` ergänzen — Glyph + Größe + read-when-Hook.
3. Beides im selben Commit (Plan §11: "Neue `.md` ⇒ One-Liner in `docs/INDEX.md` im selben Commit").

**Glyphen in `docs/INDEX.md`:**
- 🔄 aktive Phase
- 📗 live/maintained
- 📕 datierter Snapshot (nie editieren)
- 📦 Archiv
- 📜 Konzept/Plan
- 🔒 gelockt/frozen
- 🏗 As-Built
- 📊 Messung
- 🧭 Einstieg

---

## Neue Phase beginnen

1. `phase_N/`-Verzeichnis anlegen.
2. `phase_N/CLAUDE.md` schreiben — Header-Card + Build-Log + erster Session-stopped-Block
   (am Ende der ersten Session dieser Phase).
3. `phase_N/SESSIONS_ARCHIVE.md` anlegen — leer bis zur ersten Rotation.
4. `phase_N/README.md` — die menschliche Oberfläche dieser Phase (optional in P0, Pflicht ab P1).
5. One-Liner für jede neue Datei in `docs/INDEX.md`, im selben Commit.
6. Root-`CLAUDE.md` Phasenstatus-Tabelle: neue Zeile für Phase N, down-Link auf `phase_N/CLAUDE.md`.

---

## Drift-Protokoll

Wird eine Konvention hier revidiert (z.B. weil eine Verwurfung sich als voreilig erweist),
wird das in `docs/CONVENTIONS.md` als Revision markiert — mit Datum, Begründung und Verweis
auf diese Datei. Historie wird nie umgeschrieben; die Revision ist ein neuer Eintrag.
