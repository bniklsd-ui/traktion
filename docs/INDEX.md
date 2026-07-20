---
status: live
purpose: L0-Karte des Doku-Systems. Eine Zeile pro Doku, Glyph + Größe + read-when-Hook. Navigationseinstieg für jede Session — verhindert Directory-Scans.
read-when: Session-Start; vor der Suche nach einer Doku; bevor eine neue .md erstellt wird (One-Liner hier ergänzen, im selben Commit)
detail: L0
up: ./CLAUDE.md
updated: 2026-07-20
---

# Traktion — Doku-Index

> Navigation über up/down-Links in den Header-Cards, nicht per Verzeichnissuche.
> Glyphen: 🔄 aktive Phase · 📗 live/maintained · 📕 datierter Snapshot · 📦 Archiv · 📜 Konzept/Plan · 🔒 gelockt/frozen · 🏗 As-Built · 📊 Messung · 🧭 Einstieg

---

## Einstieg (L0/L1)

| Doku | Größe | Wann lesen |
|---|---|---|
| 🧭 `CLAUDE.md` | L1 | Session-Start; Single Source of Truth |
| 🧭 `AGENTS.md` | L1 | Session-Start; harness-neutraler Einstieg |
| 📗 `README.md` | L1 | Repo-Besuch über GitHub; vor Setup einer Entwicklungsmaschine |
| 📜 `TRAKTION_OVERALL_PLAN.md` | L2 | vor jeder Architektur-Entscheidung; §2/§3/§4/§9 |
| 📗 `ROADMAP.md` | L1 | vor Phasen-Übergang; für schnellen Projekt-Überblick (P0–P6) |
| 📗 `ARCHITECTURE.md` | L1 | vor jeder Architektur-Entscheidung; bei der Frage "wo lebt dieser Code" |

## Konventionen & Konzepte

| Doku | Größe | Wann lesen |
|---|---|---|
| 📜 `docs/CONVENTIONS.md` | L2 | vor jeder neuen .md; vor jedem Commit; bei Zweifel, ob eine Regel gilt |
| 📗 `docs/DOC_LAYERS_CONVENTION.md` | L2 | vor Anlage einer neuen .md; vor Anlage eines phase_N/; bei Rotation eines Session-stopped-Blocks |

## Phasen (per phase_N/)

| Doku | Größe | Wann lesen |
|---|---|---|
| 🔄 `phase1/CLAUDE.md` | L1 | Session-Start in P1; Build-Log + aktueller Session-stopped-Block |
| 📦 `phase1/SESSIONS_ARCHIVE.md` | L3 | bei Bedarf an historische P1-Session-Details |
| 📗 `phase1/README.md` | L1 | Repo-Besuch; schneller P1-Überblick ohne Phasen-Kopf |
| 📕 `phase0/CLAUDE.md` | L1 | historisch — P0-Build-Log + letzter P0-Session-stopped-Block |
| 📦 `phase0/SESSIONS_ARCHIVE.md` | L3 | bei Bedarf an historische P0-Session-Details |
| 📕 `phase0/Fabric_Loom_Mappings_Fix_01.md` | L2 | bei Build-Fehlern mit Mappings/Remapping für 26.x; vor P0.4-Spike |
| 📕 `phase0/MC26_API_NOTES.md` | L2 | vor P4-Code (Entity/Renderer/Persistenz-API in 26.2); bei Fragen zur 26.2 API |

## Phasenpläne & Handover

| Doku | Größe | Wann lesen |
|---|---|---|
| 📜 `docs/plans/PHASE0_PLAN.md` | L2 | Ausführung von P0; Konzept/Plan, aus dem P0 gebaut wird |
| 📕 `docs/plans/PHASE0_HANDOVER.md` | L2 | vor P1-Plan-Entwurf; P0-Abschlussanalyse, Aufräum-Schritt, trials-Rohdaten |
| 📜 `docs/plans/PHASE1_PLAN.md` | L2 | Ausführung von P1; train-core Durchstich (Z1–Z4), Schritt-Sequenz, Akzeptanzkriterien |

## Messung (M1-Strang)

| Doku | Größe | Wann lesen |
|---|---|---|
| 🔒 `M1_PREREGISTRATION.md` | L2 | vor jedem Trial (Konstanten prüfen); bei Auswertung (Regel anwenden); nie zum Editieren |
| 📊 `m1/trials.jsonl` | — | wird vom Operator geführt, nicht vom Agenten |
